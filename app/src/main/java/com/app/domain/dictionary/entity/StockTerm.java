package com.app.domain.dictionary.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * stock_term 테이블에 대응하는 Entity
 *
 * 역할: 실제 용어(포트폴리오, PER 등)의 상세 정보를 담는 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTerm {

    private Integer termId;                  // term_id                 용어 ID (Primary Key)
    private Integer categoryId;              // category_id             카테고리 ID (Foreign Key)
    private String termName;                 // term_name               용어 이름 (예: "포트폴리오")
    private String termEnglish;              // term_english            영문 이름 (예: "Portfolio")
    private String definition;               // definition              간단한 정의
    private String detailedExplanation;      // detailed_explanation    상세 설명
    private String relatedTerms;             // related_terms           연관 용어 (JSON 형식)
    private String exampleText;              // example_text            예시 설명
    private String imagePath;                // imgae_path              이미지 경로
    private String referenceUrl;             // reference_url           참고 URL
    private String difficultyLevel;          // difficulty_level        난이도 (ENUM)
    private Integer viewCount;               // view_count              조회수
    private Boolean isActive;                // is_active               활성화 여부
    private LocalDateTime createdAt;         // created_at              생성일시
    private LocalDateTime updatedAt;         // updated_at              수정일시

    /*
     * 사용 예시:
     * StockTerm term = StockTerm.builder()
     *     .termName("포트폴리오")
     *     .definition("여러 자산을 조합한 투자 묶음")
     *     .categoryId(1)
     *     .build();
     */
}

