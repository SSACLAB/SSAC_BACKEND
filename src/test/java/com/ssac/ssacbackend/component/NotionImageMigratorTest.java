package com.ssac.ssacbackend.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

/**
 * NotionImageMigrator 단위 테스트.
 *
 * <p>R2(S3 호환) 클라이언트와 이미지 다운로드용 HttpClient를 Mock으로 대체하여 각 분기를 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class NotionImageMigratorTest {

    private static final String BUCKET_NAME = "ssac-content-images";
    private static final String PUBLIC_BASE_URL = "https://img.ssac.io";

    @Mock
    private S3Client r2Client;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<byte[]> httpResponse;

    @InjectMocks
    private NotionImageMigrator migrator;

    private void setBucketConfig() {
        ReflectionTestUtils.setField(migrator, "bucketName", BUCKET_NAME);
        ReflectionTestUtils.setField(migrator, "publicBaseUrl", PUBLIC_BASE_URL);
    }

    @Nested
    @DisplayName("스킵 케이스 — R2 호출 없음")
    class SkipCases {

        @Test
        @DisplayName("null 입력 시 null 반환")
        void null_입력_시_null_반환() throws Exception {
            assertThat(migrator.migrateIfNeeded(null)).isNull();
            verify(r2Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("이미 R2 URL이면 그대로 반환")
        void 이미_R2_URL이면_그대로_반환() throws Exception {
            setBucketConfig();
            String r2Url = PUBLIC_BASE_URL + "/content-thumbnails/sample.jpg";
            assertThat(migrator.migrateIfNeeded(r2Url)).isEqualTo(r2Url);
            verify(httpClient, never()).send(any(), any());
        }

        @Test
        @DisplayName("과거 Cloudinary URL이면 그대로 반환 (재업로드 방지)")
        void 과거_Cloudinary_URL이면_그대로_반환() throws Exception {
            setBucketConfig();
            String cloudinaryUrl = "https://res.cloudinary.com/demo/image/upload/sample.jpg";
            assertThat(migrator.migrateIfNeeded(cloudinaryUrl)).isEqualTo(cloudinaryUrl);
            verify(httpClient, never()).send(any(), any());
        }
    }

    @Nested
    @DisplayName("R2 업로드")
    class UploadCases {

        @Test
        @DisplayName("업로드 성공 시 R2 공개 URL 반환")
        void 업로드_성공_시_R2_공개_URL_반환() throws Exception {
            setBucketConfig();
            String originalUrl = "https://prod-files-secure.s3.amazonaws.com/image.png";

            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(new byte[] {1, 2, 3});
            when(httpResponse.headers()).thenReturn(
                HttpHeaders.of(Map.of("Content-Type", List.of("image/png")), (a, b) -> true));
            when(httpClient.<byte[]>send(any(), any())).thenReturn(httpResponse);
            when(r2Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn((PutObjectResponse) PutObjectResponse.builder().build());

            String result = migrator.migrateIfNeeded(originalUrl);

            assertThat(result).startsWith(PUBLIC_BASE_URL + "/content-thumbnails/").endsWith(".png");
        }

        @Test
        @DisplayName("다운로드 실패(비정상 상태 코드) 시 원본 URL 반환")
        void 다운로드_실패_시_원본_URL_반환() throws Exception {
            setBucketConfig();
            String originalUrl = "https://unsplash.com/ko/사진/test-id";

            when(httpResponse.statusCode()).thenReturn(404);
            when(httpClient.<byte[]>send(any(), any())).thenReturn(httpResponse);

            assertThat(migrator.migrateIfNeeded(originalUrl)).isEqualTo(originalUrl);
            verify(r2Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("다운로드 중 예외 발생 시 원본 URL 반환 (예외 삼킴)")
        void 다운로드_예외_시_원본_URL_반환() throws Exception {
            setBucketConfig();
            String originalUrl = "https://unsplash.com/ko/사진/test-id";

            when(httpClient.send(any(), any())).thenThrow(new IOException("connection reset"));

            assertThat(migrator.migrateIfNeeded(originalUrl)).isEqualTo(originalUrl);
        }
    }
}
