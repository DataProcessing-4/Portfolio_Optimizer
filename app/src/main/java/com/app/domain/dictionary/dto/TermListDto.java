package com.app.domain.dictionary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 용어 목록 조회용 DTO (간단한 정보만)
 *
 * 용도: 용어 리스트를 보여줄 때 사용 (상세 정보 제외)
 *
 * 예시 JSON:
 * {
 *   "termId": 1,
 *   "termName": "포트폴리오",
 *   "termEnglish": "Portfolio",
 *   "definition": "여러 자산을 조합한 투자 묶음",
 *   "categoryName": "분산투자",
 *   "difficultyLevel": "BEGINNER"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermListDto {

    private Integer termId;           // 용어 ID
    private String termName;          // 용어 이름
    private String termEnglish;       // 영문 이름
    private String definition;        // 간단한 정의
    private String categoryName;      // 카테고리 이름 (JOIN으로 가져옴)
    private String difficultyLevel;   // 난이도

    /*
     * 프론트엔드에서 사용 예시:
     * <ul>
     *   {terms.map(term => (
     *     <li key={term.termId}>
     *       <h4>{term.termName} ({term.termEnglish})</h4>
     *       <p>{term.definition}</p>
     *       <span>난이도: {term.difficultyLevel}</span>
     *     </li>
     *   ))}
     * </ul>
     */
}