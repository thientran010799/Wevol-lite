package com.wevol.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CORSConfiguration;
import software.amazon.awssdk.services.s3.model.CORSRule;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class R2Config {

    private static final Logger log = LoggerFactory.getLogger(R2Config.class);

    @Value("${r2.account-id}")
    private String accountId;

    @Value("${r2.access-key}")
    private String accessKey;

    @Value("${r2.secret-key}")
    private String secretKey;

    @Value("${r2.bucket}")
    private String bucket;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Bean
    public S3Client r2Client() {
        return S3Client.builder()
                .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    @Bean
    public S3Presigner r2Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    @Bean
    public CommandLineRunner configureBucketCors(S3Client r2Client) {
        return args -> {
            List<String> origins = new ArrayList<>(List.of(
                    "http://localhost:5173",
                    "http://localhost:5174"
            ));
            if (!origins.contains(frontendUrl)) {
                origins.add(frontendUrl);
            }
            log.info("[R2-CORS] Configuring CORS for bucket={} origins={}", bucket, origins);
            try {
                r2Client.putBucketCors(req -> req
                        .bucket(bucket)
                        .corsConfiguration(CORSConfiguration.builder()
                                .corsRules(CORSRule.builder()
                                        .allowedOrigins(origins)
                                        .allowedMethods("PUT", "GET", "DELETE", "HEAD")
                                        .allowedHeaders("*")
                                        .exposeHeaders("ETag")
                                        .maxAgeSeconds(3600)
                                        .build())
                                .build()));
                log.info("[R2-CORS] CORS configured successfully for bucket={}", bucket);
            } catch (Exception e) {
                log.error("[R2-CORS] Failed to configure CORS: {}", e.getMessage());
            }
        };
    }
}
