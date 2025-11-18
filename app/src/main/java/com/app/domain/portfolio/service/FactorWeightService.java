package com.app.domain.portfolio.service;


import com.app.domain.portfolio.dto.FactorWeightRequest;
import com.app.domain.portfolio.dto.FactorWeightResponse;
import com.app.domain.portfolio.entity.PortfolioSettings;
import com.app.domain.portfolio.mapper.PortfolioSettingsMapper;
import com.app.domain.session.entity.UserSession;
import com.app.domain.session.mapper.SessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

/**
 * 팩터 가중치 설정 서비스
 * 사용자가 두 개의 가중치를 입력하면 나머지 하나를 자동으로 계산
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FactorWeightService {


    private final PortfolioSettingsMapper portfolioSettingsMapper;
    private final SessionMapper userSessionMapper;

    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int SCALE = 4;

    @Transactional
    public FactorWeightResponse setFactorWeights(FactorWeightRequest request) {
        // 1. 입력 검증
        validateRequest(request);

        // 2. 세션 확인 및 생성 (Controller에서 전달받은 세션 ID 사용)
        ensureSessionExists(request.getSessionId(), request.getUserIp(), request.getUserAgent());

        // 3. 나머지 가중치 자동 계산
        FactorWeightRequest calculatedRequest = calculateMissingWeight(request);

        // 4. 기존 설정 조회 또는 신규 생성
        PortfolioSettings settings = getOrCreateSettings(calculatedRequest.getSessionId());

        // 5. 가중치 업데이트
        settings.setRoeWeight(calculatedRequest.getRoeWeight());
        settings.setPbrWeight(calculatedRequest.getPbrWeight());
        settings.setPerWeight(calculatedRequest.getPerWeight());

        // 6. DB 저장
        if (settings.getSettingId() == null) {
            portfolioSettingsMapper.insert(settings);
            log.info("새 포트폴리오 설정 생성 - Session: {}, Setting ID: {}",
                    settings.getSessionId(), settings.getSettingId());
        } else {
            portfolioSettingsMapper.update(settings);
            log.info("포트폴리오 설정 업데이트 - Session: {}, Setting ID: {}",
                    settings.getSessionId(), settings.getSettingId());
        }

        // 7. 응답 생성
        return buildResponse(settings, findAutoCalculatedFactor(request));
    }

    @Transactional
    public FactorWeightResponse getFactorWeights(String sessionId) {
        ensureSessionExists(sessionId, null, null);
        PortfolioSettings settings = getOrCreateSettings(sessionId);
        return buildResponse(settings, null);
    }

    @Transactional
    public FactorWeightResponse resetFactorWeights(String sessionId) {
        ensureSessionExists(sessionId, null, null);
        PortfolioSettings settings = getOrCreateSettings(sessionId);

        settings.setRoeWeight(new BigDecimal("0.3334"));
        settings.setPbrWeight(new BigDecimal("0.3333"));
        settings.setPerWeight(new BigDecimal("0.3333"));

        portfolioSettingsMapper.update(settings);

        FactorWeightResponse response = buildResponse(settings, null);
        response.setMessage("가중치가 기본값으로 초기화되었습니다.");
        return response;
    }

    /**
     * 검증만 수행 (저장하지 않음)
     */
    public void validateWeightsOnly(FactorWeightRequest request) {
        validateRequest(request);
        calculateMissingWeight(request);
    }

    /**
     * 세션 존재 확인 및 생성
     *
     * @param sessionId 비즈니스 세션 ID (Controller에서 생성됨)
     * @param userIp 사용자 IP (선택)
     * @param userAgent User-Agent (선택)
     */
    private void ensureSessionExists(String sessionId, String userIp, String userAgent) {
        UserSession session = userSessionMapper.findBySessionId(sessionId);

        if (session == null) {
            log.info("DB에 세션 {}가 존재하지 않습니다.", sessionId);
        } else {
            // 세션이 이미 있으면 last_accessed 업데이트
            userSessionMapper.updateLastAccessed(sessionId);
            log.debug("기존 세션 사용 - Session: {}", sessionId);
        }
    }

    private void validateRequest(FactorWeightRequest request) {
        if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
            throw new IllegalArgumentException("세션 ID는 필수입니다.");
        }

        int nonNullCount = request.getNonNullWeightCount();
        if (nonNullCount != 2) {
            throw new IllegalArgumentException(
                    String.format("정확히 2개의 가중치를 입력해야 합니다. 입력된 개수: %d", nonNullCount)
            );
        }

        validateWeightRange(request.getRoeWeight(), "ROE");
        validateWeightRange(request.getPbrWeight(), "PBR");
        validateWeightRange(request.getPerWeight(), "PER");

        BigDecimal sum = request.getSumOfNonNullWeights();
        if (sum.compareTo(ONE) > 0) {
            throw new IllegalArgumentException(
                    String.format("입력된 가중치의 합이 100%%를 초과할 수 없습니다. 현재 합: %.2f%%",
                            sum.multiply(new BigDecimal("100")))
            );
        }
    }

    private void validateWeightRange(BigDecimal weight, String factorName) {
        if (weight != null) {
            if (weight.compareTo(ZERO) < 0 || weight.compareTo(ONE) > 0) {
                throw new IllegalArgumentException(
                        String.format("%s 가중치는 0과 1 사이여야 합니다. 입력값: %s", factorName, weight)
                );
            }
        }
    }

    private FactorWeightRequest calculateMissingWeight(FactorWeightRequest request) {
        BigDecimal sum = request.getSumOfNonNullWeights();
        BigDecimal remaining = ONE.subtract(sum).setScale(SCALE, RoundingMode.HALF_UP);

        if (remaining.compareTo(ZERO) < 0) {
            remaining = ZERO;
        }

        if (request.getRoeWeight() == null) {
            request.setRoeWeight(remaining);
        } else if (request.getPbrWeight() == null) {
            request.setPbrWeight(remaining);
        } else if (request.getPerWeight() == null) {
            request.setPerWeight(remaining);
        }

        log.debug("가중치 자동 계산 완료 - ROE: {}, PBR: {}, PER: {}",
                request.getRoeWeight(), request.getPbrWeight(), request.getPerWeight());

        return request;
    }

    private String findAutoCalculatedFactor(FactorWeightRequest request) {
        // 원본 요청에서 어느 것이 null이었는지 추적이 어려우므로
        // 별도 필드로 관리하거나, 여기서는 단순히 반환
        return request.getAutoCalculatedFactor();
    }

    private PortfolioSettings getOrCreateSettings(String sessionId) {
        PortfolioSettings settings = portfolioSettingsMapper.findBySessionId(sessionId);

        if (settings == null) {
            log.info("세션 {}의 설정이 없어 기본값으로 생성합니다.", sessionId);
            settings = PortfolioSettings.createDefault(sessionId);
        }

        return settings;
    }

    private FactorWeightResponse buildResponse(PortfolioSettings settings, String autoCalculatedFactor) {
        BigDecimal roeWeight = settings.getRoeWeight() != null ? settings.getRoeWeight() : new BigDecimal("0.3334");
        BigDecimal pbrWeight = settings.getPbrWeight() != null ? settings.getPbrWeight() : new BigDecimal("0.3333");
        BigDecimal perWeight = settings.getPerWeight() != null ? settings.getPerWeight() : new BigDecimal("0.3333");

        BigDecimal total = roeWeight.add(pbrWeight).add(perWeight).setScale(SCALE, RoundingMode.HALF_UP);

        String updatedAt = settings.getUpdatedAt() != null
                ? settings.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null;

        return FactorWeightResponse.builder()
                .settingId(settings.getSettingId())
                .sessionId(settings.getSessionId())
                .roeWeight(roeWeight)
                .pbrWeight(pbrWeight)
                .perWeight(perWeight)
                .totalWeight(total)
                .autoCalculatedFactor(autoCalculatedFactor)
                .updatedAt(updatedAt)
                .message("가중치 설정이 완료되었습니다.")
                .build();
    }




}//class
