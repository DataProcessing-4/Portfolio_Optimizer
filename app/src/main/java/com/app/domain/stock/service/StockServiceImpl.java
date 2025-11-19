package com.app.domain.stock.service;

import com.app.domain.stock.dto.PageResponseDto;
import com.app.domain.stock.dto.StockSearchDto;
import com.app.domain.stock.entity.Stock;
import com.app.domain.stock.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Stock 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {


    private final StockMapper stockMapper;

    /**
     * 주식 목록 조회 (페이지네이션)
     */
    @Override
    public PageResponseDto<Stock> getStockList(StockSearchDto searchDto) {
        log.debug("주식 목록 조회 시작: page={}, pageSize={}", searchDto.getPage(), searchDto.getPageSize());

        // 검색 조건 유효성 검증 및 기본값 설정
        validateAndSetDefaults(searchDto);

        try {
            // 검색 조건에 따라 다른 메서드 호출
            List<Stock> stocks;
            long totalCount;

            if (searchDto.hasSearchCondition()) {
                // 검색 조건이 있는 경우
                stocks = stockMapper.selectStockList(searchDto);
                totalCount = stockMapper.countStockList(searchDto);
                log.debug("검색 조건 적용된 주식 조회: 검색어={}, 타입={}, 결과수={}",
                        searchDto.getSearchValue(), searchDto.getSearchType(), stocks.size());
            } else {
                // 전체 목록 조회
                stocks = stockMapper.selectStockList(searchDto);
                totalCount = stockMapper.countStockList(searchDto);
                log.debug("전체 주식 목록 조회: 결과수={}", stocks.size());
            }

            PageResponseDto<Stock> result = PageResponseDto.of(stocks, searchDto.getPage(),
                    searchDto.getPageSize(), totalCount);

            log.debug("주식 목록 조회 완료: 총 {}개 중 {}개 조회", totalCount, stocks.size());
            return result;

        } catch (Exception e) {
            log.error("주식 목록 조회 중 오류 발생", e);
            throw new RuntimeException("주식 목록 조회에 실패했습니다.", e);
        }
    }

    /**
     * 주식 검색 (티커 또는 키워드)
     */
    @Override
    public PageResponseDto<Stock> searchStocks(StockSearchDto searchDto) {
        log.debug("주식 검색 시작: 검색어={}, 타입={}", searchDto.getSearchValue(), searchDto.getSearchType());

        // 검색어 유효성 검증
        if (!StringUtils.hasText(searchDto.getSearchValue())) {
            log.warn("검색어가 비어있음");
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }

        if (!searchDto.isValidSearchType()) {
            log.warn("유효하지 않은 검색 타입: {}", searchDto.getSearchType());
            searchDto.setSearchType("keyword"); // 기본값으로 설정
        }

        validateAndSetDefaults(searchDto);

        try {
            List<Stock> stocks;
            long totalCount;

            if ("ticker".equals(searchDto.getSearchType())) {
                // 티커로 검색
                stocks = stockMapper.selectStockList(searchDto);
                totalCount = stockMapper.countStockList(searchDto);
                log.debug("티커 검색 결과: {}개", stocks.size());
            } else {
                // 키워드로 검색 (종목명 + 티커 포함)
                stocks = stockMapper.searchStockByKeyword(searchDto);
                totalCount = stockMapper.countStockByKeyword(searchDto);
                log.debug("키워드 검색 결과: {}개", stocks.size());
            }

            PageResponseDto<Stock> result = PageResponseDto.of(stocks, searchDto.getPage(),
                    searchDto.getPageSize(), totalCount);

            log.debug("주식 검색 완료: 총 {}개 검색됨", totalCount);
            return result;

        } catch (Exception e) {
            log.error("주식 검색 중 오류 발생", e);
            throw new RuntimeException("주식 검색에 실패했습니다.", e);
        }
    }

    /**
     * 티커로 주식 상세 조회
     */
    @Override
    public Stock getStockDetail(String ticker) {
        log.debug("주식 상세 조회 시작: ticker={}", ticker);

        if (!StringUtils.hasText(ticker)) {
            log.warn("티커가 비어있음");
            throw new IllegalArgumentException("티커를 입력해주세요.");
        }

        try {
            Stock stock = stockMapper.selectStockByTicker(ticker.toUpperCase());

            if (stock == null) {
                log.warn("해당 티커의 주식을 찾을 수 없음: {}", ticker);
                throw new RuntimeException("해당 티커의 주식 정보를 찾을 수 없습니다: " + ticker);
            }

            log.debug("주식 상세 조회 완료: 종목명={}", stock.getStockName());
            return stock;

        } catch (Exception e) {
            log.error("주식 상세 조회 중 오류 발생: ticker={}", ticker, e);
            throw new RuntimeException("주식 상세 조회에 실패했습니다.", e);
        }
    }

    /**
     * 업종 목록 조회
     */
    @Override
    public List<String> getIndustryList() {
        log.debug("업종 목록 조회 시작");

        try {
            List<String> industries = stockMapper.selectIndustryList();
            log.debug("업종 목록 조회 완료: {}개", industries.size());
            return industries;

        } catch (Exception e) {
            log.error("업종 목록 조회 중 오류 발생", e);
            throw new RuntimeException("업종 목록 조회에 실패했습니다.", e);
        }
    }

    /**
     * 업종별 주식 개수 조회
     */
    @Override
    public long getStockCountByIndustry(String industry) {
        log.debug("업종별 주식 개수 조회 시작: industry={}", industry);

        if (!StringUtils.hasText(industry)) {
            log.warn("업종이 비어있음");
            throw new IllegalArgumentException("업종을 입력해주세요.");
        }

        try {
            long count = stockMapper.countStockByIndustry(industry);
            log.debug("업종별 주식 개수 조회 완료: {}개", count);
            return count;

        } catch (Exception e) {
            log.error("업종별 주식 개수 조회 중 오류 발생: industry={}", industry, e);
            throw new RuntimeException("업종별 주식 개수 조회에 실패했습니다.", e);
        }
    }

    // ========== 재무지표 계산 기능 ==========

    /**
     * 모든 재무지표 계산 및 업데이트
     * ROE, 부채비율, PER, PBR을 한번에 계산
     */
    @Override
    @Transactional
    public void calculateAllFinancialRatios() {
        log.info("=== 재무지표 계산 시작 ===");

        try {
            // 계산 가능한 종목 수 확인
            int calculatableCount = stockMapper.countCalculatableStocks();
            log.info("계산 가능한 종목 수: {}", calculatableCount);

            if (calculatableCount == 0) {
                log.warn("계산 가능한 종목이 없습니다.");
                return;
            }

            // 모든 재무지표 일괄 계산
            int updatedCount = stockMapper.calculateAllRatios();
            log.info("재무지표 업데이트 완료: {} 건", updatedCount);

            // 결과 확인
            validateCalculationResults();

        } catch (Exception e) {
            log.error("재무지표 계산 중 오류 발생", e);
            throw new RuntimeException("재무지표 계산 실패", e);
        }

        log.info("=== 재무지표 계산 완료 ===");
    }

    /**
     * ROE만 계산
     */
    @Override
    @Transactional
    public int calculateROE() {
        log.info("ROE 계산 시작");
        int count = stockMapper.updateROE();
        log.info("ROE 계산 완료: {} 건", count);
        return count;
    }

    /**
     * 부채비율만 계산
     */
    @Override
    @Transactional
    public int calculateDebtRatio() {
        log.info("부채비율 계산 시작");
        int count = stockMapper.updateDebtRatio();
        log.info("부채비율 계산 완료: {} 건", count);
        return count;
    }

    /**
     * PER만 계산
     */
    @Override
    @Transactional
    public int calculatePER() {
        log.info("PER 계산 시작");
        int count = stockMapper.updatePER();
        log.info("PER 계산 완료: {} 건", count);
        return count;
    }

    /**
     * PBR만 계산
     */
    @Override
    @Transactional
    public int calculatePBR() {
        log.info("PBR 계산 시작");
        int count = stockMapper.updatePBR();
        log.info("PBR 계산 완료: {} 건", count);
        return count;
    }

    /**
     * 특정 종목의 재무지표 계산
     */
    @Override
    @Transactional
    public void calculateRatiosForStock(String ticker) {
        log.info("종목 {} 재무지표 계산 시작", ticker);
        int count = stockMapper.calculateRatiosByTicker(ticker);
        log.info("종목 {} 재무지표 계산 완료: {} 건", ticker, count);
    }

    /**
     * 전체 종목 수
     */
    @Override
    public int getTotalCount() {
        return (int) stockMapper.countStockList(new StockSearchDto());
    }

    /**
     * 계산 결과 검증
     */
    private void validateCalculationResults() {
        // 간단한 통계 로깅
        int totalCount = getTotalCount();
        log.info("전체 종목 수: {}", totalCount);

        // 샘플 데이터 조회로 결과 확인
        StockSearchDto sampleDto = StockSearchDto.builder()
                .page(1)
                .pageSize(5)
                .build();
        List<Stock> samples = stockMapper.selectStockList(sampleDto);

        log.info("=== 계산 결과 샘플 (상위 5개) ===");
        for (Stock stock : samples) {
            log.info("종목: {} | ROE: {} | PER: {} | PBR: {} | 부채비율: {}",
                    stock.getStockName(),
                    stock.getRoe(),
                    stock.getPer(),
                    stock.getPbr(),
                    stock.getDebtRatio());
        }
    }

    /**
     * 검색 조건 유효성 검증 및 기본값 설정
     */
    private void validateAndSetDefaults(StockSearchDto searchDto) {
        // 페이지 번호 검증
        if (searchDto.getPage() <= 0) {
            searchDto.setPage(1);
        }

        // 페이지 크기 검증 (1~100 사이)
        if (searchDto.getPageSize() <= 0 || searchDto.getPageSize() > 100) {
            searchDto.setPageSize(30); // 기본값
        }

        // 정렬 기준 검증
        if (!StringUtils.hasText(searchDto.getSortBy())) {
            searchDto.setSortBy("stockName");
        }

        // 정렬 순서 검증
        if (!"ASC".equals(searchDto.getSortOrder()) && !"DESC".equals(searchDto.getSortOrder())) {
            searchDto.setSortOrder("ASC");
        }

        // Offset 계산
        searchDto.calculateOffset();

        log.debug("검색 조건 설정 완료: page={}, pageSize={}, offset={}, sortBy={}, sortOrder={}",
                searchDto.getPage(), searchDto.getPageSize(), searchDto.getOffset(),
                searchDto.getSortBy(), searchDto.getSortOrder());
    }






}//class
