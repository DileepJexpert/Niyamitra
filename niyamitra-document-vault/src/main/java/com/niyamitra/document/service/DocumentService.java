package com.niyamitra.document.service;

import com.niyamitra.common.config.KafkaTopics;
import com.niyamitra.common.enums.DocumentProcessingStatus;
import com.niyamitra.common.enums.FileType;
import com.niyamitra.common.enums.UploadSource;
import com.niyamitra.common.event.DocumentUploadedEvent;
import com.niyamitra.document.model.NiyamitraDocument;
import com.niyamitra.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final StorageService storageService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public NiyamitraDocument uploadDocument(UUID tenantId, UUID taskId, UUID uploadedBy,
                                            UploadSource uploadSource, MultipartFile file) {
        try {
            FileType fileType = detectFileType(file.getOriginalFilename());

            String s3Key = storageService.uploadFile(
                    tenantId,
                    file.getOriginalFilename(),
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );

            NiyamitraDocument document = NiyamitraDocument.builder()
                    .tenantId(tenantId)
                    .taskId(taskId)
                    .originalFilename(file.getOriginalFilename())
                    .s3Key(s3Key)
                    .fileType(fileType)
                    .fileSizeBytes(file.getSize())
                    .uploadSource(uploadSource)
                    .processingStatus(DocumentProcessingStatus.UPLOADED)
                    .uploadedBy(uploadedBy)
                    .build();

            document = documentRepository.save(document);

            // Publish event for Kavach Vision Worker
            UUID correlationId = UUID.randomUUID();
            DocumentUploadedEvent event = new DocumentUploadedEvent(
                    UUID.randomUUID(), document.getId(), tenantId, s3Key,
                    fileType, Instant.now(), correlationId, "NIYAMITRA"
            );
            kafkaTemplate.send(KafkaTopics.DOCUMENT_UPLOADED, document.getId().toString(), event);

            log.info("Document uploaded: {} for tenant {}, correlationId={}", document.getId(), tenantId, correlationId);
            return document;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload document", e);
        }
    }

    public NiyamitraDocument getDocument(UUID docId) {
        return documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + docId));
    }

    public String getDownloadUrl(UUID docId) {
        NiyamitraDocument doc = getDocument(docId);
        return storageService.getPresignedDownloadUrl(doc.getS3Key());
    }

    public List<NiyamitraDocument> listDocuments(UUID tenantId, UUID taskId) {
        if (taskId != null) {
            return documentRepository.findByTenantIdAndTaskId(tenantId, taskId);
        }
        return documentRepository.findByTenantId(tenantId);
    }

    @Transactional
    public void softDelete(UUID docId) {
        NiyamitraDocument doc = getDocument(docId);
        doc.setProcessingStatus(DocumentProcessingStatus.FAILED);
        documentRepository.save(doc);
    }

    private FileType detectFileType(String filename) {
        if (filename == null) return FileType.PDF;
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return FileType.JPEG;
        if (lower.endsWith(".png")) return FileType.PNG;
        return FileType.PDF;
    }
}
