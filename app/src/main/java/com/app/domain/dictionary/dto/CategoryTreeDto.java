package com.app.domain.dictionary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 카테고리 트리 구조 응답용 DTO
 *
 * 용도: 카테고리를 계층 구조로 보여줄 때 사용
 *
 * 예시 JSON:
 * {
 *   "categoryId": 1,
 *   "categoryName": "분산투자",
 *   "description": "위험 분산 전략",
 *   "children": [
 *     {
 *       "categoryId": 2,
 *       "categoryName": "포트폴리오 이론",
 *       "children": []
 *     }
 *   ]
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeDto {

    private Integer categoryId;              // 카테고리 ID
    private String categoryName;             // 카테고리 이름
    private String description;              // 설명
    private Integer parentCategoryId;        // 부모 카테고리 ID
    private Integer sortOrder;               // 정렬 순서
    private List<CategoryTreeDto> children;  // 하위 카테고리 목록 (재귀 구조!)

    /*
     * 프론트엔드에서 사용 예시:
     * categories.map(category => (
     *   <div>
     *     <h3>{category.categoryName}</h3>
     *     {category.children.map(child => <div>{child.categoryName}</div>)}
     *   </div>
     * ))
     */
}