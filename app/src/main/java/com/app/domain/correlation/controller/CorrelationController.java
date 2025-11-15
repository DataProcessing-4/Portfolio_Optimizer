package com.app.domain.correlation.controller;

import com.app.app.global.common.ApiResponse;
import com.app.domain.correlation.dto.CorrelationAnalysisRequest;
import com.app.domain.correlation.dto.CorrelationAnalysisResponse;
import com.app.domain.correlation.dto.CorrelationHeatmapData;
import com.app.domain.correlation.service.CorrelationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    /**
     * 상관관계 분석 수행
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse> performCorrelationAnalysis(
            @Valid @RequestBody CorrelationAnalysisRequest request,
            HttpServletRequest httpRequest) {

        String sessionId = httpRequest.getSession().getId();
        log.info("상관관계 분석 요청 - 세션: {}, 종목수: {}", sessionId, request.getTickers().size());

        try {
            CorrelationAnalysisResponse response = correlationService.performCorrelationAnalysis(sessionId, request);

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
    public ResponseEntity<ApiResponse> analyzeSelectedAssets(
            HttpServletRequest httpRequest) {

        String sessionId = httpRequest.getSession().getId();
        log.info("선택된 자산 상관관계 분석 요청 - 세션: {}", sessionId);

        try {
            CorrelationAnalysisResponse response = correlationService.performSelectedAssetsAnalysis(sessionId);

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
    public ResponseEntity<ApiResponse> getAnalysisResults(
            HttpServletRequest httpRequest) {

        String sessionId = httpRequest.getSession().getId();
        log.info("상관관계 분석 결과 조회 - 세션: {}", sessionId);

        try {
            CorrelationAnalysisResponse response = correlationService.getCorrelationAnalysisResults(sessionId);

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
            HttpServletRequest httpRequest) {

        String sessionId = httpRequest.getSession().getId();
        log.info("히트맵 데이터 생성 요청 - 세션: {}", sessionId);

        try {
            // 티커가 지정되지 않으면 선택된 자산 사용
            if (tickers == null || tickers.isEmpty()) {
                // 기본적으로 분석된 종목들을 사용
                CorrelationAnalysisResponse analysisResults = correlationService.getCorrelationAnalysisResults(sessionId);
                tickers = analysisResults.getTickers();

                if (tickers.isEmpty()) {
                    return ResponseEntity.badRequest().body(ApiResponse.error(
                            "히트맵 생성을 위한 상관관계 분석 결과가 없습니다.",
                            "NO_DATA"
                    ));
                }
            }

            CorrelationHeatmapData heatmapData = correlationService.generateHeatmapData(sessionId, tickers);

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
            HttpServletRequest httpRequest) {

        String sessionId = httpRequest.getSession().getId();
        log.info("높은 상관관계 종목 쌍 조회 - 세션: {}, 임계값: {}", sessionId, threshold);

        try {
            List<CorrelationAnalysisResponse.HighCorrelationPair> highCorrelationPairs =
                    correlationService.getHighCorrelationPairs(sessionId, threshold);

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
            HttpServletRequest httpRequest) {

        String sessionId = httpRequest.getSession().getId();
        log.info("분산투자 가이드라인 조회 - 세션: {}, 임계값: {}", sessionId, threshold);

        try {
            CorrelationAnalysisResponse.DiversificationGuide guide =
                    correlationService.generateDiversificationGuide(sessionId, threshold);

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
    public ResponseEntity<ApiResponse> deleteAnalysisResults(HttpServletRequest httpRequest) {

        String sessionId = httpRequest.getSession().getId();
        log.info("상관관계 분석 결과 삭제 - 세션: {}", sessionId);

        try {
            correlationService.deleteAnalysisResults(sessionId);

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

}//class
