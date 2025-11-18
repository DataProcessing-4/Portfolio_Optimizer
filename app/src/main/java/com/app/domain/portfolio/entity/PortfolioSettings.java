package com.app.domain.portfolio.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 포트폴리오 설정 Entity
 * portfolio_settings 테이블과 매핑
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSettings {

    /**
     * 설정 ID
     */
    private Long settingId;

    /**
     * 세션 ID
     */
    private String sessionId;

    /**
     * ROE 가중치 (기본값: 0.3334)
     */
    private BigDecimal roeWeight;

    /**
     * PBR 가중치 (기본값: 0.3333)
     */
    private BigDecimal pbrWeight;

    /**
     * PER 가중치 (기본값: 0.3333)
     */
    private BigDecimal perWeight;

    /**
     * 상위 N개 추천수 (1-10, 기본값: 5)
     */
    private Integer topNCount;

    /**
     * 최소 상관관계 임계값 (기본값: 0.7)
     */
    private BigDecimal minCorrelationThreshold;

    /**
     * 최대 부채비율 (기본값: 2.0)
     */
    private BigDecimal maxDebtRatio;

    /**
     * 무위험 수익률 (기본값: 0.03)
     */
    private BigDecimal riskFreeRate;

    /**
     * 생성일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    private LocalDateTime updatedAt;

    /**
     * 기본 설정값으로 초기화
     */
    public static PortfolioSettings createDefault(String sessionId) {
        return PortfolioSettings.builder()
                .sessionId(sessionId)
                .roeWeight(new BigDecimal("0.3334"))
                .pbrWeight(new BigDecimal("0.3333"))
                .perWeight(new BigDecimal("0.3333"))
                .topNCount(5)
                .minCorrelationThreshold(new BigDecimal("0.7"))
                .maxDebtRatio(new BigDecimal("2.0"))
                .riskFreeRate(new BigDecimal("0.03"))
                .build();
    }





}//class
