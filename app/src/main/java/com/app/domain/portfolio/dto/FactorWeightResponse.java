package com.app.domain.portfolio.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 팩터 가중치 설정 응답 DTO
 * 모든 가중치가 계산된 상태로 반환
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactorWeightResponse {

    /**
     * ROE 가중치 (0.0000 ~ 1.0000)
     */
    private BigDecimal roeWeight;

    /**
     * PBR 가중치 (0.0000 ~ 1.0000)
     */
    private BigDecimal pbrWeight;

    /**
     * PER 가중치 (0.0000 ~ 1.0000)
     */
    private BigDecimal perWeight;

    /**
     * 세션 ID
     */
    private String sessionId;

    /**
     * 가중치 합계 (항상 1.0000)
     */
    private BigDecimal totalWeight;

    /**
     * 자동 계산된 팩터명 (예: "PER")
     */
    private String autoCalculatedFactor;

    /**
     * 설정 ID
     */
    private Long settingId;

    /**
     * 설정 일시
     */
    private String updatedAt;

    /**
     * 성공 메시지
     */
    private String message;

    /**
     * ROE 가중치를 퍼센트로 반환 (예: 0.4 -> 40)
     */
    public BigDecimal getRoeWeightPercent() {
        return roeWeight.multiply(new BigDecimal("100"));
    }

    /**
     * PBR 가중치를 퍼센트로 반환 (예: 0.35 -> 35)
     */
    public BigDecimal getPbrWeightPercent() {
        return pbrWeight.multiply(new BigDecimal("100"));
    }

    /**
     * PER 가중치를 퍼센트로 반환 (예: 0.25 -> 25)
     */
    public BigDecimal getPerWeightPercent() {
        return perWeight.multiply(new BigDecimal("100"));
    }



}//class
