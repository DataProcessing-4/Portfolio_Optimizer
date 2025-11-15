package com.app.domain.correlation.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 상관관계 분석 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrelationAnalysisRequest {

    /**
     * 분석할 종목 티커 목록 (최소 2개, 최대 10개)
     */
    @NotEmpty(message = "분석할 종목 목록은 필수입니다.")
    @Size(min = 2, max = 10, message = "분석할 종목은 최소 2개, 최대 10개까지 선택 가능합니다.")
    private List<String> tickers;

    /**
     * 분석 기간 (3M, 6M, 1Y 중 선택, 기본값: ALL - 모든 기간 분석)
     */
    @NotNull(message = "분석 기간은 필수입니다.")
    private AnalysisPeriod period = AnalysisPeriod.ALL;

    /**
     * 높은 상관관계 임계값 (기본값: 0.7)
     */
    private Double highCorrelationThreshold = 0.7;

    /**
     * 분석 기간 열거형
     */
    public enum AnalysisPeriod {
        THREE_MONTH("3M"),
        SIX_MONTH("6M"),
        ONE_YEAR("1Y"),
        ALL("ALL");

        private final String code;

        AnalysisPeriod(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        /**
         * 기간에 따른 개월 수 반환
         */
        public int getMonths() {
            switch (this) {
                case THREE_MONTH: return 3;
                case SIX_MONTH: return 6;
                case ONE_YEAR: return 12;
                default: return 12; // ALL의 경우 1년 기준
            }
        }
    }


}//class
