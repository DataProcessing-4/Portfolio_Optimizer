package com.app.domain.correlation.service;

import com.app.domain.correlation.dto.DiversificationRequest;
import com.app.domain.correlation.dto.DiversificationResponse;
import com.app.domain.correlation.entity.CorrelationAnalysis;
import com.app.domain.correlation.mapper.CorrelationMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiversificationService {


    private final CorrelationMapper correlationMapper;

    /**
     * 분산 최적화 실행
     * @param request 분산 최적화 요청
     * @return 분산 최적화 결과
     */
    public DiversificationResponse optimizeDiversification(DiversificationRequest request) {
        log.info("분산 최적화 시작 - sessionId: {}, tickers: {}",
                request.getSessionId(), request.getTickers());

        // 1. 상관관계 데이터 조회
        List<CorrelationAnalysis> correlations = correlationMapper.selectCorrelationsByTickers(
                request.getSessionId(),
                request.getTickers()
        );

        if (correlations.isEmpty()) {
            log.warn("상관관계 데이터가 없습니다. sessionId: {}", request.getSessionId());
            throw new IllegalStateException("상관관계 데이터가 존재하지 않습니다. 먼저 상관관계 분석을 수행하세요.");
        }

        // 2. 상관관계 매트릭스 생성
        Map<String, Map<String, Double>> correlationMatrix = buildCorrelationMatrix(
                correlations,
                request.getAnalysisPeriod()
        );

        // 3. 각 종목의 분산 점수 계산
        List<DiversificationScore> allScores = calculateDiversificationScores(
                request.getTickers(),
                correlationMatrix,
                request.getHighCorrelationThreshold()
        );

        // 4. 최적 종목 선택 (그리디 알고리즘)
        List<DiversificationScore> selectedStocks = selectOptimalStocks(
                allScores,
                correlationMatrix,
                request.getHighCorrelationThreshold(),
                request.getTargetStockCount()
        );

        // 5. 제외된 종목 분류
        List<DiversificationScore> excludedStocks = allScores.stream()
                .filter(score -> !score.getSelected())
                .collect(Collectors.toList());

        // 6. 포트폴리오 지표 계산
        Double portfolioAvgCorrelation = calculatePortfolioAvgCorrelation(
                selectedStocks,
                correlationMatrix
        );

        Double portfolioDiversificationScore = calculatePortfolioDiversificationScore(
                portfolioAvgCorrelation
        );

        // 7. 선택된 종목들의 상관관계 매트릭스 필터링
        Map<String, Map<String, Double>> selectedCorrelationMatrix = filterCorrelationMatrix(
                correlationMatrix,
                selectedStocks.stream()
                        .map(DiversificationScore::getTicker)
                        .collect(Collectors.toList())
        );

        // 8. 응답 생성
        return DiversificationResponse.builder()
                .sessionId(request.getSessionId())
                .analysisDateTime(LocalDateTime.now())
                .allScores(allScores)
                .selectedStocks(selectedStocks)
                .excludedStocks(excludedStocks)
                .portfolioAvgCorrelation(portfolioAvgCorrelation)
                .portfolioDiversificationScore(portfolioDiversificationScore)
                .correlationMatrix(selectedCorrelationMatrix)
                .summary(DiversificationResponse.OptimizationSummary.builder()
                        .inputStockCount(request.getTickers().size())
                        .outputStockCount(selectedStocks.size())
                        .removedStockCount(excludedStocks.size())
                        .highCorrelationThreshold(request.getHighCorrelationThreshold())
                        .analysisPeriod(request.getAnalysisPeriod())
                        .optimizationAlgorithm("Greedy Algorithm with Correlation Threshold")
                        .build())
                .build();
    }

    /**
     * 상관관계 매트릭스 생성
     */
    private Map<String, Map<String, Double>> buildCorrelationMatrix(
            List<CorrelationAnalysis> correlations,
            String period) {

        Map<String, Map<String, Double>> matrix = new HashMap<>();

        for (CorrelationAnalysis corr : correlations) {
            Double correlation = corr.getCorrelationByPeriod(period);
            if (correlation == null) continue;

            // ticker1 -> ticker2
            matrix.computeIfAbsent(corr.getTicker1(), k -> new HashMap<>())
                    .put(corr.getTicker2(), correlation);

            // ticker2 -> ticker1 (대칭)
            matrix.computeIfAbsent(corr.getTicker2(), k -> new HashMap<>())
                    .put(corr.getTicker1(), correlation);
        }

        // 자기 자신과의 상관계수는 1.0
        for (String ticker : matrix.keySet()) {
            matrix.get(ticker).put(ticker, 1.0);
        }

        return matrix;
    }

    /**
     * 각 종목의 분산 점수 계산
     */
    private List<DiversificationScore> calculateDiversificationScores(
            List<String> tickers,
            Map<String, Map<String, Double>> correlationMatrix,
            Double highCorrelationThreshold) {

        List<DiversificationScore> scores = new ArrayList<>();

        for (String ticker : tickers) {
            Map<String, Double> correlations = correlationMatrix.get(ticker);
            if (correlations == null || correlations.isEmpty()) {
                log.warn("티커 {}의 상관관계 데이터가 없습니다.", ticker);
                continue;
            }

            // 평균 상관계수 계산 (자기 자신 제외)
            double avgCorrelation = correlations.entrySet().stream()
                    .filter(e -> !e.getKey().equals(ticker))
                    .mapToDouble(Map.Entry::getValue)
                    .average()
                    .orElse(0.0);

            // 높은 상관관계 종목 개수
            int highCorrelationCount = (int) correlations.entrySet().stream()
                    .filter(e -> !e.getKey().equals(ticker))
                    .filter(e -> Math.abs(e.getValue()) >= highCorrelationThreshold)
                    .count();

            // 분산 점수 계산 (평균 상관계수가 낮을수록 높은 점수)
            double diversificationScore = 1.0 - Math.abs(avgCorrelation);

            scores.add(DiversificationScore.builder()
                    .ticker(ticker)
                    .avgCorrelation(avgCorrelation)
                    .highCorrelationCount(highCorrelationCount)
                    .diversificationScore(diversificationScore)
                    .selected(false)
                    .build());
        }

        // 분산 점수 기준 내림차순 정렬
        scores.sort(Comparator.comparing(DiversificationScore::getDiversificationScore).reversed());

        return scores;
    }

    /**
     * 최적 종목 선택 (그리디 알고리즘)
     *
     * 알고리즘:
     * 1. 분산 점수가 가장 높은 종목을 첫 번째로 선택
     * 2. 이미 선택된 종목들과 높은 상관관계(0.7 이상)를 가지지 않는 종목 중
     *    분산 점수가 가장 높은 종목을 순차적으로 선택
     * 3. 목표 개수에 도달할 때까지 반복
     */
    private List<DiversificationScore> selectOptimalStocks(
            List<DiversificationScore> allScores,
            Map<String, Map<String, Double>> correlationMatrix,
            Double highCorrelationThreshold,
            Integer targetCount) {

        List<DiversificationScore> selected = new ArrayList<>();
        Set<String> selectedTickers = new HashSet<>();

        // 이미 분산 점수 기준으로 정렬되어 있음
        for (DiversificationScore score : allScores) {
            if (selected.size() >= targetCount) {
                break;
            }

            String currentTicker = score.getTicker();

            // 이미 선택된 종목들과의 상관관계 확인
            boolean hasHighCorrelation = false;
            for (String selectedTicker : selectedTickers) {
                Double correlation = correlationMatrix.get(currentTicker).get(selectedTicker);
                if (correlation != null && Math.abs(correlation) >= highCorrelationThreshold) {
                    hasHighCorrelation = true;
                    score.setExclusionReason(
                            String.format("종목 %s와 높은 상관관계(%.4f)", selectedTicker, correlation)
                    );
                    break;
                }
            }

            if (!hasHighCorrelation) {
                // 선택
                score.setSelected(true);
                score.setSelectionRank(selected.size() + 1);
                score.setExclusionReason(null);
                selected.add(score);
                selectedTickers.add(currentTicker);

                log.info("종목 선택: {} (분산점수: {:.4f}, 평균상관계수: {:.4f})",
                        currentTicker, score.getDiversificationScore(), score.getAvgCorrelation());
            }
        }

        // 목표 개수에 미달하는 경우 경고
        if (selected.size() < targetCount) {
            log.warn("목표 개수({})에 미달하여 {}개 종목만 선택되었습니다.",
                    targetCount, selected.size());
        }

        return selected;
    }

    /**
     * 포트폴리오 평균 상관계수 계산
     */
    private Double calculatePortfolioAvgCorrelation(
            List<DiversificationScore> selectedStocks,
            Map<String, Map<String, Double>> correlationMatrix) {

        if (selectedStocks.size() <= 1) {
            return 0.0;
        }

        List<String> tickers = selectedStocks.stream()
                .map(DiversificationScore::getTicker)
                .collect(Collectors.toList());

        double sum = 0.0;
        int count = 0;

        for (int i = 0; i < tickers.size(); i++) {
            for (int j = i + 1; j < tickers.size(); j++) {
                Double corr = correlationMatrix.get(tickers.get(i)).get(tickers.get(j));
                if (corr != null) {
                    sum += Math.abs(corr);
                    count++;
                }
            }
        }

        return count > 0 ? sum / count : 0.0;
    }

    /**
     * 포트폴리오 분산 점수 계산 (0~100)
     */
    private Double calculatePortfolioDiversificationScore(Double avgCorrelation) {
        // 평균 상관계수가 낮을수록 높은 점수
        return (1.0 - avgCorrelation) * 100.0;
    }

    /**
     * 선택된 종목들의 상관관계 매트릭스 필터링
     */
    private Map<String, Map<String, Double>> filterCorrelationMatrix(
            Map<String, Map<String, Double>> fullMatrix,
            List<String> selectedTickers) {

        Map<String, Map<String, Double>> filtered = new HashMap<>();

        for (String ticker1 : selectedTickers) {
            Map<String, Double> row = new HashMap<>();
            for (String ticker2 : selectedTickers) {
                Double corr = fullMatrix.get(ticker1).get(ticker2);
                if (corr != null) {
                    row.put(ticker2, corr);
                }
            }
            filtered.put(ticker1, row);
        }

        return filtered;
    }

}//class
