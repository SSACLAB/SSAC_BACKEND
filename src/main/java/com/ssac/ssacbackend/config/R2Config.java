package com.ssac.ssacbackend.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Cloudflare R2 SDK 설정.
 *
 * <p>R2는 S3 호환 API를 제공하므로 AWS SDK v2의 S3Client를 그대로 사용하되,
 * endpoint만 계정별 R2 엔드포인트로 재정의한다. AWS 계정/과금과는 무관하다.
 */
@Configuration
public class R2Config {

    @Value("${r2.account-id:}")
    private String accountId;

    @Value("${r2.access-key-id:}")
    private String accessKeyId;

    @Value("${r2.secret-access-key:}")
    private String secretAccessKey;

    @Bean
    public S3Client r2Client() {
        return S3Client.builder()
            .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
            // R2는 리전 개념이 없어 SDK 호환을 위해 고정값 "auto"를 사용한다.
            .region(Region.of("auto"))
            .credentialsProvider(credentialsProvider())
            // R2는 버추얼호스트 스타일(<bucket>.<endpoint>)을 지원하지 않아 경로 스타일 필수.
            .forcePathStyle(true)
            .build();
    }

    /**
     * 자격증명 미설정 환경(로컬/테스트)에서도 Bean 생성이 실패하지 않도록 한다.
     * 실제 R2 호출은 이 경우 인증 오류로 실패하며, 이는 마이그레이션 실패 시 원본 URL을
     * 반환하는 {@code NotionImageMigrator}의 폴백 정책으로 흡수된다.
     */
    private AwsCredentialsProvider credentialsProvider() {
        if (accessKeyId == null || accessKeyId.isBlank()
            || secretAccessKey == null || secretAccessKey.isBlank()) {
            return AnonymousCredentialsProvider.create();
        }
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
    }
}
