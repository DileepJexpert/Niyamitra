package com.niyamitra.document.model;

import com.niyamitra.common.enums.DocumentProcessingStatus;
import com.niyamitra.common.enums.FileType;
import com.niyamitra.common.enums.UploadSource;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "niyamitra_documents", schema = "niyamitra_documents")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NiyamitraDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "s3_key", length = 500)
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type")
    private FileType fileType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_source")
    private UploadSource uploadSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status")
    @Builder.Default
    private DocumentProcessingStatus processingStatus = DocumentProcessingStatus.UPLOADED;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extraction_result", columnDefinition = "jsonb")
    private Map<String, Object> extractionResult;

    @CreationTimestamp
    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    @Column(name = "uploaded_by")
    private UUID uploadedBy;
}
