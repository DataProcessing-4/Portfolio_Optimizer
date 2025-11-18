package com.app.domain.correlation.service;


import com.app.domain.correlation.dto.CorrelationAnalysisRequest;
import com.app.domain.correlation.dto.CorrelationAnalysisResponse;
import com.app.domain.correlation.dto.CorrelationHeatmapData;
import com.app.domain.correlation.entity.CorrelationAnalysis;
import com.app.domain.correlation.mapper.CorrelationMapper;
import com.app.domain.session.mapper.SessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 상관관계 분석 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CorrelationServiceImpl implements CorrelationService {


    private final CorrelationMapper correlationMapper;

    @Override
    public CorrelationAnalysisResponse performCorrelationAnalysis(String sessionId, CorrelationAnalysisRequest request) {
        log.info("상관관계 분석 시작 - 세션: {}, 종목수: {}", sessionId, request.getTickers().size());

        try {
            // 1. 입력 데이터 검증
            validateAnalysisRequest(request);

            // 2. 기존 분석 결과 삭제
            correlationMapper.deleteAnalysisResults(sessionId);

            // 3. 종목 쌍별 상관관계 계산 및 저장
            List<String> tickers = request.getTickers();
            LocalDate endDate = LocalDate.now();

            for (int i = 0; i < tickers.size(); i++) {
                for (int j = i + 1; j < tickers.size(); j++) {
                    String ticker1 = tickers.get(i);
                    String ticker2 = tickers.get(j);

                    // 상관계수 계산
                    CorrelationAnalysis correlation = calculateCorrelation(
                            sessionId, ticker1, ticker2, endDate, request.getPeriod());

                    if (correlation != null) {
                        correlationMapper.insertCorrelationAnalysis(correlation);
                        log.debug("상관계수 계산 완료: {} vs {} = {}",
                                ticker1, ticker2, correlation.getAverageCorrelation());
                    }
                }
            }

            // 4. 분석 결과 조회 및 응답 생성
            return buildAnalysisResponse(sessionId, request.getHighCorrelationThreshold());

        } catch (Exception e) {
            log.error("상관관계 분석 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("상관관계 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public CorrelationHeatmapData generateHeatmapData(String sessionId, List<String> tickers) {
        log.info("히트맵 데이터 생성 - 세션: {}, 종목수: {}", sessionId, tickers.size());

        // 1. 상관관계 분석 결과 조회
        List<CorrelationAnalysis> correlations = correlationMapper.findBySessionId(sessionId);

        if (correlations.isEmpty()) {
            log.warn("히트맵 생성을 위한 상관관계 데이터가 없습니다. 세션: {}", sessionId);
            throw new IllegalStateException("상관관계 분석 결과가 없습니다. 먼저 분석을 수행해주세요.");
        }

        // 2. 히트맵 매트릭스 생성
        return buildHeatmapData(tickers, correlations);
    }

    @Override
    public CorrelationAnalysisResponse getCorrelationAnalysisResults(String sessionId) {
        log.info("상관관계 분석 결과 조회 - 세션: {}", sessionId);

        List<CorrelationAnalysis> correlations = correlationMapper.findBySessionId(sessionId);

        if (correlations.isEmpty()) {
            log.warn("상관관계 분석 결과가 없습니다. 세션: {}", sessionId);
            return CorrelationAnalysisResponse.builder()
                    .sessionId(sessionId)
                    .analysisDate(LocalDate.now())
                    .tickers(Collections.emptyList())
                    .correlationMatrix(new CorrelationAnalysisResponse.PeriodCorrelationMatrix())
                    .highCorrelationPairs(Collections.emptyList())
                    .build();
        }

        return buildAnalysisResponse(sessionId, 0.7);
    }

    @Override
    public List<CorrelationAnalysisResponse.HighCorrelationPair> getHighCorrelationPairs(
            String sessionId, Double threshold) {
        log.info("높은 상관관계 종목 쌍 조회 - 세션: {}, 임계값: {}", sessionId, threshold);

        List<CorrelationAnalysis> correlations = correlationMapper.findHighCorrelations(sessionId, threshold);

        return correlations.stream()
                .map(corr -> CorrelationAnalysisResponse.HighCorrelationPair.builder()
                        .ticker1(corr.getTicker1())
                        .ticker2(corr.getTicker2())
                        .stockName1(corr.getStockName1())
                        .stockName2(corr.getStockName2())
                        .correlation3M(corr.getCorrelation3m())
                        .correlation6M(corr.getCorrelation6m())
                        .correlation1Y(corr.getCorrelation1y())
                        .averageCorrelation(corr.getAverageCorrelation())
                        .riskLevel(corr.getRiskLevel(threshold))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public CorrelationAnalysisResponse.DiversificationGuide generateDiversificationGuide(
            String sessionId, Double threshold) {
        log.info("분산투자 가이드라인 생성 - 세션: {}, 임계값: {}", sessionId, threshold);

        List<CorrelationAnalysis> correlations = correlationMapper.findBySessionId(sessionId);

        if (correlations.isEmpty()) {
            return CorrelationAnalysisResponse.DiversificationGuide.builder()
                    .overallDiversificationScore(0.0)
                    .riskAssessment("POOR")
                    .recommendations(List.of("상관관계 분석을 먼저 수행해주세요."))
                    .warnings(Collections.emptyList())
                    .highlyCorrelatedPairCount(0)
                    .averageCorrelation(0.0)
                    .build();
        }

        // 통계 계산
        long highlyCorrelatedCount = correlations.stream()
                .mapToLong(corr -> corr.isHighCorrelation(threshold) ? 1 : 0)
                .sum();

        double averageCorrelation = correlations.stream()
                .mapToDouble(corr -> Math.abs(corr.getAverageCorrelation() != null ? corr.getAverageCorrelation() : 0.0))
                .average()
                .orElse(0.0);

        // 분산점수 계산 (0-100점)
        double diversificationScore = calculateDiversificationScore(correlations, threshold);

        // 위험도 평가
        String riskAssessment = assessRisk(diversificationScore, highlyCorrelatedCount, correlations.size());

        // 추천사항 생성
        List<String> recommendations = generateRecommendations(
                diversificationScore, highlyCorrelatedCount, correlations.size());

        // 경고사항 생성
        List<String> warnings = generateWarnings(correlations, threshold);

        return CorrelationAnalysisResponse.DiversificationGuide.builder()
                .overallDiversificationScore(Math.round(diversificationScore * 100.0) / 100.0)
                .riskAssessment(riskAssessment)
                .recommendations(recommendations)
                .warnings(warnings)
                .highlyCorrelatedPairCount((int) highlyCorrelatedCount)
                .averageCorrelation(Math.round(averageCorrelation * 1000.0) / 1000.0)
                .build();
    }

    @Override
    public CorrelationAnalysisResponse performSelectedAssetsAnalysis(String sessionId) {
        log.info("선택된 자산 상관관계 분석 수행 - 세션: {}", sessionId);

        // 1. 선택된 자산 목록 조회
        List<String> selectedTickers = correlationMapper.findSelectedTickers(sessionId);

        if (selectedTickers.isEmpty()) {
            throw new IllegalStateException("선택된 자산이 없습니다. 먼저 자산을 선택해주세요.");
        }

        if (selectedTickers.size() < 2) {
            throw new IllegalArgumentException("상관관계 분석을 위해서는 최소 2개의 자산이 필요합니다.");
        }

        // 2. 상관관계 분석 수행
        CorrelationAnalysisRequest request = CorrelationAnalysisRequest.builder()
                .tickers(selectedTickers)
                .period(CorrelationAnalysisRequest.AnalysisPeriod.ALL)
                .highCorrelationThreshold(0.7)
                .build();

        return performCorrelationAnalysis(sessionId, request);
    }

    @Override
    public void deleteAnalysisResults(String sessionId) {
        log.info("상관관계 분석 결과 삭제 - 세션: {}", sessionId);
        correlationMapper.deleteAnalysisResults(sessionId);
    }

    // === Private Methods ===

    private void validateAnalysisRequest(CorrelationAnalysisRequest request) {
        if (request.getTickers() == null || request.getTickers().isEmpty()) {
            throw new IllegalArgumentException("분석할 종목 목록이 비어있습니다.");
        }

        if (request.getTickers().size() < 2) {
            throw new IllegalArgumentException("상관관계 분석을 위해서는 최소 2개의 종목이 필요합니다.");
        }

        if (request.getTickers().size() > 10) {
            throw new IllegalArgumentException("최대 10개의 종목까지만 분석 가능합니다.");
        }

        if (request.getHighCorrelationThreshold() == null ||
                request.getHighCorrelationThreshold() < 0.0 ||
                request.getHighCorrelationThreshold() > 1.0) {
            throw new IllegalArgumentException("상관관계 임계값은 0.0과 1.0 사이의 값이어야 합니다.");
        }
    }

    private CorrelationAnalysis calculateCorrelation(String sessionId, String ticker1, String ticker2,
                                                     LocalDate endDate, CorrelationAnalysisRequest.AnalysisPeriod period) {
        try {
            // 3개월 상관계수 계산
            Double corr3m = null;
            if (period == CorrelationAnalysisRequest.AnalysisPeriod.ALL ||
                    period == CorrelationAnalysisRequest.AnalysisPeriod.THREE_MONTH) {

                LocalDate startDate3m = endDate.minusMonths(3);
                corr3m = correlationMapper.calculatePearsonCorrelation(ticker1, ticker2, startDate3m, endDate);
            }

            // 6개월 상관계수 계산
            Double corr6m = null;
            if (period == CorrelationAnalysisRequest.AnalysisPeriod.ALL ||
                    period == CorrelationAnalysisRequest.AnalysisPeriod.SIX_MONTH) {

                LocalDate startDate6m = endDate.minusMonths(6);
                corr6m = correlationMapper.calculatePearsonCorrelation(ticker1, ticker2, startDate6m, endDate);
            }

            // 1년 상관계수 계산
            Double corr1y = null;
            if (period == CorrelationAnalysisRequest.AnalysisPeriod.ALL ||
                    period == CorrelationAnalysisRequest.AnalysisPeriod.ONE_YEAR) {

                LocalDate startDate1y = endDate.minusMonths(12);
                corr1y = correlationMapper.calculatePearsonCorrelation(ticker1, ticker2, startDate1y, endDate);
            }

            return CorrelationAnalysis.builder()
                    .sessionId(sessionId)
                    .ticker1(ticker1)
                    .ticker2(ticker2)
                    .correlation3m(corr3m)
                    .correlation6m(corr6m)
                    .correlation1y(corr1y)
                    .analysisStartDate(endDate.minusMonths(12))
                    .analysisEndDate(endDate)
                    .analysisDate(LocalDate.now())
                    .build();

        } catch (Exception e) {
            log.error("상관계수 계산 실패: {} vs {} - {}", ticker1, ticker2, e.getMessage());
            return null;
        }
    }

    private CorrelationAnalysisResponse buildAnalysisResponse(String sessionId, Double threshold) {
        List<CorrelationAnalysis> correlations = correlationMapper.findBySessionId(sessionId);

        if (correlations.isEmpty()) {
            return CorrelationAnalysisResponse.builder()
                    .sessionId(sessionId)
                    .analysisDate(LocalDate.now())
                    .tickers(Collections.emptyList())
                    .build();
        }

        // 종목 목록 추출
        Set<String> tickerSet = new HashSet<>();
        correlations.forEach(corr -> {
            tickerSet.add(corr.getTicker1());
            tickerSet.add(corr.getTicker2());
        });
        List<String> tickers = new ArrayList<>(tickerSet);

        // 상관관계 매트릭스 생성
        CorrelationAnalysisResponse.PeriodCorrelationMatrix matrix = buildCorrelationMatrix(tickers, correlations);

        // 높은 상관관계 종목 쌍 추출
        List<CorrelationAnalysisResponse.HighCorrelationPair> highCorrelationPairs =
                getHighCorrelationPairs(sessionId, threshold);

        // 분산투자 가이드라인 생성
        CorrelationAnalysisResponse.DiversificationGuide guide =
                generateDiversificationGuide(sessionId, threshold);

        return CorrelationAnalysisResponse.builder()
                .sessionId(sessionId)
                .analysisDate(LocalDate.now())
                .analysisStartDate(LocalDate.now().minusMonths(12))
                .analysisEndDate(LocalDate.now())
                .tickers(tickers)
                .correlationMatrix(matrix)
                .highCorrelationPairs(highCorrelationPairs)
                .diversificationGuide(guide)
                .build();
    }

    private CorrelationAnalysisResponse.PeriodCorrelationMatrix buildCorrelationMatrix(
            List<String> tickers, List<CorrelationAnalysis> correlations) {

        Map<String, Map<String, Double>> matrix3m = buildMatrix(tickers, correlations, "3M");
        Map<String, Map<String, Double>> matrix6m = buildMatrix(tickers, correlations, "6M");
        Map<String, Map<String, Double>> matrix1y = buildMatrix(tickers, correlations, "1Y");

        return CorrelationAnalysisResponse.PeriodCorrelationMatrix.builder()
                .threeMonthMatrix(matrix3m)
                .sixMonthMatrix(matrix6m)
                .oneYearMatrix(matrix1y)
                .build();
    }

    private Map<String, Map<String, Double>> buildMatrix(List<String> tickers,
                                                         List<CorrelationAnalysis> correlations,
                                                         String period) {
        Map<String, Map<String, Double>> matrix = new HashMap<>();

        // 매트릭스 초기화
        for (String ticker : tickers) {
            matrix.put(ticker, new HashMap<>());
            for (String otherTicker : tickers) {
                matrix.get(ticker).put(otherTicker, ticker.equals(otherTicker) ? 1.0 : 0.0);
            }
        }

        // 상관계수 값 설정
        for (CorrelationAnalysis corr : correlations) {
            Double value = null;
            switch (period) {
                case "3M": value = corr.getCorrelation3m(); break;
                case "6M": value = corr.getCorrelation6m(); break;
                case "1Y": value = corr.getCorrelation1y(); break;
            }

            if (value != null) {
                matrix.get(corr.getTicker1()).put(corr.getTicker2(), value);
                matrix.get(corr.getTicker2()).put(corr.getTicker1(), value);
            }
        }

        return matrix;
    }

    private CorrelationHeatmapData buildHeatmapData(List<String> tickers, List<CorrelationAnalysis> correlations) {
        List<CorrelationHeatmapData.HeatmapPeriodData> periodDataList = new ArrayList<>();

        // 3개월 데이터
        periodDataList.add(buildPeriodHeatmapData("3M", "3개월", tickers, correlations));

        // 6개월 데이터
        periodDataList.add(buildPeriodHeatmapData("6M", "6개월", tickers, correlations));

        // 1년 데이터
        periodDataList.add(buildPeriodHeatmapData("1Y", "1년", tickers, correlations));

        return CorrelationHeatmapData.builder()
                .labels(tickers)
                .periodData(periodDataList)
                .colorScale(CorrelationHeatmapData.ColorScale.builder().build())
                .build();
    }

    private CorrelationHeatmapData.HeatmapPeriodData buildPeriodHeatmapData(
            String period, String periodName, List<String> tickers, List<CorrelationAnalysis> correlations) {

        int size = tickers.size();
        List<List<Double>> matrix = new ArrayList<>();

        double min = 1.0, max = -1.0, sum = 0.0;
        int count = 0;

        for (int i = 0; i < size; i++) {
            List<Double> row = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    row.add(1.0);
                } else {
                    Double value = findCorrelationValue(tickers.get(i), tickers.get(j), correlations, period);
                    row.add(value != null ? value : 0.0);

                    if (value != null) {
                        min = Math.min(min, value);
                        max = Math.max(max, value);
                        sum += value;
                        count++;
                    }
                }
            }
            matrix.add(row);
        }

        return CorrelationHeatmapData.HeatmapPeriodData.builder()
                .period(period)
                .periodName(periodName)
                .matrix(matrix)
                .minValue(count > 0 ? min : 0.0)
                .maxValue(count > 0 ? max : 0.0)
                .avgValue(count > 0 ? sum / count : 0.0)
                .build();
    }

    private Double findCorrelationValue(String ticker1, String ticker2,
                                        List<CorrelationAnalysis> correlations, String period) {
        for (CorrelationAnalysis corr : correlations) {
            if ((corr.getTicker1().equals(ticker1) && corr.getTicker2().equals(ticker2)) ||
                    (corr.getTicker1().equals(ticker2) && corr.getTicker2().equals(ticker1))) {
                switch (period) {
                    case "3M": return corr.getCorrelation3m();
                    case "6M": return corr.getCorrelation6m();
                    case "1Y": return corr.getCorrelation1y();
                }
            }
        }
        return null;
    }

    private double calculateDiversificationScore(List<CorrelationAnalysis> correlations, double threshold) {
        if (correlations.isEmpty()) return 0.0;

        long highlyCorrelatedCount = correlations.stream()
                .mapToLong(corr -> corr.isHighCorrelation(threshold) ? 1 : 0)
                .sum();

        double totalPairs = correlations.size();
        double highCorrelationRatio = highlyCorrelatedCount / totalPairs;

        // 높은 상관관계 비율이 낮을수록 높은 점수
        double baseScore = (1.0 - highCorrelationRatio) * 70.0;

        // 평균 상관계수가 낮을수록 추가 점수
        double averageCorrelation = correlations.stream()
                .mapToDouble(corr -> Math.abs(corr.getAverageCorrelation() != null ? corr.getAverageCorrelation() : 0.0))
                .average()
                .orElse(0.0);

        double bonusScore = (1.0 - averageCorrelation) * 30.0;

        return Math.max(0.0, Math.min(100.0, baseScore + bonusScore));
    }

    private String assessRisk(double diversificationScore, long highCorrelatedCount, int totalPairs) {
        if (diversificationScore >= 80.0) {
            return "EXCELLENT";
        } else if (diversificationScore >= 60.0) {
            return "GOOD";
        } else if (diversificationScore >= 40.0) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }

    private List<String> generateRecommendations(double diversificationScore,
                                                 long highCorrelatedCount, int totalPairs) {
        List<String> recommendations = new ArrayList<>();

        if (diversificationScore >= 80.0) {
            recommendations.add("우수한 분산투자 포트폴리오입니다. 현재 구성을 유지하세요.");
        } else if (diversificationScore >= 60.0) {
            recommendations.add("양호한 분산투자 수준입니다. 일부 종목 조정을 고려해보세요.");
        } else if (diversificationScore >= 40.0) {
            recommendations.add("분산투자 효과가 제한적입니다. 상관관계가 낮은 종목으로 교체를 검토하세요.");
            recommendations.add("다양한 업종의 종목을 추가로 고려해보세요.");
        } else {
            recommendations.add("분산투자 수준이 낮습니다. 포트폴리오 재구성이 필요합니다.");
            recommendations.add("상관관계가 높은 종목들을 서로 다른 업종의 종목으로 교체하세요.");
            recommendations.add("국내외 다양한 시장의 자산을 고려해보세요.");
        }

        double highCorrelationRatio = (double) highCorrelatedCount / totalPairs;
        if (highCorrelationRatio > 0.5) {
            recommendations.add("높은 상관관계 종목 쌍이 많습니다. 포트폴리오 다각화를 강화하세요.");
        }

        return recommendations;
    }

    private List<String> generateWarnings(List<CorrelationAnalysis> correlations, double threshold) {
        List<String> warnings = new ArrayList<>();

        // 매우 높은 상관관계 경고
        long veryHighCorrelatedCount = correlations.stream()
                .mapToLong(corr -> {
                    Double avg = corr.getAverageCorrelation();
                    return (avg != null && Math.abs(avg) >= 0.9) ? 1 : 0;
                })
                .sum();

        if (veryHighCorrelatedCount > 0) {
            warnings.add(String.format("매우 높은 상관관계(0.9 이상) 종목 쌍이 %d개 있습니다. 중복 리스크가 높습니다.",
                    veryHighCorrelatedCount));
        }

        // 음의 상관관계 정보
        long negativeCorrelatedCount = correlations.stream()
                .mapToLong(corr -> {
                    Double avg = corr.getAverageCorrelation();
                    return (avg != null && avg <= -0.3) ? 1 : 0;
                })
                .sum();

        if (negativeCorrelatedCount > 0) {
            warnings.add(String.format("음의 상관관계 종목 쌍이 %d개 있습니다. 헤지 효과를 기대할 수 있습니다.",
                    negativeCorrelatedCount));
        }

        return warnings;
    }

}//class
