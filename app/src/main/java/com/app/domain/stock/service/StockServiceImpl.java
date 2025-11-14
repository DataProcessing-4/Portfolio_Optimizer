package com.app.domain.stock.service;

import com.app.domain.stock.dto.PageResponseDto;
import com.app.domain.stock.dto.StockSearchDto;
import com.app.domain.stock.entity.Stock;
import com.app.domain.stock.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
