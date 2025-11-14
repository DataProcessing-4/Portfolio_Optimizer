package com.app.domain.stock.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 페이지네이션 결과 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponseDto<T> {

    private List<T> content;        // 실제 데이터 리스트
    private int currentPage;        // 현재 페이지 번호
    private int pageSize;           // 페이지 크기
    private long totalElements;     // 전체 데이터 개수
    private int totalPages;         // 전체 페이지 수
    private boolean hasNext;        // 다음 페이지 존재 여부
    private boolean hasPrevious;    // 이전 페이지 존재 여부

    public static <T> PageResponseDto<T> of(List<T> content, int currentPage,
                                            int pageSize, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        return PageResponseDto.<T>builder()
                .content(content)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(currentPage < totalPages)
                .hasPrevious(currentPage > 1)
                .build();
    }





}//class
