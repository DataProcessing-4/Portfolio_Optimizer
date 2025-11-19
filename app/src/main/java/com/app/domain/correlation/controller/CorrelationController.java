package com.app.domain.correlation.controller;

import com.app.app.global.common.ApiResponse;
import com.app.app.global.util.SessionUtil;
import com.app.domain.correlation.dto.*;
import com.app.domain.correlation.service.CorrelationService;
import com.app.domain.correlation.service.DiversificationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * 상관관계 분석 컨트롤러
 */
@RestController
@RequestMapping("/api/correlation")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CorrelationController {

    private final CorrelationService correlationService;
    private final DiversificationService diversificationService;
    private final SessionUtil sessionUtil;  // 이것만 있으면 됨

    /**
     * 상관관계 분석 수행
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse> performCorrelationAnalysis(
            @Valid @RequestBody CorrelationAnalysisRequest request,
            HttpSession httpSession) {

        String businessSessionId = sessionUtil.getBusinessSessionId(httpSession);
        log.info("상관관계 분석 요청 - 비즈니스 세션: {}, 종목수: {}", businessSessionId, request.getTickers().size());

        try {
            CorrelationAnalysisResponse response = correlationService.performCorrelationAnalysis(businessSessionId, request);

            return ResponseEntity.ok(ApiResponse.success(
                    "상관관계 분석이 성공적으로 완료되었습니다.",
                    response
            ));

        } catch (IllegalArgumentException e) {
            log.warn("잘못된 상관관계 분석 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    e.getMessage(),
                    "INVALID_REQUEST"
            ));
        } catch (Exception e) {
            log.error("상관관계 분석 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(
                    "상관관계 분석 중 오류가 발생했습니다.",
                    "ANALYSIS_ERROR"
            ));
        }
    }

    /**
     * 선택된 자산들의 상관관계 분석 수행 (간단한 API)
     */
    @PostMapping("/analyze-selected")
    public ResponseEntity<ApiResponse> analyzeSelectedAssets(HttpSession httpSession) {

        String businessSessionId = sessionUtil.getBusinessSessionId(httpSession);
        log.info("선택된 자산 상관관계 분석 요청 - 비즈니스 세션: {}", businessSessionId);

        try {
            CorrelationAnalysisResponse response = correlationService.performSelectedAssetsAnalysis(businessSessionId);

            return ResponseEntity.ok(ApiResponse.success(
                    "선택된 자산들의 상관관계 분석이 완료되었습니다.",
                    response
            ));

        } catch (IllegalStateException e) {
            log.warn("선택된 자산 상관관계 분석 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    e.getMessage(),
                    "NO_SELECTED_ASSETS"
            ));
        } catch (Exception e) {
            log.error("선택된 자산 상관관계 분석 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(
                    "분석 중 오류가 발생했습니다.",
                    "ANALYSIS_ERROR"
            ));
        }
    }

    /**
     * 상관관계 분석 결과 조회
     */
    @GetMapping("/results")
    public ResponseEntity<ApiResponse> getAnalysisResults(HttpSession httpSession) {

        String businessSessionId = sessionUtil.getBusinessSessionId(httpSession);
        log.info("상관관계 분석 결과 조회 - 비즈니스 세션: {}", businessSessionId);

        try {
            CorrelationAnalysisResponse response = correlationService.getCorrelationAnalysisResults(businessSessionId);

            return ResponseEntity.ok(ApiResponse.success(
                    "상관관계 분석 결과를 조회했습니다.",
                    response
            ));

        } catch (Exception e) {
            log.error("상관관계 분석 결과 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(
                    "분석 결과 조회 중 오류가 발생했습니다.",
                    "QUERY_ERROR"
            ));
        }
    }

    /**
     * 히트맵 데이터 생성
     */
    @GetMapping("/heatmap")
    public ResponseEntity<ApiResponse> generateHeatmap(
            @RequestParam(required = false) List<String> tickers,
            HttpSession httpSession) {

        String businessSessionId = sessionUtil.getBusinessSessionId(httpSession);
        log.info("히트맵 데이터 생성 요청 - 비즈니스 세션: {}", businessSessionId);

        try {
            // 티커가 지정되지 않으면 선택된 자산 사용
            if (tickers == null || tickers.isEmpty()) {
                // 기본적으로 분석된 종목들을 사용
                CorrelationAnalysisResponse analysisResults = correlationService.getCorrelationAnalysisResults(businessSessionId);
                tickers = analysisResults.getTickers();

                if (tickers.isEmpty()) {
                    return ResponseEntity.badRequest().body(ApiResponse.error(
                            "히트맵 생성을 위한 상관관계 분석 결과가 없습니다.",
                            "NO_DATA"
                    ));
                }
            }

            CorrelationHeatmapData heatmapData = correlationService.generateHeatmapData(businessSessionId, tickers);

            return ResponseEntity.ok(ApiResponse.success(
                    "히트맵 데이터가 생성되었습니다.",
                    heatmapData
            ));

        } catch (IllegalStateException e) {
            log.warn("히트맵 데이터 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    e.getMessage(),
                    "NO_DATA"
            ));
        } catch (Exception e) {
            log.error("히트맵 데이터 생성 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(
                    "히트맵 데이터 생성 중 오류가 발생했습니다.",
                    "GENERATION_ERROR"
            ));
        }
    }

    /**
     * 높은 상관관계 종목 쌍 조회
     */
    @GetMapping("/high-correlations")
    public ResponseEntity<ApiResponse> getHighCorrelationPairs(
            @RequestParam(defaultValue = "0.7") Double threshold,
            HttpSession httpSession) {

        String businessSessionId = sessionUtil.getBusinessSessionId(httpSession);
        log.info("높은 상관관계 종목 쌍 조회 - 비즈니스 세션: {}, 임계값: {}", businessSessionId, threshold);

        try {
            List<CorrelationAnalysisResponse.HighCorrelationPair> highCorrelationPairs =
                    correlationService.getHighCorrelationPairs(businessSessionId, threshold);

            return ResponseEntity.ok(ApiResponse.success(
                    String.format("상관계수 %.2f 이상의 종목 쌍 %d개를 조회했습니다.",
                            threshold, highCorrelationPairs.size()),
                    highCorrelationPairs
            ));

        } catch (Exception e) {
            log.error("높은 상관관계 종목 쌍 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(
                    "높은 상관관계 종목 쌍 조회 중 오류가 발생했습니다.",
                    "QUERY_ERROR"
            ));
        }
    }

    /**
     * 분산투자 가이드라인 조회
     */
    @GetMapping("/diversification-guide")
    public ResponseEntity<ApiResponse> getDiversificationGuide(
            @RequestParam(defaultValue = "0.7") Double threshold,
            HttpSession httpSession) {

        String businessSessionId = sessionUtil.getBusinessSessionId(httpSession);
        log.info("분산투자 가이드라인 조회 - 비즈니스 세션: {}, 임계값: {}", businessSessionId, threshold);

        try {
            CorrelationAnalysisResponse.DiversificationGuide guide =
                    correlationService.generateDiversificationGuide(businessSessionId, threshold);

            return ResponseEntity.ok(ApiResponse.success(
                    "분산투자 가이드라인을 생성했습니다.",
                    guide
            ));

        } catch (Exception e) {
            log.error("분산투자 가이드라인 생성 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(
                    "분산투자 가이드라인 생성 중 오류가 발생했습니다.",
                    "GENERATION_ERROR"
            ));
        }
    }

    /**
     * 상관관계 분석 결과 삭제
     */
    @DeleteMapping("/results")
    public ResponseEntity<ApiResponse> deleteAnalysisResults(HttpSession httpSession) {

        String businessSessionId = sessionUtil.getBusinessSessionId(httpSession);
        log.info("상관관계 분석 결과 삭제 - 비즈니스 세션: {}", businessSessionId);

        try {
            correlationService.deleteAnalysisResults(businessSessionId);

            return ResponseEntity.ok(ApiResponse.success(
                    "상관관계 분석 결과가 삭제되었습니다."
            ));

        } catch (Exception e) {
            log.error("상관관계 분석 결과 삭제 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(
                    "분석 결과 삭제 중 오류가 발생했습니다.",
                    "DELETE_ERROR"
            ));
        }
    }

    /**
     * 분산 최적화 실행
     *
     * <p>기능:</p>
     * <ul>
     *   <li>높은 상관관계(0.7 이상) 종목 중복 제거</li>
     *   <li>낮은 상관관계 종목 우선 선택</li>
     *   <li>분산점수 계산 및 표시</li>
     * </ul>
     *
     * @param request 분산 최적화 요청 (티커 목록, 임계값 등)
     * @param httpSession HTTP 세션
     * @return 분산 최적화 결과 (선택된 종목, 제외된 종목, 분산점수 등)
     */
    @PostMapping("/diversification/optimize")
    public ResponseEntity<DiversificationResponse> optimizeDiversification(
            @Valid @RequestBody DiversificationRequest request,
            HttpSession httpSession) {

        String businessSessionId =sessionUtil.getBusinessSessionId(httpSession);

        // Request에 비즈니스 세션 ID 설정
        request.setSessionId(businessSessionId);

        log.info("분산 최적화 요청 - sessionId: {}, tickers: {}, threshold: {}",
                businessSessionId,
                request.getTickers(),
                request.getHighCorrelationThreshold());

        try {
            DiversificationResponse response = diversificationService.optimizeDiversification(request);

            log.info("분산 최적화 완료 - 선택: {}개, 제외: {}개, 포트폴리오 분산점수: {}",
                    response.getSelectedStocks().size(),
                    response.getExcludedStocks().size(),
                    response.getPortfolioDiversificationScore());

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            log.error("분산 최적화 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("분산 최적화 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 분산 최적화 결과 조회
     *
     * @param sessionId 세션 ID (선택적)
     * @param httpSession HTTP 세션
     * @return 최근 분산 최적화 결과
     */
    @GetMapping("/diversification/{sessionId}")
    public ResponseEntity<DiversificationResponse> getDiversificationResult(
            @PathVariable(required = false) String sessionId,
            HttpSession httpSession) {

        // PathVariable이 있으면 사용, 없으면 HTTP 세션에서 추출
        String businessSessionId = (sessionId != null && !sessionId.isEmpty())
                ? sessionId
                : sessionUtil.getBusinessSessionId(httpSession);

        log.info("분산 최적화 결과 조회 - sessionId: {}", businessSessionId);

        // TODO: 결과 조회 로직 구현 (DB에서 저장된 결과 조회)
        return ResponseEntity.ok().build();
    }



}//class
