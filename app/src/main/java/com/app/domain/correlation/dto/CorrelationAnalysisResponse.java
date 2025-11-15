package com.app.domain.correlation.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 상관관계 분석 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrelationAnalysisResponse {

    /**
     * 분석 세션 ID
     */
    private String sessionId;

    /**
     * 분석 일자
     */
    private LocalDate analysisDate;

    /**
     * 분석 시작일
     */
    private LocalDate analysisStartDate;

    /**
     * 분석 종료일
     */
    private LocalDate analysisEndDate;

    /**
     * 분석 대상 종목 목록
     */
    private List<String> tickers;

    /**
     * 기간별 상관관계 매트릭스
     */
    private PeriodCorrelationMatrix correlationMatrix;

    /**
     * 높은 상관관계 종목 쌍 목록
     */
    private List<HighCorrelationPair> highCorrelationPairs;

    /**
     * 분산투자 가이드라인
     */
    private DiversificationGuide diversificationGuide;

    /**
     * 기간별 상관관계 매트릭스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PeriodCorrelationMatrix {
        private Map<String, Map<String, Double>> threeMonthMatrix;
        private Map<String, Map<String, Double>> sixMonthMatrix;
        private Map<String, Map<String, Double>> oneYearMatrix;
    }

    /**
     * 높은 상관관계 종목 쌍
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HighCorrelationPair {
        private String ticker1;
        private String ticker2;
        private String stockName1;
        private String stockName2;
        private Double correlation3M;
        private Double correlation6M;
        private Double correlation1Y;
        private Double averageCorrelation;
        private String riskLevel; // HIGH, MEDIUM, LOW
    }

    /**
     * 분산투자 가이드라인
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DiversificationGuide {
        private Double overallDiversificationScore; // 0-100점
        private String riskAssessment; // EXCELLENT, GOOD, FAIR, POOR
        private List<String> recommendations;
        private List<String> warnings;
        private Integer highlyCorrelatedPairCount;
        private Double averageCorrelation;
    }


}//class
