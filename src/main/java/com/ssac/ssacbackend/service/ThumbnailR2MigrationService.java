package com.ssac.ssacbackend.service;

import com.ssac.ssacbackend.component.NotionImageMigrator;
import com.ssac.ssacbackend.domain.content.Content;
import com.ssac.ssacbackend.dto.response.ThumbnailMigrationResponse;
import com.ssac.ssacbackend.repository.ContentRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 기존 Cloudinary 썸네일 자산을 Cloudflare R2로 옮기는 1회성 배치 서비스 (Phase 3).
 *
 * <p>{@code content.thumbnailUrl}이 과거 Cloudinary 호스트를 포함하는 행만 대상으로 하며,
 * 원본 URL은 {@code content.thumbnailUrlLegacy}에 최초 1회 백업된다.
 * 개별 항목 실패는 전체를 중단시키지 않고 다음 배치 실행에서 재시도 대상으로 남는다
 * (실패 시 원본 URL이 그대로 유지되어 다시 조회 대상에 포함되기 때문).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThumbnailR2MigrationService {

    private static final String LEGACY_CLOUDINARY_HOST = "res.cloudinary.com";

    private final ContentRepository contentRepository;
    private final NotionImageMigrator notionImageMigrator;

    @Transactional
    public ThumbnailMigrationResponse migrateLegacyThumbnails() {
        List<Content> targets = contentRepository.findAllByThumbnailUrlContaining(LEGACY_CLOUDINARY_HOST);

        int migrated = 0;
        int failed = 0;
        for (Content content : targets) {
            String originalUrl = content.getThumbnailUrl();
            String r2Url = notionImageMigrator.forceMigrateLegacy(originalUrl);

            if (r2Url.equals(originalUrl)) {
                failed++;
                log.warn("썸네일 R2 마이그레이션 실패, 다음 배치 실행에서 재시도: contentId={}", content.getId());
                continue;
            }

            content.backfillThumbnailToR2(r2Url);
            migrated++;
        }

        log.info("썸네일 R2 배치 마이그레이션 완료: 대상={}, 성공={}, 실패={}",
            targets.size(), migrated, failed);
        return new ThumbnailMigrationResponse(targets.size(), migrated, failed, LocalDateTime.now());
    }
}
