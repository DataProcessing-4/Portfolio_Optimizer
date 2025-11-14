package com.app.domain.stock.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


/**
 * 자산 선택 요청 DTO
 * - 클라이언트에서 서버로 자산 선택 요청을 보낼 때 사용
 * - Validation 어노테이션으로 데이터 검증
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetSelectionRequest {

    @NotBlank(message = "티커는 필수입니다")
    @Size(max = 10, message = "티커는 10자를 초과할 수 없습니다")
    private String ticker;              // 선택할 티커 심볼

    @Min(value = 1, message = "선택 순서는 1 이상이어야 합니다")
    @Max(value = 10, message = "선택 순서는 10 이하여야 합니다")
    private Integer selectionOrder;     // 선택 순서 (선택사항, 없으면 자동 부여)

    // 사용 예시:
    // {
    //   "ticker": "AAPL",
    //   "selectionOrder": 1
    // }


}//class
