package com.app.domain.correlation.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 분산 최적화 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiversificationRequest {

    /**
     * 세션 ID
     */
    private String sessionId;

    /**
     * 분석 대상 티커 목록 (5-10개)
     */
    private List<String> tickers;

    /**
     * 높은 상관관계 임계값 (기본값: 0.7)
     */
    @Builder.Default
    private Double highCorrelationThreshold = 0.7;

    /**
     * 최종 선택할 종목 개수 (기본값: 5)
     */
    @Builder.Default
    private Integer targetStockCount = 5;

    /**
     * 분석 기간 (3M, 6M, 1Y)
     */
    @Builder.Default
    private String analysisPeriod = "1Y";









}//class
