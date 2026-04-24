package com.ssac.ssacbackend.dto.response;

import com.ssac.ssacbackend.domain.user.User;
import com.ssac.ssacbackend.domain.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 관리자 사용자 목록 조회용 응답 DTO.
 */
public record UserSummaryResponse(

    @Schema(description = "사용자 ID", example = "1")
    Long id,

    @Schema(description = "이메일", example = "user@example.com")
    String email,

    @Schema(description = "닉네임", example = "닉네임123")
    String nickname,

    @Schema(description = "권한", example = "USER")
    UserRole role,

    @Schema(description = "가입일시")
    LocalDateTime createdAt

) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getRole(),
            user.getCreatedAt()
        );
    }
}
