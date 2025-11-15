package com.app.domain.correlation.service;


import com.app.domain.correlation.dto.CorrelationAnalysisRequest;
import com.app.domain.correlation.dto.CorrelationAnalysisResponse;
import com.app.domain.correlation.dto.CorrelationHeatmapData;

import java.util.List;

/**
 * 상관관계 분석 서비스 인터페이스
 */
public interface CorrelationService {

    CorrelationAnalysisResponse performCorrelationAnalysis(String sessionId, CorrelationAnalysisRequest request);

    CorrelationHeatmapData generateHeatmapData(String sessionId, List<String> tickers);

    CorrelationAnalysisResponse getCorrelationAnalysisResults(String sessionId);

    List<CorrelationAnalysisResponse.HighCorrelationPair> getHighCorrelationPairs(
            String sessionId, Double threshold);

    CorrelationAnalysisResponse.DiversificationGuide generateDiversificationGuide(
            String sessionId, Double threshold);

    CorrelationAnalysisResponse performSelectedAssetsAnalysis(String sessionId);

    void deleteAnalysisResults(String sessionId);


}//interface
