package com.app.domain.stock.service;


import com.app.domain.stock.entity.Stock;
import com.app.domain.stock.dto.StockSearchDto;
import com.app.domain.stock.dto.PageResponseDto;

import java.util.List;
/**
 * Stock 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface StockService {

    /**
     * 주식 목록 조회 (페이지네이션)
     * @param searchDto 검색 조건 및 페이지 정보
     * @return 페이지네이션된 주식 목록
     */
    PageResponseDto<Stock> getStockList(StockSearchDto searchDto);

    /**
     * 주식 검색 (티커 또는 키워드)
     * @param searchDto 검색 조건
     * @return 검색된 주식 목록
     */
    PageResponseDto<Stock> searchStocks(StockSearchDto searchDto);

    /**
     * 티커로 주식 상세 조회
     * @param ticker 티커 심볼
     * @return 주식 상세 정보
     */
    Stock getStockDetail(String ticker);

    /**
     * 업종 목록 조회
     * @return 전체 업종 목록
     */
    List<String> getIndustryList();

    /**
     * 업종별 주식 개수 조회
     * @param industry 업종명
     * @return 해당 업종의 주식 개수
     */
    long getStockCountByIndustry(String industry);







}//class
