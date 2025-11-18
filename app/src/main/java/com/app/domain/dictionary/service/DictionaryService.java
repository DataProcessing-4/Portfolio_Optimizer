package com.app.domain.dictionary.service;

import com.app.domain.dictionary.dto.*;
import com.app.domain.dictionary.entity.StockTerm;
import com.app.domain.dictionary.entity.StockTermCategory;
import com.app.domain.dictionary.mapper.DictionaryMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryService {

    private final DictionaryMapper dictionaryMapper;
    private final ObjectMapper objectMapper;

    /**
     * 카테고리 트리 구조 조회
     */
    public List<CategoryTreeDto> getCategoryTree() {
        log.info("카테고리 트리 구조 조회 시작");
        
        List<StockTermCategory> allCategories = dictionaryMapper.findAllCategories();
        log.debug("전체 카테고리 수: {}", allCategories.size());
        
        Map<Integer, CategoryTreeDto> categoryMap = new HashMap<>();
        
        for (StockTermCategory category : allCategories) {
            CategoryTreeDto dto = CategoryTreeDto.builder()
                    .categoryId(category.getCategoryId())
                    .categoryName(category.getCategoryName())
                    .description(category.getDescription())
                    .parentCategoryId(category.getParentCategoryId())
                    .sortOrder(category.getSortOrder())
                    .children(new ArrayList<>())
                    .build();
            
            categoryMap.put(category.getCategoryId(), dto);
        }
        
        List<CategoryTreeDto> rootCategories = new ArrayList<>();
        
        for (CategoryTreeDto dto : categoryMap.values()) {
            if (dto.getParentCategoryId() == null) {
                rootCategories.add(dto);
            } else {
                CategoryTreeDto parent = categoryMap.get(dto.getParentCategoryId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }
        
        sortCategoryTree(rootCategories);
        
        log.info("카테고리 트리 구조 조회 완료: 최상위 카테고리 {}개", rootCategories.size());
        return rootCategories;
    }
    
    private void sortCategoryTree(List<CategoryTreeDto> categories) {
        categories.sort(Comparator
                .comparing(CategoryTreeDto::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(CategoryTreeDto::getCategoryName));
        
        for (CategoryTreeDto category : categories) {
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                sortCategoryTree(category.getChildren());
            }
        }
    }

    /**
     * 특정 카테고리의 용어 목록 조회
     */
    public CategoryTermsDto getTermsByCategory(Integer categoryId) {
        log.info("카테고리별 용어 조회: categoryId={}", categoryId);
        
        StockTermCategory category = dictionaryMapper.findCategoryById(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId);
        }
        
        List<StockTerm> terms = dictionaryMapper.findTermsByCategory(categoryId);
        
        List<TermListDto> termDtos = terms.stream()
                .map(term -> TermListDto.builder()
                        .termId(term.getTermId())
                        .termName(term.getTermName())
                        .termEnglish(term.getTermEnglish())
                        .definition(term.getDefinition())
                        .categoryName(category.getCategoryName())
                        .difficultyLevel(term.getDifficultyLevel())
                        .build())
                .collect(Collectors.toList());
        
        CategoryTermsDto result = CategoryTermsDto.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .termCount(termDtos.size())
                .terms(termDtos)
                .build();
        
        log.info("카테고리별 용어 조회 완료: {}개 용어", termDtos.size());
        return result;
    }

    /**
     * 용어 상세 정보 조회
     */
    @Transactional
    public TermDetailDto getTermDetail(Integer termId) {
        log.info("용어 상세 조회: termId={}", termId);
        
        StockTerm term = dictionaryMapper.findTermById(termId);
        if (term == null) {
            throw new IllegalArgumentException("용어를 찾을 수 없습니다: " + termId);
        }
        
        StockTermCategory category = dictionaryMapper.findCategoryById(term.getCategoryId());
        
        List<String> relatedTermsList = parseRelatedTerms(term.getRelatedTerms());
        
        TermDetailDto result = TermDetailDto.builder()
                .termId(term.getTermId())
                .termName(term.getTermName())
                .termEnglish(term.getTermEnglish())
                .definition(term.getDefinition())
                .detailedExplanation(term.getDetailedExplanation())
                .relatedTerms(relatedTermsList)
                .exampleText(term.getExampleText())
                .imagePath(term.getImagePath())
                .referenceUrl(term.getReferenceUrl())
                .difficultyLevel(term.getDifficultyLevel())
                .categoryName(category != null ? category.getCategoryName() : null)
                .viewCount(term.getViewCount())
                .build();
        
        try {
            dictionaryMapper.incrementViewCount(termId);
            log.debug("조회수 증가: termId={}, 기존 조회수={}", termId, term.getViewCount());
        } catch (Exception e) {
            log.error("조회수 증가 실패: termId={}", termId, e);
        }
        
        log.info("용어 상세 조회 완료: {}", term.getTermName());
        return result;
    }

    /**
     * 용어 검색
     */
    public List<TermListDto> searchTerms(String keyword) {
        log.info("용어 검색: keyword={}", keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("검색 키워드가 비어있음");
            return Collections.emptyList();
        }
        
        List<StockTerm> terms = dictionaryMapper.searchTerms(keyword.trim());
        
        List<TermListDto> result = terms.stream()
                .map(term -> {
                    StockTermCategory category = dictionaryMapper.findCategoryById(term.getCategoryId());
                    return TermListDto.builder()
                            .termId(term.getTermId())
                            .termName(term.getTermName())
                            .termEnglish(term.getTermEnglish())
                            .definition(term.getDefinition())
                            .categoryName(category != null ? category.getCategoryName() : null)
                            .difficultyLevel(term.getDifficultyLevel())
                            .build();
                })
                .collect(Collectors.toList());
        
        log.info("용어 검색 완료: {}개 결과", result.size());
        return result;
    }

    /**
     * 인기 용어 조회
     */
    public List<TermListDto> getPopularTerms(Integer limit) {
        log.info("인기 용어 조회: limit={}", limit);
        
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        
        List<StockTerm> terms = dictionaryMapper.findPopularTerms(limit);
        
        List<TermListDto> result = terms.stream()
                .map(term -> {
                    StockTermCategory category = dictionaryMapper.findCategoryById(term.getCategoryId());
                    return TermListDto.builder()
                            .termId(term.getTermId())
                            .termName(term.getTermName())
                            .termEnglish(term.getTermEnglish())
                            .definition(term.getDefinition())
                            .categoryName(category != null ? category.getCategoryName() : null)
                            .difficultyLevel(term.getDifficultyLevel())
                            .build();
                })
                .collect(Collectors.toList());
        
        log.info("인기 용어 조회 완료: {}개", result.size());
        return result;
    }

    /**
     * 전체 용어 목록 조회
     */
    public List<TermListDto> getAllTerms() {
        log.info("전체 용어 목록 조회");
        
        List<StockTerm> terms = dictionaryMapper.findAllTerms();
        
        List<TermListDto> result = terms.stream()
                .map(term -> {
                    StockTermCategory category = dictionaryMapper.findCategoryById(term.getCategoryId());
                    return TermListDto.builder()
                            .termId(term.getTermId())
                            .termName(term.getTermName())
                            .termEnglish(term.getTermEnglish())
                            .definition(term.getDefinition())
                            .categoryName(category != null ? category.getCategoryName() : null)
                            .difficultyLevel(term.getDifficultyLevel())
                            .build();
                })
                .collect(Collectors.toList());
        
        log.info("전체 용어 목록 조회 완료: {}개", result.size());
        return result;
    }

    /**
     * JSON 형식의 연관 용어를 List로 변환
     */
    private List<String> parseRelatedTerms(String relatedTermsJson) {
        if (relatedTermsJson == null || relatedTermsJson.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            return objectMapper.readValue(
                    relatedTermsJson, 
                    new TypeReference<List<String>>() {}
            );
        } catch (Exception e) {
            log.error("연관 용어 JSON 파싱 실패: {}", relatedTermsJson, e);
            return Collections.emptyList();
        }
    }
}