package com.ssac.ssacbackend.dto.request;

import com.ssac.ssacbackend.domain.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 사용자 권한 변경 요청 DTO.
 */
public record UpdateRoleRequest(

    @NotNull(message = "변경할 권한을 지정해야 합니다.")
    @Schema(description = "변경할 권한 (USER 또는 ADMIN)", example = "ADMIN")
    UserRole role

) {}
