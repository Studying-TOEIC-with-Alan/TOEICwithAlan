package com.estsoft.project3.file;

import static org.assertj.core.api.Assertions.assertThat;

import com.estsoft.project3.config.MockS3ClientConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
@ActiveProfiles("test")
@Import(MockS3ClientConfig.class)
class FileStorageServiceTest {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private S3Client s3Client;

    @Test
    void uploadFile() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "hello.txt",
            "text/plain",
            "Hello, S3 Test!".getBytes(StandardCharsets.UTF_8)
        );

        // when
        String filePath = fileStorageService.uploadSingleFile(file);

        // then
        assertThat(filePath).isNotBlank();
        assertThat(filePath).startsWith("upload/");
        assertThat(filePath).endsWith(".txt");
    }

    @Test
    void deleteEmptyOrNullKeys() {
        // given
        List<String> emptyKeys = List.of();
        List<String> nullKeys = null;

        // when / then
        fileStorageService.deleteImagesByKeys(emptyKeys);
        fileStorageService.deleteImagesByKeys(nullKeys);
    }

    @Test
    void deleteWithKeys() {
        // given
        List<String> keys = List.of("upload/test1.png", "test2.jpg");

        // when
        fileStorageService.deleteImagesByKeys(keys);

        // then
    }
}