package com.ssac.ssacbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 닉네임 수정 요청 DTO.
 */
public record UpdateNicknameRequest(

    @Schema(description = "변경할 닉네임", example = "새닉네임123")
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    @Pattern(
        regexp = "^[a-zA-Z0-9가-힣_-]+$",
        message = "닉네임은 한글, 영문, 숫자, 언더스코어(_), 하이픈(-)만 사용 가능합니다."
    )
    String nickname

) {
}
