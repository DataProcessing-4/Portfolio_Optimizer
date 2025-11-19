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

    // ========== 재무지표 계산 기능 ==========

    /**
     * ROE 계산 및 업데이트
     * ROE = (당기순이익 / 자기자본) * 100
     */
    int updateROE();

    /**
     * 부채비율 계산 및 업데이트
     * 부채비율 = (총부채 / 자기자본) * 100
     */
    int updateDebtRatio();

    /**
     * PER 계산 및 업데이트
     * PER = 주가 / 주당순이익
     */
    int updatePER();

    /**
     * PBR 계산 및 업데이트
     * PBR = 주가 / 주당순자산가치
     */
    int updatePBR();

    /**
     * 모든 재무지표 일괄 계산
     */
    int calculateAllRatios();

    /**
     * 특정 종목의 재무지표 계산
     */
    int calculateRatiosByTicker(@Param("ticker") String ticker);

    /**
     * 재무지표 계산 가능한 종목 수 조회
     */
    int countCalculatableStocks();


}//interface
