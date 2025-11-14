package com.app.domain.stock.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 자산 선택 응답 DTO
 * - 서버에서 클라이언트로 선택된 자산 정보를 보낼 때 사용
 * - AssetSelectionRequest와 네이밍 일관성 유지
 * - Entity보다 클라이언트 친화적인 형태로 구성
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class AssetSelectionResponse {

    // ===== 기본 정보 =====
    private Long selectionId;           // 선택 ID
    private String ticker;              // 티커 심볼
    private Integer selectionOrder;     // 선택 순서
    private LocalDateTime selectedAt;   // 선택 일시

    // ===== 주식 상세 정보 =====
    private String stockName;           // 종목명
    private String industry;            // 업종
    private Double closePrice;          // 현재가

    // ===== 투자 지표 =====
    private Double per;                 // PER (주가수익비율)
    private Double pbr;                 // PBR (주가순자산비율)
    private Double roe;                 // ROE (자기자본이익률)










}//class
