package com.app.domain.correlation.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 종목별 분산 점수 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiversificationScore {

    /**
     * 티커 심볼
     */
    private String ticker;

    /**
     * 종목명
     */
    private String stockName;

    /**
     * 분산 점수 (0~1, 높을수록 다른 종목과 상관관계가 낮음)
     */
    private Double diversificationScore;

    /**
     * 평균 상관계수 (다른 종목들과의 평균)
     */
    private Double avgCorrelation;

    /**
     * 높은 상관관계 종목 개수 (0.7 이상)
     */
    private Integer highCorrelationCount;

    /**
     * 선택 여부
     */
    private Boolean selected;

    /**
     * 선택 순위 (낮을수록 우선순위 높음)
     */
    private Integer selectionRank;

    /**
     * 제외 사유 (선택되지 않은 경우)
     */
    private String exclusionReason;


}//class
