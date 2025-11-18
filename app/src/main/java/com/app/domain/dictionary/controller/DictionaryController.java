package com.app.domain.dictionary.controller;

import com.app.app.global.common.ApiResponse;
import com.app.domain.dictionary.dto.CategoryTermsDto;
import com.app.domain.dictionary.dto.CategoryTreeDto;
import com.app.domain.dictionary.dto.TermDetailDto;
import com.app.domain.dictionary.dto.TermListDto;
import com.app.domain.dictionary.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dictionary")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;

    /**
     * API 1: 카테고리 트리 구조 조회
     * GET /api/dictionary/categories
     */
    @GetMapping("/categories")
    public ApiResponse getCategoryTree() {
        log.info("API 호출: 카테고리 트리 조회");
        List<CategoryTreeDto> categories = dictionaryService.getCategoryTree();
        return ApiResponse.success(categories);
    }

    /**
     * API 2: 특정 카테고리의 용어 목록 조회
     * GET /api/dictionary/categories/{categoryId}/terms
     */
    @GetMapping("/categories/{categoryId}/terms")
    public ApiResponse getTermsByCategory(@PathVariable Integer categoryId) {
        log.info("API 호출: 카테고리별 용어 조회, categoryId={}", categoryId);
        CategoryTermsDto result = dictionaryService.getTermsByCategory(categoryId);
        return ApiResponse.success(result);
    }

    /**
     * API 3: 용어 상세 정보 조회
     * GET /api/dictionary/terms/{termId}
     */
    @GetMapping("/terms/{termId}")
    public ApiResponse getTermDetail(@PathVariable Integer termId) {
        log.info("API 호출: 용어 상세 조회, termId={}", termId);
        TermDetailDto result = dictionaryService.getTermDetail(termId);
        return ApiResponse.success(result);
    }

    /**
     * API 4: 용어 검색
     * GET /api/dictionary/search?keyword=포트폴리오
     */
    @GetMapping("/search")
    public ApiResponse searchTerms(@RequestParam String keyword) {
        log.info("API 호출: 용어 검색, keyword={}", keyword);
        List<TermListDto> results = dictionaryService.searchTerms(keyword);
        return ApiResponse.success(results);
    }

    /**
     * API 5: 인기 용어 조회
     * GET /api/dictionary/popular?limit=10
     */
    @GetMapping("/popular")
    public ApiResponse getPopularTerms(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        log.info("API 호출: 인기 용어 조회, limit={}", limit);
        List<TermListDto> results = dictionaryService.getPopularTerms(limit);
        return ApiResponse.success(results);
    }

    /**
     * API 6: 전체 용어 목록 조회
     * GET /api/dictionary/terms
     */
    @GetMapping("/terms")
    public ApiResponse getAllTerms() {
        log.info("API 호출: 전체 용어 목록 조회");
        List<TermListDto> results = dictionaryService.getAllTerms();
        return ApiResponse.success(results);
    }
}