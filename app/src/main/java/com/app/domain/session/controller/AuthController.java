package com.app.domain.session.controller;

import com.app.app.global.common.ApiResponse;
import com.app.domain.session.dto.SessionDto;
import com.app.domain.session.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
@Slf4j
@Validated  // 추가
public class AuthController {

    private final SessionService sessionService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createSession(HttpServletRequest request) {
        try {
            String userIp = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");

            SessionDto.CreateRequest createRequest = SessionDto.CreateRequest.builder()
                    .userIp(userIp)
                    .userAgent(userAgent)
                    .build();

            SessionDto.Response response = sessionService.createSession(createRequest);

            return ResponseEntity.ok(
                    ApiResponse.success("세션이 성공적으로 생성되었습니다.", response)
            );

        } catch (Exception e) {
            log.error("세션 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("세션 생성에 실패했습니다."));
        }
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse> getSession(
            @PathVariable @NotBlank(message = "세션 ID는 필수입니다.") String sessionId) {
        try {
            SessionDto.Response response = sessionService.getSessionAndUpdateAccess(sessionId);

            if (response == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("유효하지 않은 세션입니다."));
            }

            return ResponseEntity.ok(
                    ApiResponse.success("세션 정보를 조회했습니다.", response)
            );

        } catch (Exception e) {
            log.error("세션 조회 중 오류 발생: sessionId={}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("세션 조회에 실패했습니다."));
        }
    }

    @GetMapping("/{sessionId}/validate")
    public ResponseEntity<ApiResponse> validateSession(
            @PathVariable @NotBlank(message = "세션 ID는 필수입니다.") String sessionId) {
        try {
            boolean isValid = sessionService.isValidSession(sessionId);

            return ResponseEntity.ok(
                    ApiResponse.success("세션 유효성을 확인했습니다.", isValid)
            );

        } catch (Exception e) {
            log.error("세션 유효성 검증 중 오류 발생: sessionId={}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("세션 유효성 검증에 실패했습니다."));
        }
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse> deactivateSession(
            @PathVariable @NotBlank(message = "세션 ID는 필수입니다.") String sessionId) {
        try {
            boolean result = sessionService.deactivateSession(sessionId);

            if (!result) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("세션을 찾을 수 없습니다."));
            }

            return ResponseEntity.ok(
                    ApiResponse.success("세션이 비활성화되었습니다.")
            );

        } catch (Exception e) {
            log.error("세션 비활성화 중 오류 발생: sessionId={}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("세션 비활성화에 실패했습니다."));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }


}//class
