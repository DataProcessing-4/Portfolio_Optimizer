package com.app.domain.correlation.mapper;


import com.app.domain.correlation.entity.CorrelationAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 상관관계 분석 데이터 접근 객체
 */
@Mapper
public interface CorrelationMapper {

    /**
     * 상관관계 분석 결과 저장
     */
    void insertCorrelationAnalysis(CorrelationAnalysis correlation);

    /**
     * 세션별 상관관계 분석 결과 조회
     */
    List<CorrelationAnalysis> findBySessionId(@Param("sessionId") String sessionId);

    /**
     * 높은 상관관계 종목 쌍 조회
     */
    List<CorrelationAnalysis> findHighCorrelations(
            @Param("sessionId") String sessionId,
            @Param("threshold") Double threshold);

    /**
     * 특정 종목 쌍의 상관관계 조회
     */
    CorrelationAnalysis findByTickerPair(
            @Param("sessionId") String sessionId,
            @Param("ticker1") String ticker1,
            @Param("ticker2") String ticker2);

    /**
     * 피어슨 상관계수 계산
     */
    Double calculatePearsonCorrelation(
            @Param("ticker1") String ticker1,
            @Param("ticker2") String ticker2,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 선택된 자산 목록 조회
     */
    List<String> findSelectedTickers(@Param("sessionId") String sessionId);

    /**
     * 종목별 주가 데이터 개수 조회
     */
    Integer countPriceData(
            @Param("ticker") String ticker,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 기간별 일수익률 조회
     */
    List<Double> findDailyReturns(
            @Param("ticker") String ticker,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 두 종목의 공통 거래일 수 조회
     */
    Integer countCommonTradingDays(
            @Param("ticker1") String ticker1,
            @Param("ticker2") String ticker2,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 상관관계 분석 결과 삭제
     */
    void deleteAnalysisResults(@Param("sessionId") String sessionId);

    /**
     * 특정 상관관계 분석 결과 삭제
     */
    void deleteByCorrelationId(@Param("correlationId") Long correlationId);

    /**
     * 세션의 종목별 상관관계 요약 통계
     */
    List<CorrelationSummary> findCorrelationSummary(@Param("sessionId") String sessionId);

    /**
     * 상관관계 요약 통계 DTO
     */
    class CorrelationSummary {
        private String ticker;
        private String stockName;
        private Double avgCorrelation3m;
        private Double avgCorrelation6m;
        private Double avgCorrelation1y;
        private Integer highCorrelationCount;
        private String riskLevel;

        // getters and setters
        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }

        public String getStockName() { return stockName; }
        public void setStockName(String stockName) { this.stockName = stockName; }

        public Double getAvgCorrelation3m() { return avgCorrelation3m; }
        public void setAvgCorrelation3m(Double avgCorrelation3m) { this.avgCorrelation3m = avgCorrelation3m; }

        public Double getAvgCorrelation6m() { return avgCorrelation6m; }
        public void setAvgCorrelation6m(Double avgCorrelation6m) { this.avgCorrelation6m = avgCorrelation6m; }

        public Double getAvgCorrelation1y() { return avgCorrelation1y; }
        public void setAvgCorrelation1y(Double avgCorrelation1y) { this.avgCorrelation1y = avgCorrelation1y; }

        public Integer getHighCorrelationCount() { return highCorrelationCount; }
        public void setHighCorrelationCount(Integer highCorrelationCount) { this.highCorrelationCount = highCorrelationCount; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    }


    //---------------[분산 최적화 로직] 25.11.06 추가 메서드 ---------------//
    /**
     * 세션 ID로 상관관계 데이터 조회
     * @param sessionId 세션 ID
     * @return 상관관계 데이터 목록
     */
    List<CorrelationAnalysis> selectCorrelationsBySessionId(@Param("sessionId") String sessionId);

    /**
     * 특정 티커들 간의 상관관계 조회
     * @param sessionId 세션 ID
     * @param tickers 티커 목록
     * @return 상관관계 데이터 목록
     */
    List<CorrelationAnalysis> selectCorrelationsByTickers(
            @Param("sessionId") String sessionId,
            @Param("tickers") List<String> tickers
    );

    /**
     * 특정 티커와 다른 티커들 간의 상관관계 조회
     * @param sessionId 세션 ID
     * @param ticker 기준 티커
     * @return 상관관계 데이터 목록
     */
    List<CorrelationAnalysis> selectCorrelationsByTicker(
            @Param("sessionId") String sessionId,
            @Param("ticker") String ticker
    );

    /**
     * 상관관계 데이터 삽입
     * @param correlationAnalysis 상관관계 데이터
     * @return 삽입된 행 수
     */
    int insertCorrelation(CorrelationAnalysis correlationAnalysis);

    /**
     * 상관관계 데이터 일괄 삽입
     * @param correlations 상관관계 데이터 목록
     * @return 삽입된 행 수
     */
    int insertCorrelationsBatch(@Param("correlations") List<CorrelationAnalysis> correlations);

// ==========  추가: 종목명 조회 메서드 ==========

    /**
     * 종목 정보 DTO (종목명 조회용)
     */
    class StockInfo {
        private String ticker;
        private String stockName;

        public StockInfo() {}

        public StockInfo(String ticker, String stockName) {
            this.ticker = ticker;
            this.stockName = stockName;
        }

        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }

        public String getStockName() { return stockName; }
        public void setStockName(String stockName) { this.stockName = stockName; }
    }

    /**
     * 티커 목록으로 종목명 조회
     * @param tickers 티커 목록
     * @return Map<ticker, stockName>
     */
    List<StockInfo> findStockInfosByTickers(@Param("tickers") List<String> tickers);

    /**
     * 단일 티커로 종목명 조회
     * @param ticker 티커
     * @return 종목명
     */
    String findStockNameByTicker(@Param("ticker") String ticker);


}//interface
