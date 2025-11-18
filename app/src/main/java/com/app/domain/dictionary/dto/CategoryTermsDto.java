package com.app.domain.dictionary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 카테고리별 용어 목록 응답용 DTO
 *
 * 용도: "분산투자" 카테고리를 선택했을 때, 해당 카테고리의 용어들을 함께 반환
 *
 * 예시 JSON:
 * {
 *   "categoryId": 1,
 *   "categoryName": "분산투자",
 *   "description": "위험을 분산하는 투자 전략",
 *   "termCount": 5,
 *   "terms": [
 *     {
 *       "termId": 1,
 *       "termName": "포트폴리오",
 *       "definition": "여러 자산을 조합한 투자 묶음"
 *     },
 *     ...
 *   ]
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTermsDto {

    private Integer categoryId;           // 카테고리 ID
    private String categoryName;          // 카테고리 이름
    private String description;           // 카테고리 설명
    private Integer termCount;            // 해당 카테고리의 용어 개수
    private List<TermListDto> terms;      // 용어 목록

    /*
     * 프론트엔드에서 사용 예시:
     * <div className="category-section">
     *   <h2>{categoryTerms.categoryName}</h2>
     *   <p>{categoryTerms.description}</p>
     *   <span>총 {categoryTerms.termCount}개의 용어</span>
     *   <ul>
     *     {categoryTerms.terms.map(term => (
     *       <li>{term.termName}: {term.definition}</li>
     *     ))}
     *   </ul>
     * </div>
     */
}