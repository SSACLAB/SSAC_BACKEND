package com.ssac.ssacbackend.dto.response;

import com.ssac.ssacbackend.domain.quiz.Quiz;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 개인화 추천 응답 DTO.
 *
 * <p>학습 기록이 없는 신규 사용자에게는 기본 추천(DEFAULT)을 반환한다.
 * 기존 사용자에게는 낮은 정답률 퀴즈 재도전(RETRY)과 미시도 퀴즈(UNTRIED)를 반환한다.
 */
public record RecommendationResponse(

    @Schema(description = "개인화 추천 여부 (false: 신규 사용자 기본 추천, true: 학습 기록 기반 추천)")
    boolean personalized,

    @Schema(description = "추천 퀴즈 목록")
    List<RecommendedQuizResponse> recommendations

) {

    /**
     * 추천 유형.
     *
     * <ul>
     *   <li>DEFAULT  - 신규 사용자용 최신 퀴즈 기본 추천</li>
     *   <li>RETRY    - 정답률이 낮아 재도전을 권장하는 퀴즈</li>
     *   <li>UNTRIED  - 아직 한 번도 응시하지 않은 퀴즈</li>
     * </ul>
     */
    public enum RecommendationType {
        DEFAULT,
        RETRY,
        UNTRIED
    }

    /**
     * 추천 퀴즈 항목 DTO.
     */
    public record RecommendedQuizResponse(

        @Schema(description = "퀴즈 ID", example = "1")
        Long quizId,

        @Schema(description = "퀴즈 제목", example = "Spring Boot 기초")
        String title,

        @Schema(description = "퀴즈 설명", example = "Spring Boot 핵심 개념을 묻는 퀴즈")
        String description,

        @Schema(description = "최고 점수", example = "100")
        int maxScore,

        @Schema(description = "전체 문항 수", example = "10")
        int totalQuestions,

        @Schema(description = "추천 유형 (DEFAULT / RETRY / UNTRIED)")
        RecommendationType type,

        @Schema(description = "마지막 응시 정답률 % (RETRY 타입만, 나머지는 null)", example = "45.0")
        Double lastAccuracyRate

    ) {

        public static RecommendedQuizResponse ofDefault(Quiz quiz) {
            return new RecommendedQuizResponse(
                quiz.getId(), quiz.getTitle(), quiz.getDescription(),
                quiz.getMaxScore(), quiz.getTotalQuestions(),
                RecommendationType.DEFAULT, null
            );
        }

        public static RecommendedQuizResponse ofRetry(Quiz quiz, double lastAccuracyRate) {
            return new RecommendedQuizResponse(
                quiz.getId(), quiz.getTitle(), quiz.getDescription(),
                quiz.getMaxScore(), quiz.getTotalQuestions(),
                RecommendationType.RETRY, lastAccuracyRate
            );
        }

        public static RecommendedQuizResponse ofUntried(Quiz quiz) {
            return new RecommendedQuizResponse(
                quiz.getId(), quiz.getTitle(), quiz.getDescription(),
                quiz.getMaxScore(), quiz.getTotalQuestions(),
                RecommendationType.UNTRIED, null
            );
        }
    }
}
