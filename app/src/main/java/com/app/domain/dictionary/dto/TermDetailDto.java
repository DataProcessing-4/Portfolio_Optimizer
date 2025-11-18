package com.app.domain.dictionary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 용어 상세 조회용 DTO (모든 정보 포함)
 *
 * 용도: 특정 용어를 클릭했을 때 보여줄 상세 페이지용
 *
 * 예시 JSON:
 * {
 *   "termId": 1,
 *   "termName": "포트폴리오",
 *   "termEnglish": "Portfolio",
 *   "definition": "여러 자산을 조합한 투자 묶음",
 *   "detailedExplanation": "포트폴리오는 투자 위험을 분산시키기 위해...",
 *   "relatedTerms": ["분산투자", "자산배분", "리밸런싱"],
 *   "exampleText": "예를 들어, 주식 50%, 채권 30%, 현금 20%로...",
 *   "imagePath": "/images/portfolio.png",
 *   "categoryName": "분산투자",
 *   "viewCount": 1250
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermDetailDto {

    private Integer termId;                  // 용어 ID
    private String termName;                 // 용어 이름
    private String termEnglish;              // 영문 이름
    private String definition;               // 간단한 정의
    private String detailedExplanation;      // 상세 설명
    private List<String> relatedTerms;       // 연관 용어 리스트 (JSON 파싱됨)
    private String exampleText;              // 예시
    private String imagePath;                // 이미지 경로
    private String referenceUrl;             // 참고 URL
    private String difficultyLevel;          // 난이도
    private String categoryName;             // 카테고리 이름
    private Integer viewCount;               // 조회수

    /*
     * 프론트엔드에서 사용 예시:
     * <div className="term-detail">
     *   <h1>{termDetail.termName} ({termDetail.termEnglish})</h1>
     *   <p className="definition">{termDetail.definition}</p>
     *   <div className="explanation">{termDetail.detailedExplanation}</div>
     *   <img src={termDetail.imagePath} alt={termDetail.termName} />
     *   <div className="related">
     *     <h3>연관 용어</h3>
     *     {termDetail.relatedTerms.map(term => <span>{term}</span>)}
     *   </div>
     * </div>
     */
}