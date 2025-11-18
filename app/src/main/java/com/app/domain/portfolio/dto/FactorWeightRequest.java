package com.app.domain.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 팩터 가중치 설정 요청 DTO (v1.2)
 * 사용자가 두 개의 가중치를 입력하면 나머지 하나를 자동 계산
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactorWeightRequest {

    /**
     * ROE 가중치 (0.0000 ~ 1.0000)
     * null인 경우 자동 계산됨
     */
    private BigDecimal roeWeight;

    /**
     * PBR 가중치 (0.0000 ~ 1.0000)
     * null인 경우 자동 계산됨
     */
    private BigDecimal pbrWeight;

    /**
     * PER 가중치 (0.0000 ~ 1.0000)
     * null인 경우 자동 계산됨
     */
    private BigDecimal perWeight;

    /**
     * 세션 ID (Controller에서 설정)
     */
    private String sessionId;

    /**
     * 사용자 IP (Controller에서 설정, 선택사항)
     */
    private String userIp;

    /**
     * User-Agent (Controller에서 설정, 선택사항)
     */
    private String userAgent;

    /**
     * 자동 계산된 팩터 추적용
     */
    private String autoCalculatedFactor;

    /**
     * null이 아닌 가중치의 개수를 반환
     */
    public int getNonNullWeightCount() {
        int count = 0;
        if (roeWeight != null) count++;
        if (pbrWeight != null) count++;
        if (perWeight != null) count++;
        return count;
    }

    /**
     * 입력된 가중치의 합계를 반환
     */
    public BigDecimal getSumOfNonNullWeights() {
        BigDecimal sum = BigDecimal.ZERO;
        if (roeWeight != null) sum = sum.add(roeWeight);
        if (pbrWeight != null) sum = sum.add(pbrWeight);
        if (perWeight != null) sum = sum.add(perWeight);
        return sum;
    }

    /**
     * 자동 계산된 팩터 기록
     */
    public void markAutoCalculatedFactor() {
        if (roeWeight == null) {
            autoCalculatedFactor = "ROE";
        } else if (pbrWeight == null) {
            autoCalculatedFactor = "PBR";
        } else if (perWeight == null) {
            autoCalculatedFactor = "PER";
        }
    }


}//class