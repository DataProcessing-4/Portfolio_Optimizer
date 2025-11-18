package com.app.domain.dictionary.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * stock_term_category 테이블에 대응하는 Entity
 *
 * 역할: 용어 카테고리 정보를 담는 객체
 * 예시: "분산투자", "팩터", "그래프" 같은 대분류/소분류
 */

@Data                    // Lombok: getter, setter, toString 자동 생성
@Builder                 // Lombok: 객체 생성을 편하게 해주는 패턴
@NoArgsConstructor       // Lombok: 기본 생성자 자동 생성
@AllArgsConstructor      // Lombok: 모든 필드를 받는 생성자 자동 생성
public class StockTermCategory {

    private Integer categoryId;           // 카테고리 ID (Primary Key)
    private String categoryName;          // 카테고리 이름 (예: "분산투자")
    private Integer parentCategoryId;     // 부모 카테고리 ID (트리 구조용)
    private String description;           // 카테고리 설명
    private Integer sortOrder;            // 정렬 순서
    private Boolean isActive;             // 활성화 여부
    private LocalDateTime createdAt;      // 생성일시

    /*
     * 사용 예시:
     * StockTermCategory category = StockTermCategory.builder()
     *     .categoryName("분산투자")
     *     .description("위험을 분산하는 투자 전략")
     *     .build();
     */
}