package com.app.domain.stock.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
/**
 * 사용자 선택 자산 엔티티
 * - 데이터베이스 user_selected_assets 테이블과 1:1 매핑
 * - 순수한 데이터 구조만 정의 (비즈니스 로직 포함 안함)
 */
@Data                    // getter, setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor       // 파라미터 없는 기본 생성자
@AllArgsConstructor      // 모든 필드를 파라미터로 받는 생성자
@Builder                 // 빌더 패턴으로 객체 생성 가능
public class UserSelectedAssets {

    // ===== 기본 필드 (DB 컬럼과 매핑) =====
    private Long selectionId;           // 선택 ID (Primary Key)
    private String sessionId;           // 세션 ID (Foreign Key)
    private String ticker;              // 티커 심볼 (Foreign Key)
    private Integer selectionOrder;     // 선택 순서 (1~10)
    private LocalDateTime selectedAt;   // 선택 일시

    // ===== 조인 필드들 (Stock 테이블과 조인해서 가져오는 정보) =====
    private String stockName;           // 종목명 (stock.stock_name)
    private String industry;            // 업종 (stock.industry)
    private Double closePrice;          // 종가 (stock.close_price)
    private Double per;                 // PER (stock.per)
    private Double pbr;                 // PBR (stock.pbr)
    private Double roe;                 // ROE (stock.roe)












}//class
