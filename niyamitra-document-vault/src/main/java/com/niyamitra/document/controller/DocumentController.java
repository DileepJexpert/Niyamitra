package com.niyamitra.document.controller;

import com.niyamitra.common.enums.UploadSource;
import com.niyamitra.document.model.NiyamitraDocument;
import com.niyamitra.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<NiyamitraDocument> uploadDocument(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) UUID taskId,
            @RequestParam(required = false) UUID uploadedBy,
            @RequestParam(defaultValue = "NIYAMITRA_WEB") UploadSource uploadSource,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.uploadDocument(tenantId, taskId, uploadedBy, uploadSource, file));
    }

    @GetMapping("/{docId}")
    public ResponseEntity<NiyamitraDocument> getDocument(@PathVariable UUID docId) {
        return ResponseEntity.ok(documentService.getDocument(docId));
    }

    @GetMapping("/{docId}/download")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable UUID docId) {
        return ResponseEntity.ok(Map.of("url", documentService.getDownloadUrl(docId)));
    }

    @GetMapping
    public ResponseEntity<List<NiyamitraDocument>> listDocuments(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) UUID taskId) {
        return ResponseEntity.ok(documentService.listDocuments(tenantId, taskId));
    }

    @DeleteMapping("/{docId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID docId) {
        documentService.softDelete(docId);
        return ResponseEntity.noContent().build();
    }
}
