package com.estsoft.project3.config;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.s3.S3Client;

@TestConfiguration
public class MockS3ClientConfig {

    @Bean
    public S3Client s3Client() {
        return mock(S3Client.class);
    }
}
