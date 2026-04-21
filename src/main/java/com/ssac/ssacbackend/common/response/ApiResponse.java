package com.ssac.ssacbackend.common.response;

import lombok.Getter;

/**
 * 모든 API 응답을 감싸는 공통 래퍼.
 *
 * <p>성공: {"success": true, "data": {...}, "message": null}
 * <p>실패: {"success": false, "data": null, "message": "한국어 오류 메시지"}
 *
 * <p>변경 기준: docs/conventions.md#api-응답-형식
 */
@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;

    private ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    /**
     * 성공 응답을 생성한다.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * 실패 응답을 생성한다. 메시지는 한국어로 작성해야 한다.
     */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
