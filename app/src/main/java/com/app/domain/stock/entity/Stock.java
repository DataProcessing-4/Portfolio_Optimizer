package com.app.domain.stock.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {



    // 기본 정보
    private String ticker;              // 티커 심볼 (PK)
    private String stockName;           // 종목명
    private String industry;            // 업종
    private Integer accountingYear;     // 회계년도
    private Integer settlementMonth;    // 결산월

    // 교수님 제공 재무 데이터
    private Long totalAssets;           // 총자산
    private Long totalDebt;             // 총부채
    private Long totalEquity;           // 총자본
    private Long revenue;               // 매출액
    private Long operatingProfit;       // 영업이익
    private Long netIncome;             // 당기순이익

    // 교수님 제공 주당 지표
    private BigDecimal eps;             // 주당순이익(수정_EPS)
    private BigDecimal bps;             // 주당순자산가치(수정_BPS)
    private BigDecimal sps;             // 주당매출(수정_SPS)
    private BigDecimal cfps;            // 주당현금흐름(수정_CFPS)
    private BigDecimal ebitdaps;        // 주당EBITDA(수정_EBITDAPS)

    // 계산된 재무비율
    private BigDecimal roe;             // 자기자본이익률
    private BigDecimal debtRatio;       // 부채비율

    // 정적 주가 데이터
    private LocalDate referenceDate;    // 기준 일자
    private BigDecimal closePrice;      // 기준일 종가
    private Long marketCap;             // 기준일 시가총액
    private BigDecimal per;             // 주가수익비율
    private BigDecimal pbr;             // 주가순자산비율

    // 메타 데이터
    private LocalDateTime createdAt;    // 생성일시
    private LocalDateTime updatedAt;    // 수정일시



}//class
