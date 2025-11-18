package com.app.domain.correlation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 분산 최적화 결과 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiversificationResponse {

    /**
     * 세션 ID
     */
    private String sessionId;

    /**
     * 분석 일시
     */
    private LocalDateTime analysisDateTime;

    /**
     * 전체 종목 분산 점수 목록
     */
    private List<DiversificationScore> allScores;

    /**
     * 선택된 최적 종목 목록
     */
    private List<DiversificationScore> selectedStocks;

    /**
     * 제외된 종목 목록 (높은 상관관계로 인해)
     */
    private List<DiversificationScore> excludedStocks;

    /**
     * 포트폴리오 전체 평균 상관계수
     */
    private Double portfolioAvgCorrelation;

    /**
     * 포트폴리오 분산 점수 (0~100)
     */
    private Double portfolioDiversificationScore;

    /**
     * 상관관계 매트릭스 (선택된 종목 기준)
     */
    private Map<String, Map<String, Double>> correlationMatrix;

    /**
     * 분산 최적화 요약
     */
    private OptimizationSummary summary;

    /**
     * 최적화 요약 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizationSummary {
        /**
         * 입력 종목 수
         */
        private Integer inputStockCount;

        /**
         * 출력 종목 수
         */
        private Integer outputStockCount;

        /**
         * 제거된 종목 수
         */
        private Integer removedStockCount;

        /**
         * 높은 상관관계 임계값
         */
        private Double highCorrelationThreshold;

        /**
         * 분석 기간
         */
        private String analysisPeriod;

        /**
         * 최적화 알고리즘
         */
        private String optimizationAlgorithm;
    }


}//class
