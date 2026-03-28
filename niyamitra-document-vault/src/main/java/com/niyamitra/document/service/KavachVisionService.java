package com.niyamitra.document.service;

import com.niyamitra.common.config.KafkaTopics;
import com.niyamitra.common.enums.DocumentProcessingStatus;
import com.niyamitra.common.event.DocumentExtractedEvent;
import com.niyamitra.common.event.DocumentUploadedEvent;
import com.niyamitra.document.model.NiyamitraDocument;
import com.niyamitra.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;

/**
 * Kavach Vision — Document AI extraction service.
 * Listens for uploaded documents, performs text extraction,
 * and publishes extraction results.
 *
 * Phase 2: Basic text extraction from document metadata.
 * Future: OCR via Tesseract/Cloud Vision, Claude Vision for complex documents.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KavachVisionService {

    private final DocumentRepository documentRepository;
    private final StorageService storageService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = KafkaTopics.DOCUMENT_UPLOADED, groupId = "kavach-vision")
    @Transactional
    public void onDocumentUploaded(DocumentUploadedEvent event) {
        log.info("Kavach Vision processing document: {}, correlationId={}", event.documentId(), event.correlationId());

        Optional<NiyamitraDocument> docOpt = documentRepository.findById(event.documentId());
        if (docOpt.isEmpty()) {
            log.warn("Document not found: {}", event.documentId());
            return;
        }

        NiyamitraDocument document = docOpt.get();
        document.setProcessingStatus(DocumentProcessingStatus.PROCESSING);
        documentRepository.save(document);

        try {
            Map<String, Object> extractionResult = extractDocumentData(document);

            document.setProcessingStatus(DocumentProcessingStatus.EXTRACTED);
            document.setExtractionResult(extractionResult);
            documentRepository.save(document);

            DocumentExtractedEvent extractedEvent = new DocumentExtractedEvent(
                    UUID.randomUUID(),
                    document.getId(),
                    document.getTenantId(),
                    document.getTaskId(),
                    extractionResult,
                    Instant.now(),
                    event.correlationId()
            );
            kafkaTemplate.send(KafkaTopics.DOCUMENT_EXTRACTED, document.getId().toString(), extractedEvent);

            log.info("Kavach Vision extraction complete for document: {}", document.getId());
        } catch (Exception e) {
            log.error("Kavach Vision extraction failed for document: {}", document.getId(), e);
            document.setProcessingStatus(DocumentProcessingStatus.FAILED);
            documentRepository.save(document);
        }
    }

    /**
     * Extract data from a document.
     * Phase 2: Extracts metadata + basic file info.
     * Future enhancement: OCR text extraction, Claude Vision analysis.
     */
    private Map<String, Object> extractDocumentData(NiyamitraDocument document) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("documentId", document.getId().toString());
        result.put("fileName", document.getOriginalFilename());
        result.put("fileType", document.getFileType().name());
        result.put("fileSizeBytes", document.getFileSizeBytes());
        result.put("extractedAt", Instant.now().toString());
        result.put("extractionMethod", "METADATA");

        // Detect document category from filename patterns
        String filename = document.getOriginalFilename().toLowerCase();
        String detectedCategory = detectCategory(filename);
        result.put("detectedCategory", detectedCategory);

        // Extract potential dates from filename
        List<String> detectedDates = extractDatesFromFilename(filename);
        if (!detectedDates.isEmpty()) {
            result.put("detectedDates", detectedDates);
        }

        // Detect potential document type
        String documentType = detectDocumentType(filename);
        result.put("documentType", documentType);

        result.put("confidence", "LOW");
        result.put("requiresManualReview", true);

        return result;
    }

    private String detectCategory(String filename) {
        if (filename.contains("cto") || filename.contains("consent") || filename.contains("pollution"))
            return "ENVIRONMENTAL";
        if (filename.contains("fire") || filename.contains("noc"))
            return "FIRE_SAFETY";
        if (filename.contains("factory") || filename.contains("license"))
            return "FACTORY_LICENSE";
        if (filename.contains("hazardous") || filename.contains("waste"))
            return "HAZARDOUS_WASTE";
        if (filename.contains("boiler"))
            return "BOILER_SAFETY";
        if (filename.contains("labor") || filename.contains("labour") || filename.contains("esi") || filename.contains("epf"))
            return "LABOR";
        return "UNKNOWN";
    }

    private List<String> extractDatesFromFilename(String filename) {
        List<String> dates = new ArrayList<>();
        // Simple pattern: look for YYYY-MM-DD or DDMMYYYY patterns
        java.util.regex.Pattern datePattern = java.util.regex.Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
        java.util.regex.Matcher matcher = datePattern.matcher(filename);
        while (matcher.find()) {
            dates.add(matcher.group(1));
        }
        return dates;
    }

    private String detectDocumentType(String filename) {
        if (filename.contains("certificate") || filename.contains("cert"))
            return "CERTIFICATE";
        if (filename.contains("receipt") || filename.contains("challan"))
            return "RECEIPT";
        if (filename.contains("report"))
            return "REPORT";
        if (filename.contains("application") || filename.contains("form"))
            return "APPLICATION";
        if (filename.contains("renewal"))
            return "RENEWAL";
        if (filename.contains("invoice"))
            return "INVOICE";
        return "GENERAL";
    }
}
