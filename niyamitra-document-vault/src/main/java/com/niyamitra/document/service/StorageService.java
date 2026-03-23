package com.niyamitra.document.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class StorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public StorageService(
            @Value("${minio.endpoint}") String endpoint,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.bucket-name}") String bucketName) {
        this.bucketName = bucketName;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        ensureBucketExists();
    }

    private void ensureBucketExists() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created MinIO bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.warn("Could not check/create MinIO bucket: {}", e.getMessage());
        }
    }

    public String uploadFile(UUID tenantId, String filename, InputStream inputStream, long size, String contentType) {
        String s3Key = tenantId + "/" + UUID.randomUUID() + "/" + filename;
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(s3Key)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
            log.info("Uploaded file to MinIO: {}", s3Key);
            return s3Key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }

    public String getPresignedDownloadUrl(String s3Key) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(s3Key)
                    .method(Method.GET)
                    .expiry(15, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }
}
