package com.ssac.ssacbackend.controller;

import com.ssac.ssacbackend.common.response.ApiResponse;
import com.ssac.ssacbackend.dto.response.RecommendationResponse;
import com.ssac.ssacbackend.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 개인화 콘텐츠 추천 엔드포인트.
 *
 * <p>로그인 회원만 접근 가능하다. 비회원(GUEST)은 403으로 차단된다.
 */
@Slf4j
@Tag(name = "Recommendation", description = "개인화 퀴즈 추천 API")
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(
        summary = "개인화 퀴즈 추천 조회",
        description = """
            [호출 화면] 홈 화면 또는 마이페이지 > 추천 퀴즈
            [권한 조건] 로그인 필수 (JWT Bearer 토큰, GUEST 불가)
            [특이 동작]
            - 학습 기록이 없는 신규 사용자: 최신 퀴즈 5개를 기본 추천 (personalized=false)
            - 기존 사용자: 최근 30일 활동 기준으로
                * 정답률 70% 미만 퀴즈 최대 3개 (RETRY)
                * 아직 응시하지 않은 최신 퀴즈 최대 3개 (UNTRIED)
              를 혼합 추천 (personalized=true)
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "추천 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "인증 토큰 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "비회원(GUEST) 접근 불가"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendations(
        Authentication authentication) {
        log.debug("추천 조회 요청: email={}", authentication.getName());
        RecommendationResponse result =
            recommendationService.getRecommendations(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
