package com.estsoft.project3.configuration;


import com.estsoft.project3.domain.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@RequiredArgsConstructor
@Configuration
public class AmazonS3Config {

    private final S3Properties s3Properties;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
            s3Properties.getCredentials().getAccessKey(),
            s3Properties.getCredentials().getSecretKey()
        );

        return S3Client.builder()
            .region(Region.of(s3Properties.getRegion()))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
    }

}
