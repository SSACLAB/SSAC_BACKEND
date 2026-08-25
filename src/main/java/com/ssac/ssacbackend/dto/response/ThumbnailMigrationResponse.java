package com.ssac.ssacbackend.dto.response;

import java.time.LocalDateTime;

/**
 * Cloudinary → R2 썸네일 배치 마이그레이션 결과 응답 DTO.
 */
public record ThumbnailMigrationResponse(
    int targetCount,
    int migratedCount,
    int failedCount,
    LocalDateTime migratedAt
) {}
