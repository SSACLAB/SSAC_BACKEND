package com.ssac.ssacbackend.component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Notion 이미지 URL을 Cloudflare R2로 이전하는 컴포넌트.
 *
 * <p>Notion 이미지 URL은 1시간 후 만료되므로 동기화 시점에 R2로 복사하여
 * 영구 URL로 교체한다. 이미 R2 URL이거나(재이전 방지) 과거 Cloudinary URL이면
 * (마이그레이션 이전 잔존 데이터) 건너뛴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotionImageMigrator {

    /** 마이그레이션 이전 Cloudinary 자산 식별용 — 재업로드 방지 목적으로만 유지한다. */
    private static final String LEGACY_CLOUDINARY_HOST = "res.cloudinary.com";
    private static final String FOLDER = "content-thumbnails";
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private static final String DEFAULT_EXTENSION = ".bin";

    private final S3Client r2Client;
    private final HttpClient httpClient;

    @Value("${r2.bucket-name:}")
    private String bucketName;

    @Value("${r2.public-base-url:}")
    private String publicBaseUrl;

    /**
     * 이미지 URL을 R2 URL로 교체한다.
     *
     * @param imageUrl 원본 URL (null 허용)
     * @return R2 URL, 또는 마이그레이션 불필요/실패 시 원본 URL
     */
    public String migrateIfNeeded(String imageUrl) {
        if (imageUrl == null || isAlreadyMigrated(imageUrl)) {
            return imageUrl;
        }
        try {
            Downloaded downloaded = download(imageUrl);
            String key = FOLDER + "/" + UUID.randomUUID() + extensionOf(downloaded.contentType());

            r2Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(downloaded.contentType())
                    .build(),
                RequestBody.fromBytes(downloaded.bytes())
            );

            String publicUrl = publicBaseUrl + "/" + key;
            log.debug("이미지 마이그레이션 완료: {} → {}", imageUrl, publicUrl);
            return publicUrl;
        } catch (Exception e) {
            log.warn("이미지 마이그레이션 실패, 원본 URL 유지: {}", imageUrl, e);
            return imageUrl;
        }
    }

    private boolean isAlreadyMigrated(String imageUrl) {
        return imageUrl.contains(LEGACY_CLOUDINARY_HOST)
            || (!publicBaseUrl.isBlank() && imageUrl.startsWith(publicBaseUrl));
    }

    private Downloaded download(String imageUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(imageUrl)).GET().build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            throw new IOException("이미지 다운로드 실패: status=" + response.statusCode());
        }
        String contentType = response.headers().firstValue("Content-Type").orElse(DEFAULT_CONTENT_TYPE);
        return new Downloaded(response.body(), contentType);
    }

    /** Content-Type 기준으로 확장자를 결정한다. 알 수 없는 타입은 확장자 없이 저장한다. */
    private String extensionOf(String contentType) {
        if (contentType == null) {
            return DEFAULT_EXTENSION;
        }
        int semicolon = contentType.indexOf(';');
        String mimeType = (semicolon >= 0 ? contentType.substring(0, semicolon) : contentType).trim();
        return switch (mimeType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/svg+xml" -> ".svg";
            default -> DEFAULT_EXTENSION;
        };
    }

    private record Downloaded(byte[] bytes, String contentType) {
    }
}
