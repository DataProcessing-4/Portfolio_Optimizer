package com.app.domain.portfolio.controller;


import com.app.domain.portfolio.dto.FactorWeightRequest;
import com.app.domain.portfolio.dto.FactorWeightResponse;
import com.app.domain.portfolio.service.FactorWeightService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 팩터 가중치 설정 컨트롤러
 *
 * 주요 기능:
 * 1. 사용자가 두 개의 가중치를 입력하면 나머지 하나를 자동 계산
 * 2. ROE + PBR + PER = 100%
 * 3. 예시: ROE=40, PBR=35 입력 → PER=25 자동 계산
 */
@Slf4j
@RestController
@RequestMapping("/api/portfolio/factor-weights")
@RequiredArgsConstructor
public class PortfolioController {


    private final FactorWeightService factorWeightService;

    private static final String SESSION_KEY = "BUSINESS_SESSION_ID";

    /**
     * 팩터 가중치 설정
     */
    @PostMapping
    public ResponseEntity<FactorWeightResponse> setFactorWeights(
            @RequestBody FactorWeightRequest request,
            HttpSession httpSession,
            HttpServletRequest httpRequest) {

        try {
            // 1. 비즈니스 세션 ID 가져오기 (없으면 생성)
            String businessSessionId = getOrCreateBusinessSessionId(httpSession, httpRequest);
            request.setSessionId(businessSessionId);

            log.info("팩터 가중치 설정 요청 - HTTP Session: {}, Business Session: {}, ROE: {}, PBR: {}, PER: {}",
                    httpSession.getId(), businessSessionId,
                    request.getRoeWeight(), request.getPbrWeight(), request.getPerWeight());

            FactorWeightResponse response = factorWeightService.setFactorWeights(request);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("가중치 설정 검증 오류: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(FactorWeightResponse.builder()
                            .message("오류: " + e.getMessage())
                            .build());

        } catch (Exception e) {
            log.error("가중치 설정 중 오류 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FactorWeightResponse.builder()
                            .message("가중치 설정 중 오류가 발생했습니다.")
                            .build());
        }
    }

    /**
     * 현재 설정된 가중치 조회
     */
    @GetMapping
    public ResponseEntity<FactorWeightResponse> getFactorWeights(
            HttpSession httpSession,
            HttpServletRequest httpRequest) {

        try {
            String businessSessionId = getOrCreateBusinessSessionId(httpSession, httpRequest);

            log.info("팩터 가중치 조회 요청 - HTTP Session: {}, Business Session: {}",
                    httpSession.getId(), businessSessionId);

            FactorWeightResponse response = factorWeightService.getFactorWeights(businessSessionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("가중치 조회 중 오류 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FactorWeightResponse.builder()
                            .message("가중치 조회 중 오류가 발생했습니다.")
                            .build());
        }
    }

    /**
     * 가중치 초기화 (기본값으로 리셋)
     */
    @PostMapping("/reset")
    public ResponseEntity<FactorWeightResponse> resetFactorWeights(
            HttpSession httpSession,
            HttpServletRequest httpRequest) {

        try {
            String businessSessionId = getOrCreateBusinessSessionId(httpSession, httpRequest);

            log.info("팩터 가중치 초기화 요청 - HTTP Session: {}, Business Session: {}",
                    httpSession.getId(), businessSessionId);

            FactorWeightResponse response = factorWeightService.resetFactorWeights(businessSessionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("가중치 초기화 중 오류 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FactorWeightResponse.builder()
                            .message("가중치 초기화 중 오류가 발생했습니다.")
                            .build());
        }
    }

    /**
     * 가중치 유효성 검증 API (프론트엔드 실시간 검증용)
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateWeights(
            @RequestBody FactorWeightRequest request,
            HttpSession httpSession,
            HttpServletRequest httpRequest) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 검증용 임시 세션 ID 설정
            String businessSessionId = getOrCreateBusinessSessionId(httpSession, httpRequest);
            request.setSessionId(businessSessionId);

            // 서비스의 검증 로직 호출 (실제 저장은 하지 않음)
            factorWeightService.validateWeightsOnly(request);

            response.put("valid", true);
            response.put("message", "유효한 가중치입니다.");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("valid", false);
            response.put("message", e.getMessage());

            return ResponseEntity.ok(response);
        }
    }

    /**
     * 세션 정보 조회 API (디버깅/모니터링용)
     */
    @GetMapping("/session-info")
    public ResponseEntity<Map<String, Object>> getSessionInfo(
            HttpSession httpSession,
            HttpServletRequest httpRequest) {

        Map<String, Object> info = new HashMap<>();

        String businessSessionId = getOrCreateBusinessSessionId(httpSession, httpRequest);

        info.put("httpSessionId", httpSession.getId());
        info.put("businessSessionId", businessSessionId);
        info.put("httpSessionCreationTime", httpSession.getCreationTime());
        info.put("httpSessionLastAccessedTime", httpSession.getLastAccessedTime());
        info.put("httpSessionMaxInactiveInterval", httpSession.getMaxInactiveInterval());
        info.put("userIp", getClientIp(httpRequest));
        info.put("userAgent", httpRequest.getHeader("User-Agent"));

        return ResponseEntity.ok(info);
    }

    /**
     * 비즈니스 세션 ID 가져오기 또는 생성
     *
     * 정책:
     * 1. HTTP Session에서 비즈니스 세션 ID 조회
     * 2. 없으면 새로 생성하고 HTTP Session에 저장
     * 3. 이후 같은 HTTP Session에서는 동일한 비즈니스 세션 ID 사용
     */
    private String getOrCreateBusinessSessionId(HttpSession httpSession, HttpServletRequest httpRequest) {
        // HTTP Session에서 비즈니스 세션 ID 조회
        String businessSessionId = (String) httpSession.getAttribute(SESSION_KEY);

        if (businessSessionId == null) {
            // 비즈니스 세션 ID 생성
            businessSessionId = "SES_" + System.currentTimeMillis() + "_" + httpSession.getId().substring(0, 8);

            // HTTP Session에 저장 (이후 요청에서 재사용)
            httpSession.setAttribute(SESSION_KEY, businessSessionId);

            // 추가 정보 저장
            httpSession.setAttribute("USER_IP", getClientIp(httpRequest));
            httpSession.setAttribute("USER_AGENT", httpRequest.getHeader("User-Agent"));

            log.info("새로운 비즈니스 세션 생성 - HTTP Session: {}, Business Session: {}, IP: {}",
                    httpSession.getId(),
                    businessSessionId,
                    getClientIp(httpRequest));
        }

        return businessSessionId;
    }

    /**
     * 클라이언트 실제 IP 가져오기
     * (Proxy, Load Balancer 고려)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 여러 IP가 있을 경우 첫 번째 IP 사용
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }



}//class
