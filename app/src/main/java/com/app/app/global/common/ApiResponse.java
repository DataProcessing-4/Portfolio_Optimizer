package com.app.app.global.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 통일된 API 응답 형식을 위한 공통 클래스
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    private boolean success;
    private String message;
    private Object data;  // T 대신 Object 사용
    private LocalDateTime timestamp;
    private String errorCode;


    // 성공 응답 생성 (메시지와 데이터 포함)
    public static ApiResponse success(String message, Object data) {
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 성공 응답 생성 (데이터만)
    public static ApiResponse success(Object data) {
        return success("요청이 성공적으로 처리되었습니다.", data);
    }

    // 성공 응답 생성 (메시지만)
    public static ApiResponse success(String message) {
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 실패 응답 생성 (에러 메시지만)
    public static ApiResponse error(String message) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 실패 응답 생성 (에러 메시지와 에러 코드)
    public static ApiResponse error(String message, String errorCode) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 성공 여부 확인
    public boolean isSuccess() {
        return this.success;
    }

    // 실패 여부 확인
    public boolean isError() {
        return !this.success;
    }


}//class
