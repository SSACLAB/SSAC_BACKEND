package com.ssac.ssacbackend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ssac.ssacbackend.dto.response.ContentMonitoringListResponse;
import com.ssac.ssacbackend.dto.response.ContentSyncResponse;
import com.ssac.ssacbackend.dto.response.ThumbnailMigrationResponse;
import com.ssac.ssacbackend.service.NotionSyncService;
import com.ssac.ssacbackend.service.ThumbnailR2MigrationService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class AdminContentControllerTest {

    private NotionSyncService notionSyncService;
    private ThumbnailR2MigrationService thumbnailR2MigrationService;
    private AdminContentController controller;

    @BeforeEach
    void setUp() {
        notionSyncService = mock(NotionSyncService.class);
        thumbnailR2MigrationService = mock(ThumbnailR2MigrationService.class);
        controller = new AdminContentController(notionSyncService, thumbnailR2MigrationService);
    }

    // ── getMonitoring ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("콘텐츠 모니터링 목록 조회 성공 시 200을 반환한다")
    void 콘텐츠_모니터링_목록_조회_성공() {
        ContentMonitoringListResponse mockResponse =
            new ContentMonitoringListResponse(10L, 8L, LocalDateTime.now(), List.of());
        given(notionSyncService.getMonitoring(1, 20)).willReturn(mockResponse);

        ResponseEntity<?> response = controller.getMonitoring(1, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notionSyncService).getMonitoring(1, 20);
    }

    @Test
    @DisplayName("페이지/사이즈 파라미터가 전달되면 그대로 서비스에 위임한다")
    void 페이지_사이즈_파라미터_서비스_위임() {
        ContentMonitoringListResponse mockResponse =
            new ContentMonitoringListResponse(0L, 0L, LocalDateTime.now(), List.of());
        given(notionSyncService.getMonitoring(2, 10)).willReturn(mockResponse);

        ResponseEntity<?> response = controller.getMonitoring(2, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notionSyncService).getMonitoring(2, 10);
    }

    // ── sync ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Notion 수동 동기화 성공 시 200과 동기화 결과를 반환한다")
    void Notion_수동_동기화_성공() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "admin@test.com", null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        ContentSyncResponse mockResponse =
            new ContentSyncResponse(5, 2, 3, LocalDateTime.now());
        given(notionSyncService.syncAll()).willReturn(mockResponse);

        ResponseEntity<?> response = controller.sync(auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notionSyncService).syncAll();
    }

    // ── migrateLegacyThumbnails ─────────────────────────────────────────────────

    @Test
    @DisplayName("썸네일 R2 배치 마이그레이션 성공 시 200과 결과를 반환한다")
    void 썸네일_R2_배치_마이그레이션_성공() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "admin@test.com", null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        ThumbnailMigrationResponse mockResponse =
            new ThumbnailMigrationResponse(3, 2, 1, LocalDateTime.now());
        given(thumbnailR2MigrationService.migrateLegacyThumbnails()).willReturn(mockResponse);

        ResponseEntity<?> response = controller.migrateLegacyThumbnails(auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(thumbnailR2MigrationService).migrateLegacyThumbnails();
    }
}
