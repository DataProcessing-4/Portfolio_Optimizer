package com.app.domain.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSearchDto {

    // 검색 조건
    private String searchType;      // "ticker" 또는 "keyword"
    private String searchValue;     // 검색어
    private String industry;        // 업종 필터

    // 정렬 조건
    private String sortBy;          // 정렬 기준 (stockName, per, pbr, roe 등)
    private String sortOrder;       // 정렬 순서 (ASC, DESC)

    // 페이지네이션
    private int page;               // 페이지 번호 (1부터 시작)
    private int pageSize;           // 페이지 크기 (기본 30개)
    private int offset;             // OFFSET 값

    public StockSearchDto(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
        this.offset = (page - 1) * pageSize;
        this.sortBy = "stockName";  // 기본 정렬: 종목명
        this.sortOrder = "ASC";     // 기본 정렬: 오름차순
    }

    public void calculateOffset() {
        this.offset = (this.page - 1) * this.pageSize;
    }

    // 유효성 검증 메서드
    public boolean isValidSearchType() {
        return "ticker".equals(searchType) || "keyword".equals(searchType);
    }

    public boolean hasSearchCondition() {
        return searchValue != null && !searchValue.trim().isEmpty();
    }


}//class
