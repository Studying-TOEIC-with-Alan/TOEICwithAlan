package com.estsoft.project3.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Profile("!test")
@Configuration
public class AmazonS3Config {

    @Value("${AWS_CREDENTIALS_ACCESS_KEY}")
    private String accessKey;

    @Value("${AWS_CREDENTIALS_SECRET_KEY}")
    private String secretKey;

    @Value("${AWS_REGION_STATIC}")
    private String region;

    @Bean
    public S3Client s3Client() {
        System.out.println("AWS_REGION_STATIC=" + region);

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .build();
    }
}