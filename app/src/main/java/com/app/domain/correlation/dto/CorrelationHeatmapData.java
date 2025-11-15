package com.app.domain.correlation.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 히트맵 시각화를 위한 상관관계 데이터 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrelationHeatmapData {


    /**
     * 종목 레이블 목록 (x축, y축 공통)
     */
    private List<String> labels;

    /**
     * 기간별 히트맵 데이터
     */
    private List<HeatmapPeriodData> periodData;

    /**
     * 색상 범위 설정
     */
    private ColorScale colorScale;

    /**
     * 기간별 히트맵 데이터
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HeatmapPeriodData {
        private String period; // "3M", "6M", "1Y"
        private String periodName; // "3개월", "6개월", "1년"
        private List<List<Double>> matrix; // 2차원 상관계수 매트릭스
        private Double minValue; // 최소 상관계수
        private Double maxValue; // 최대 상관계수
        private Double avgValue; // 평균 상관계수
    }

    /**
     * 색상 스케일 설정
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ColorScale {
        private String lowColor = "#0571b0";     // 낮은 상관관계 (파란색)
        private String midColor = "#f7f7f7";     // 중간 상관관계 (회색)
        private String highColor = "#ca0020";    // 높은 상관관계 (빨간색)
        private Double lowThreshold = -0.5;      // 낮은 상관관계 임계값
        private Double highThreshold = 0.7;      // 높은 상관관계 임계값
    }

    /**
     * 히트맵 셀 정보 (툴팁용)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HeatmapCell {
        private String ticker1;
        private String ticker2;
        private String stockName1;
        private String stockName2;
        private Double correlation;
        private String riskLevel;
        private String description;
    }



}//class
