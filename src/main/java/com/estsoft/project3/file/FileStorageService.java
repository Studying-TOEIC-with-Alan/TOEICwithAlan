package com.estsoft.project3.file;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RequiredArgsConstructor
@Service
public class FileStorageService {

    private final S3Client s3Client;

    @Value("${AWS_S3_BUCKET}")
    private String bucketName;

    @Value("${AWS_REGION_STATIC}")
    private String region;

    public String uploadSingleFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + extension;
        String filePath = "upload/" + fileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(filePath)
            .contentType(file.getContentType())
            .build();

        s3Client.putObject(putObjectRequest,
            RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return filePath;
    }

    public void deleteImagesByKeys(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            String fullKey = key.startsWith("upload/") ? key : "upload/" + key;
            System.out.println("삭제 시도 키: " + fullKey);
            
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fullKey)
                .build();

            s3Client.deleteObject(request);
        }
    }
}
