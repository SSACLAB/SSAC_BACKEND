package com.ssac.ssacbackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ssac.ssacbackend.component.NotionImageMigrator;
import com.ssac.ssacbackend.domain.content.Content;
import com.ssac.ssacbackend.dto.response.ThumbnailMigrationResponse;
import com.ssac.ssacbackend.repository.ContentRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ThumbnailR2MigrationService 단위 테스트.
 *
 * <p>Content는 도메인 메서드로만 상태를 변경하는 컨벤션이라 Mock으로 대체하고,
 * backfillThumbnailToR2 호출 여부로 마이그레이션 성공/실패를 검증한다.
 */
class ThumbnailR2MigrationServiceTest {

    private static final String LEGACY_HOST = "res.cloudinary.com";

    private ContentRepository contentRepository;
    private NotionImageMigrator notionImageMigrator;
    private ThumbnailR2MigrationService service;

    @BeforeEach
    void setUp() {
        contentRepository = mock(ContentRepository.class);
        notionImageMigrator = mock(NotionImageMigrator.class);
        service = new ThumbnailR2MigrationService(contentRepository, notionImageMigrator);
    }

    @Test
    @DisplayName("대상이 없으면 0건으로 즉시 반환한다")
    void 대상_없으면_0건_반환() {
        given(contentRepository.findAllByThumbnailUrlContaining(LEGACY_HOST)).willReturn(List.of());

        ThumbnailMigrationResponse response = service.migrateLegacyThumbnails();

        assertThat(response.targetCount()).isZero();
        assertThat(response.migratedCount()).isZero();
        assertThat(response.failedCount()).isZero();
    }

    @Test
    @DisplayName("마이그레이션 성공 시 backfillThumbnailToR2를 호출하고 성공 건수를 집계한다")
    void 마이그레이션_성공_시_backfill_호출() {
        Content content = mock(Content.class);
        String originalUrl = "https://res.cloudinary.com/demo/image/upload/sample.png";
        String r2Url = "https://img.ssac.io/content-thumbnails/sample.png";
        given(content.getId()).willReturn(1L);
        given(content.getThumbnailUrl()).willReturn(originalUrl);
        given(contentRepository.findAllByThumbnailUrlContaining(LEGACY_HOST))
            .willReturn(List.of(content));
        given(notionImageMigrator.forceMigrateLegacy(originalUrl)).willReturn(r2Url);

        ThumbnailMigrationResponse response = service.migrateLegacyThumbnails();

        assertThat(response.targetCount()).isEqualTo(1);
        assertThat(response.migratedCount()).isEqualTo(1);
        assertThat(response.failedCount()).isZero();
        verify(content).backfillThumbnailToR2(eq(r2Url));
    }

    @Test
    @DisplayName("마이그레이션 실패(원본 URL 그대로 반환) 시 backfillThumbnailToR2를 호출하지 않고 실패 건수로 집계한다")
    void 마이그레이션_실패_시_backfill_미호출() {
        Content content = mock(Content.class);
        String originalUrl = "https://res.cloudinary.com/demo/image/upload/broken.png";
        given(content.getId()).willReturn(2L);
        given(content.getThumbnailUrl()).willReturn(originalUrl);
        given(contentRepository.findAllByThumbnailUrlContaining(LEGACY_HOST))
            .willReturn(List.of(content));
        // 실패 시 NotionImageMigrator는 원본 URL을 그대로 반환한다 (폴백 정책).
        given(notionImageMigrator.forceMigrateLegacy(originalUrl)).willReturn(originalUrl);

        ThumbnailMigrationResponse response = service.migrateLegacyThumbnails();

        assertThat(response.targetCount()).isEqualTo(1);
        assertThat(response.migratedCount()).isZero();
        assertThat(response.failedCount()).isEqualTo(1);
        verify(content, never()).backfillThumbnailToR2(anyString());
    }

    @Test
    @DisplayName("여러 건 중 일부만 실패해도 나머지는 계속 처리한다")
    void 일부_실패해도_나머지_계속_처리() {
        Content success = mock(Content.class);
        Content failure = mock(Content.class);
        String successUrl = "https://res.cloudinary.com/demo/image/upload/ok.png";
        String failureUrl = "https://res.cloudinary.com/demo/image/upload/ng.png";
        String r2Url = "https://img.ssac.io/content-thumbnails/ok.png";

        given(success.getId()).willReturn(10L);
        given(success.getThumbnailUrl()).willReturn(successUrl);
        given(failure.getId()).willReturn(11L);
        given(failure.getThumbnailUrl()).willReturn(failureUrl);
        given(contentRepository.findAllByThumbnailUrlContaining(LEGACY_HOST))
            .willReturn(List.of(success, failure));
        given(notionImageMigrator.forceMigrateLegacy(successUrl)).willReturn(r2Url);
        given(notionImageMigrator.forceMigrateLegacy(failureUrl)).willReturn(failureUrl);

        ThumbnailMigrationResponse response = service.migrateLegacyThumbnails();

        assertThat(response.targetCount()).isEqualTo(2);
        assertThat(response.migratedCount()).isEqualTo(1);
        assertThat(response.failedCount()).isEqualTo(1);
        verify(success).backfillThumbnailToR2(eq(r2Url));
        verify(failure, never()).backfillThumbnailToR2(anyString());
    }
}
