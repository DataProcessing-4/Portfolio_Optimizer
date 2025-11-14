package com.app.domain.stock.mapper;

import com.app.domain.stock.entity.Stock;
import com.app.domain.stock.dto.StockSearchDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StockMapper {

    /**
     * 전체 주식 목록 조회 (페이지네이션)
     */
    List<Stock> selectStockList(@Param("searchDto") StockSearchDto searchDto);

    /**
     * 전체 주식 개수 조회
     */
    long countStockList(@Param("searchDto") StockSearchDto searchDto);

    /**
     * 티커로 주식 상세 조회
     */
    Stock selectStockByTicker(@Param("ticker") String ticker);

    /**
     * 키워드로 주식 검색
     */
    List<Stock> searchStockByKeyword(@Param("searchDto") StockSearchDto searchDto);

    /**
     * 키워드로 검색한 주식 개수 조회
     */
    long countStockByKeyword(@Param("searchDto") StockSearchDto searchDto);

    /**
     * 업종 목록 조회
     */
    List<String> selectIndustryList();

    /**
     * 업종별 주식 개수 조회
     */
    long countStockByIndustry(@Param("industry") String industry);


}//interface
