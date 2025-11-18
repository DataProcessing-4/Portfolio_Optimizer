package com.app.domain.correlation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 상관관계 분석 결과 엔티티
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrelationAnalysis {



    /**
     * 상관관계 ID (Primary Key)
     */
    private Long correlationId;

    /**
     * 세션 ID
     */
    private String sessionId;

    /**
     * 첫 번째 종목 티커
     */
    private String ticker1;

    /**
     * 두 번째 종목 티커
     */
    private String ticker2;

    /**
     * 3개월 상관계수
     */
    private Double correlation3m;

    /**
     * 6개월 상관계수
     */
    private Double correlation6m;

    /**
     * 1년 상관계수
     */
    private Double correlation1y;

    /**
     * 분석 시작일
     */
    private LocalDate analysisStartDate;

    /**
     * 분석 종료일
     */
    private LocalDate analysisEndDate;

    /**
     * 분석 수행일
     */
    private LocalDate analysisDate;

    /**
     * 생성일시
     */
    private LocalDateTime createdAt;

    // 조인용 필드들 (stock 테이블에서 가져올 정보)
    private String stockName1;
    private String stockName2;
    private String industry1;
    private String industry2;

    /**
     * 평균 상관계수 계산
     */
    public Double getAverageCorrelation() {
        int count = 0;
        double sum = 0.0;

        if (correlation3m != null) {
            sum += correlation3m;
            count++;
        }
        if (correlation6m != null) {
            sum += correlation6m;
            count++;
        }
        if (correlation1y != null) {
            sum += correlation1y;
            count++;
        }

        return count > 0 ? sum / count : null;
    }

    /**
     * 높은 상관관계 여부 확인
     */
    public boolean isHighCorrelation(double threshold) {
        Double avg = getAverageCorrelation();
        return avg != null && Math.abs(avg) >= threshold;
    }

    /**
     * 위험도 평가
     */
    public String getRiskLevel(double threshold) {
        Double avg = getAverageCorrelation();
        if (avg == null) return "UNKNOWN";

        double absCorr = Math.abs(avg);
        if (absCorr >= threshold) {
            return "HIGH";
        } else if (absCorr >= threshold * 0.7) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    //------------[분산 최적화 로직 기능] 25.11.18 추가 메서드 -------------
    /**
     * 선택된 기간의 상관계수 반환
     * @param period 기간 (3M, 6M, 1Y)
     * @return 상관계수
     */
    public Double getCorrelationByPeriod(String period) {
        if (period == null) {
            return correlation1y != null ? correlation1y.doubleValue() : null;
        }

        switch (period.toUpperCase()) {
            case "3M":
                return correlation3m != null ? correlation3m.doubleValue() : null;
            case "6M":
                return correlation6m != null ? correlation6m.doubleValue() : null;
            case "1Y":
            default:
                return correlation1y != null ? correlation1y.doubleValue() : null;
        }
    }


    /**
     * 절댓값 기준 상관계수 반환
     * @param period 기간 (3M, 6M, 1Y)
     * @return 상관계수 절댓값
     */
    public Double getAbsCorrelationByPeriod(String period) {
        Double correlation = getCorrelationByPeriod(period);
        return correlation != null ? Math.abs(correlation) : null;
    }

    /**
     * 상관관계 강도 평가
     * @param period 기간
     * @return 강도 (VERY_HIGH, HIGH, MEDIUM, LOW, VERY_LOW)
     */
    public CorrelationStrength getCorrelationStrength(String period) {
        Double absCorr = getAbsCorrelationByPeriod(period);
        if (absCorr == null) return CorrelationStrength.UNKNOWN;

        if (absCorr >= 0.8) return CorrelationStrength.VERY_HIGH;
        if (absCorr >= 0.6) return CorrelationStrength.HIGH;
        if (absCorr >= 0.4) return CorrelationStrength.MEDIUM;
        if (absCorr >= 0.2) return CorrelationStrength.LOW;
        return CorrelationStrength.VERY_LOW;
    }

    /**
     * 상관관계 방향 (양/음)
     * @param period 기간
     * @return true: 양의 상관관계, false: 음의 상관관계
     */
    public boolean isPositiveCorrelation(String period) {
        Double correlation = getCorrelationByPeriod(period);
        return correlation != null && correlation >= 0;
    }

    /**
     * 상관관계 강도 열거형
     */
    public enum CorrelationStrength {
        VERY_HIGH("매우 높음", 0.8),
        HIGH("높음", 0.6),
        MEDIUM("중간", 0.4),
        LOW("낮음", 0.2),
        VERY_LOW("매우 낮음", 0.0),
        UNKNOWN("알 수 없음", 0.0);

        private final String description;
        private final double threshold;

        CorrelationStrength(String description, double threshold) {
            this.description = description;
            this.threshold = threshold;
        }

        public String getDescription() {
            return description;
        }

        public double getThreshold() {
            return threshold;
        }
    }














}//class
