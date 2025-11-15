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











}//class
