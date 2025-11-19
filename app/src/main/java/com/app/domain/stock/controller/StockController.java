package com.app.domain.stock.controller;

import com.app.domain.stock.dto.AssetSelectionRequest;
import com.app.domain.stock.dto.AssetSelectionResponse;
import com.app.domain.stock.dto.PageResponseDto;
import com.app.domain.stock.entity.Stock;
import com.app.domain.stock.dto.StockSearchDto;
import com.app.domain.stock.service.StockService;
import com.app.domain.stock.service.UserSelectedAssetsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final UserSelectedAssetsService selectedAssetsService;

    /**
     * 주식 목록 조회 (페이지네이션)
     * GET /api/stocks?page=1&pageSize=30&sortBy=stockName&sortOrder=ASC&industry=제조업
     */
    @GetMapping
    public ResponseEntity<PageResponseDto<Stock>> getStockList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int pageSize,
            @RequestParam(defaultValue = "stockName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortOrder,
            @RequestParam(required = false) String industry) {

        log.debug("주식 목록 조회 요청: page={}, pageSize={}, sortBy={}, sortOrder={}, industry={}",
                page, pageSize, sortBy, sortOrder, industry);

        StockSearchDto searchDto = StockSearchDto.builder()
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .industry(industry)
                .build();

        PageResponseDto<Stock> result = stockService.getStockList(searchDto);

        log.debug("주식 목록 조회 응답: 총 {}개 중 {}개 조회",
                result.getTotalElements(), result.getContent().size());

        return ResponseEntity.ok(result);
    }

    /**
     * 주식 검색 (티커 또는 키워드)
     * GET /api/stocks/search?searchType=ticker&searchValue=005930&page=1&pageSize=30
     * GET /api/stocks/search?searchType=keyword&searchValue=삼성&page=1&pageSize=30
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponseDto<Stock>> searchStocks(
            @RequestParam String searchType,
            @RequestParam String searchValue,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int pageSize,
            @RequestParam(defaultValue = "stockName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortOrder,
            @RequestParam(required = false) String industry) {

        log.debug("주식 검색 요청: type={}, value={}, page={}, pageSize={}",
                searchType, searchValue, page, pageSize);

        StockSearchDto searchDto = StockSearchDto.builder()
                .searchType(searchType)
                .searchValue(searchValue)
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .industry(industry)
                .build();

        PageResponseDto<Stock> result = stockService.searchStocks(searchDto);

        log.debug("주식 검색 응답: 검색어 '{}'로 {}개 검색됨",
                searchValue, result.getTotalElements());

        return ResponseEntity.ok(result);
    }

    /**
     * 주식 상세 조회 (티커로 조회)
     * GET /api/stocks/{ticker}
     */
    @GetMapping("/{ticker}")
    public ResponseEntity<Stock> getStockDetail(@PathVariable String ticker) {
        log.debug("주식 상세 조회 요청: ticker={}", ticker);

        Stock stock = stockService.getStockDetail(ticker);

        log.debug("주식 상세 조회 응답: 종목명={}", stock.getStockName());

        return ResponseEntity.ok(stock);
    }

    /**
     * 업종 목록 조회
     * GET /api/stocks/industries
     */
    @GetMapping("/industries")
    public ResponseEntity<List<String>> getIndustryList() {
        log.debug("업종 목록 조회 요청");

        List<String> industries = stockService.getIndustryList();

        log.debug("업종 목록 조회 응답: {}개 업종", industries.size());

        return ResponseEntity.ok(industries);
    }

    /**
     * 업종별 주식 개수 조회
     * GET /api/stocks/industries/{industry}/count
     */
    @GetMapping("/industries/{industry}/count")
    public ResponseEntity<Long> getStockCountByIndustry(@PathVariable String industry) {
        log.debug("업종별 주식 개수 조회 요청: industry={}", industry);

        long count = stockService.getStockCountByIndustry(industry);

        log.debug("업종별 주식 개수 조회 응답: {}개", count);

        return ResponseEntity.ok(count);
    }

//    /**
//     * 예외 처리
//     */
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
//        log.warn("잘못된 요청: {}", e.getMessage());
//        return ResponseEntity.badRequest().body(e.getMessage());
//    }
//
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
//        log.error("서버 오류", e);
//        return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다: " + e.getMessage());
//    }


    // ========== 재무지표 계산 API ==========

    /**
     * 모든 재무지표 계산 (ROE, PER, PBR, 부채비율)
     * POST /api/stocks/calculate-ratios
     */
    @PostMapping("/calculate-ratios")
    public ResponseEntity<Map<String, Object>> calculateAllRatios() {
        log.info("재무지표 일괄 계산 요청");

        try {
            stockService.calculateAllFinancialRatios();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "재무지표 계산이 완료되었습니다.");
            response.put("totalStocks", stockService.getTotalCount());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("재무지표 계산 실패", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "재무지표 계산 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * ROE만 계산
     * POST /api/stocks/calculate-roe
     */
    @PostMapping("/calculate-roe")
    public ResponseEntity<Map<String, Object>> calculateROE() {
        log.info("ROE 계산 요청");

        try {
            int count = stockService.calculateROE();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "ROE 계산 완료");
            response.put("updatedCount", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("ROE 계산 실패", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    /**
     * 부채비율만 계산
     * POST /api/stocks/calculate-debt-ratio
     */
    @PostMapping("/calculate-debt-ratio")
    public ResponseEntity<Map<String, Object>> calculateDebtRatio() {
        log.info("부채비율 계산 요청");

        try {
            int count = stockService.calculateDebtRatio();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "부채비율 계산 완료");
            response.put("updatedCount", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("부채비율 계산 실패", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    /**
     * PER만 계산
     * POST /api/stocks/calculate-per
     */
    @PostMapping("/calculate-per")
    public ResponseEntity<Map<String, Object>> calculatePER() {
        log.info("PER 계산 요청");

        try {
            int count = stockService.calculatePER();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PER 계산 완료");
            response.put("updatedCount", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("PER 계산 실패", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    /**
     * PBR만 계산
     * POST /api/stocks/calculate-pbr
     */
    @PostMapping("/calculate-pbr")
    public ResponseEntity<Map<String, Object>> calculatePBR() {
        log.info("PBR 계산 요청");

        try {
            int count = stockService.calculatePBR();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "PBR 계산 완료");
            response.put("updatedCount", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("PBR 계산 실패", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    /**
     * 특정 종목 재무지표 계산
     * POST /api/stocks/{ticker}/calculate-ratios
     */
    @PostMapping("/{ticker}/calculate-ratios")
    public ResponseEntity<Map<String, Object>> calculateRatiosForStock(@PathVariable String ticker) {
        log.info("종목 {} 재무지표 계산 요청", ticker);

        try {
            stockService.calculateRatiosForStock(ticker);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "종목 " + ticker + " 재무지표 계산 완료");
            response.put("ticker", ticker);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("종목 {} 재무지표 계산 실패", ticker, e);
            return ResponseEntity.internalServerError().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    /**
     * 예외 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("잘못된 요청: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        log.error("서버 오류", e);
        return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다: " + e.getMessage());
    }


    // ========== 자산 선택 API ==========


    /**
     * 자산 선택
     * POST /api/stocks/select
     */
    @PostMapping("/select")
    public ResponseEntity<Map<String, Object>> selectAsset(
            @RequestBody Map<String, String> request,
            HttpSession session) {

        String ticker = request.get("ticker");
        String sessionId = session.getId();

        log.info("자산 선택 요청 - 세션: {}, 티커: {}", sessionId, ticker);

        try {
            // 기존 서비스 메서드 사용
            AssetSelectionRequest req = AssetSelectionRequest.builder()
                    .ticker(ticker)
                    .build();

            selectedAssetsService.addSelectedAsset(sessionId, req);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "자산이 선택되었습니다.");
            response.put("data", Map.of("ticker", ticker));
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("자산 선택 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));

        } catch (IllegalStateException e) {
            log.warn("자산 선택 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));

        } catch (Exception e) {
            log.error("자산 선택 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 선택된 자산 목록 조회
     * GET /api/stocks/selected
     */
    @GetMapping("/selected")
    public ResponseEntity<Map<String, Object>> getSelectedAssets(HttpSession session) {

        String sessionId = session.getId();
        log.info("선택된 자산 조회 - 세션: {}", sessionId);

        try {
            // 기존 서비스 메서드 사용
            List<AssetSelectionResponse> selectedAssets = selectedAssetsService.getSelectedAssets(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "선택된 자산을 조회했습니다.");
            response.put("data", selectedAssets);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("선택된 자산 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 자산 선택 취소
     * DELETE /api/stocks/deselect/{ticker}
     */
    @DeleteMapping("/deselect/{ticker}")
    public ResponseEntity<Map<String, Object>> deselectAsset(
            @PathVariable String ticker,
            HttpSession session) {

        String sessionId = session.getId();
        log.info("자산 선택 취소 - 세션: {}, 티커: {}", sessionId, ticker);

        try {
            // 기존 서비스 메서드 사용 (removeSelectedAsset)
            boolean success = selectedAssetsService.removeSelectedAsset(sessionId, ticker);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "자산 선택이 취소되었습니다.");
                response.put("data", Map.of("ticker", ticker));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "선택되지 않은 자산입니다.");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (IllegalArgumentException e) {
            log.warn("자산 선택 취소 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));

        } catch (Exception e) {
            log.error("자산 선택 취소 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 모든 자산 선택 초기화
     * DELETE /api/stocks/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearSelectedAssets(HttpSession session) {

        String sessionId = session.getId();
        log.info("모든 자산 선택 초기화 - 세션: {}", sessionId);

        try {
            // 기존 서비스 메서드 사용 (clearAllSelectedAssets)
            boolean success = selectedAssetsService.clearAllSelectedAssets(sessionId);

            // 성공 여부와 상관없이 현재 개수를 0으로 반환
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "모든 자산 선택이 초기화되었습니다.");
            response.put("data", Map.of("deletedCount", success ? 1 : 0));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("자산 선택 초기화 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "서버 오류가 발생했습니다."));
        }
    }

}//class
