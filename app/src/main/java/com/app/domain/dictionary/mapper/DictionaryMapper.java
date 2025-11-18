package com.app.domain.dictionary.mapper;

import com.app.domain.dictionary.entity.StockTerm;
import com.app.domain.dictionary.entity.StockTermCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Dictionary 관련 데이터베이스 작업을 위한 Mapper
 *
 * 역할: SQL 쿼리를 실행하는 메서드 정의
 * 실제 SQL은 DictionaryMapper.xml 파일에 작성
 *
 * 동작 원리:
 * 1. Service에서 mapper.findAllCategories() 호출
 * 2. MyBatis가 DictionaryMapper.xml에서 같은 이름의 SQL 찾기
 * 3. SQL 실행 후 결과를 Entity 객체로 변환
 * 4. Service로 반환
 */
@Mapper  // MyBatis가 이 인터페이스를 구현체로 만들어줌
public interface DictionaryMapper {

    // ============ 카테고리 관련 ============

    /**
     * 모든 카테고리 조회
     *
     * 사용 예시:
     * List<StockTermCategory> categories = mapper.findAllCategories();
     *
     * 실행될 SQL (DictionaryMapper.xml에 정의):
     * SELECT * FROM stock_term_category WHERE is_active = true ORDER BY sort_order
     */
    List<StockTermCategory> findAllCategories();

    /**
     * 최상위 카테고리만 조회 (parent_category_id가 NULL인 것들)
     *
     * 사용 예시:
     * List<StockTermCategory> rootCategories = mapper.findRootCategories();
     * // 결과: [분산투자, 팩터, 그래프]
     */
    List<StockTermCategory> findRootCategories();

    /**
     * 특정 부모의 하위 카테고리 조회
     *
     * 사용 예시:
     * List<StockTermCategory> children = mapper.findChildCategories(1);
     * // 카테고리 ID 1의 하위 카테고리들 조회
     */
    List<StockTermCategory> findChildCategories(@Param("parentId") Integer parentId);

    /**
     * 카테고리 ID로 단일 카테고리 조회
     *
     * 사용 예시:
     * StockTermCategory category = mapper.findCategoryById(1);
     */
    StockTermCategory findCategoryById(@Param("categoryId") Integer categoryId);


    // ============ 용어 관련 ============

    /**
     * 모든 활성화된 용어 조회
     *
     * 사용 예시:
     * List<StockTerm> allTerms = mapper.findAllTerms();
     */
    List<StockTerm> findAllTerms();

    /**
     * 특정 카테고리의 용어 조회
     *
     * 사용 예시:
     * List<StockTerm> terms = mapper.findTermsByCategory(1);
     * // 카테고리 ID 1 (예: 분산투자)의 모든 용어 조회
     */
    List<StockTerm> findTermsByCategory(@Param("categoryId") Integer categoryId);

    /**
     * 용어 ID로 상세 정보 조회
     *
     * 사용 예시:
     * StockTerm term = mapper.findTermById(5);
     * // ID 5인 용어의 모든 정보 조회
     */
    StockTerm findTermById(@Param("termId") Integer termId);

    /**
     * 용어 검색 (이름으로)
     *
     * 사용 예시:
     * List<StockTerm> results = mapper.searchTerms("포트폴리오");
     * // "포트폴리오"가 포함된 모든 용어 검색
     */
    List<StockTerm> searchTerms(@Param("keyword") String keyword);

    /**
     * 용어 조회수 증가
     *
     * 사용 예시:
     * mapper.incrementViewCount(5);
     * // ID 5인 용어의 조회수를 1 증가
     *
     * 실행될 SQL:
     * UPDATE stock_term SET view_count = view_count + 1 WHERE term_id = 5
     */
    void incrementViewCount(@Param("termId") Integer termId);

    /**
     * 인기 용어 조회 (조회수 기준)
     *
     * 사용 예시:
     * List<StockTerm> popular = mapper.findPopularTerms(10);
     * // 조회수가 가장 많은 10개의 용어
     */
    List<StockTerm> findPopularTerms(@Param("limit") Integer limit);
}


/*
 * 💡 @Param 어노테이션이란?
 * - XML에서 파라미터를 사용할 때 #{paramName} 형태로 접근하기 위한 이름 지정
 * - 예: @Param("categoryId") Integer categoryId
 *   → XML에서 #{categoryId}로 사용 가능
 */
