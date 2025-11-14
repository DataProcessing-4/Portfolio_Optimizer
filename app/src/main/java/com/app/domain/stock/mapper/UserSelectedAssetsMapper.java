package com.app.domain.stock.mapper;

import com.app.domain.stock.entity.UserSelectedAssets;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 사용자 선택 자산 Mapper
 * - MyBatis 인터페이스
 * - 순수 데이터 CRUD만 담당 (비즈니스 로직 포함 안함)
 * - SQL과 자바 메서드를 매핑
 */
@Mapper
public interface UserSelectedAssetsMapper {


// ===== 등록(Create) =====
    /**
     * 선택된 자산 추가
     * @param userSelectedAssets 저장할 선택 정보
     * @return 저장된 레코드 수
     */
    int insertSelectedAsset(UserSelectedAssets userSelectedAssets);

    // ===== 조회(Read) =====
    /**
     * 세션별 선택된 자산 목록 조회 (주식 정보 포함)
     * @param sessionId 세션 ID
     * @return 선택된 자산 목록
     */
    List<UserSelectedAssets> selectAssetsBySession(@Param("sessionId") String sessionId);

    /**
     * 세션별 선택된 자산 개수 조회
     * @param sessionId 세션 ID
     * @return 선택된 자산 개수
     */
    int countSelectedAssets(@Param("sessionId") String sessionId);

    /**
     * 특정 자산 선택 여부 확인
     * @param sessionId 세션 ID
     * @param ticker 티커 심볼
     * @return 선택 여부 (true/false)
     */
    boolean isAssetSelected(@Param("sessionId") String sessionId, @Param("ticker") String ticker);

    /**
     * 세션별 다음 선택 순서 조회
     * @param sessionId 세션 ID
     * @return 다음 선택 순서
     */
    Integer getNextSelectionOrder(@Param("sessionId") String sessionId);

    // ===== 수정(Update) =====
    /**
     * 선택 순서 업데이트
     * @param sessionId 세션 ID
     * @param ticker 티커 심볼
     * @param selectionOrder 새로운 선택 순서
     * @return 수정된 레코드 수
     */
    int updateSelectionOrder(@Param("sessionId") String sessionId,
                             @Param("ticker") String ticker,
                             @Param("selectionOrder") Integer selectionOrder);

    // ===== 삭제(Delete) =====
    /**
     * 특정 자산 선택 삭제
     * @param sessionId 세션 ID
     * @param ticker 티커 심볼
     * @return 삭제된 레코드 수
     */
    int deleteSelectedAsset(@Param("sessionId") String sessionId, @Param("ticker") String ticker);

    /**
     * 세션별 모든 선택 삭제
     * @param sessionId 세션 ID
     * @return 삭제된 레코드 수
     */
    int deleteAllSelectedAssets(@Param("sessionId") String sessionId);






}//interface
