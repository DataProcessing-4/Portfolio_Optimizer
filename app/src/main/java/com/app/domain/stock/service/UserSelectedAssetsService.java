package com.app.domain.stock.service;

import com.app.domain.stock.dto.AssetSelectionRequest;
import com.app.domain.stock.dto.AssetSelectionResponse;  // 👈 변경됨
import com.app.domain.stock.entity.UserSelectedAssets;
import com.app.domain.stock.mapper.StockMapper;
import com.app.domain.stock.mapper.UserSelectedAssetsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSelectedAssetsService {

    // ===== 의존성 주입 =====
    private final UserSelectedAssetsMapper userSelectedAssetsMapper;
    private final StockMapper stockMapper;  // 주식 정보 조회용

    // ===== 상수 정의 =====
    private static final int MIN_SELECTION_COUNT = 5;   // 최소 선택 개수
    private static final int MAX_SELECTION_COUNT = 10;  // 최대 선택 개수

    /**
     * 자산 선택 추가
     * - 비즈니스 규칙: 5~10개 제한, 중복 방지, 존재하는 주식인지 검증
     */
    @Transactional  // 트랜잭션 관리 (실패시 롤백)
    public AssetSelectionResponse addSelectedAsset(String sessionId, AssetSelectionRequest request) {

        log.info("자산 선택 추가 시작 - 세션: {}, 티커: {}", sessionId, request.getTicker());

        // ===== 1. 선택 개수 제한 확인 =====
        int currentCount = userSelectedAssetsMapper.countSelectedAssets(sessionId);
        if (currentCount >= MAX_SELECTION_COUNT) {
            throw new IllegalStateException(
                    String.format("최대 %d개까지만 선택할 수 있습니다. (현재: %d개)",
                            MAX_SELECTION_COUNT, currentCount));
        }

        // ===== 2. 중복 선택 방지 =====
        if (userSelectedAssetsMapper.isAssetSelected(sessionId, request.getTicker())) {
            throw new IllegalArgumentException("이미 선택된 종목입니다: " + request.getTicker());
        }

        // ===== 3. 주식 존재 여부 확인 =====
        if (stockMapper.selectStockByTicker(request.getTicker()) == null) {
            throw new IllegalArgumentException("존재하지 않는 종목입니다: " + request.getTicker());
        }

        // ===== 4. 선택 순서 결정 =====
        Integer selectionOrder = request.getSelectionOrder();
        if (selectionOrder == null) {
            selectionOrder = userSelectedAssetsMapper.getNextSelectionOrder(sessionId);
        }

        // ===== 5. 엔티티 생성 및 저장 =====
        UserSelectedAssets selectedAsset = UserSelectedAssets.builder()
                .sessionId(sessionId)
                .ticker(request.getTicker())
                .selectionOrder(selectionOrder)
                .selectedAt(LocalDateTime.now())
                .build();

        int insertedCount = userSelectedAssetsMapper.insertSelectedAsset(selectedAsset);
        if (insertedCount == 0) {
            throw new RuntimeException("자산 선택 저장에 실패했습니다.");
        }

        // ===== 6. 저장된 데이터 조회 및 응답 생성 =====
        List<UserSelectedAssets> assets = userSelectedAssetsMapper.selectAssetsBySession(sessionId);
        AssetSelectionResponse response = assets.stream()
                .filter(asset -> asset.getTicker().equals(request.getTicker()))
                .findFirst()
                .map(this::convertToResponse)  // Entity -> DTO 변환
                .orElseThrow(() -> new RuntimeException("저장된 데이터를 조회할 수 없습니다."));

        log.info("자산 선택 추가 완료 - 티커: {}, 순서: {}", response.getTicker(), response.getSelectionOrder());
        return response;
    }

    /**
     * 선택된 자산 목록 조회
     */
    @Transactional(readOnly = true)  // 읽기 전용 트랜잭션
    public List<AssetSelectionResponse> getSelectedAssets(String sessionId) {

        log.debug("선택된 자산 목록 조회 - 세션: {}", sessionId);

        List<UserSelectedAssets> assets = userSelectedAssetsMapper.selectAssetsBySession(sessionId);
        return assets.stream()
                .map(this::convertToResponse)  // Entity -> DTO 변환
                .collect(Collectors.toList());
    }

    /**
     * 특정 자산 선택 취소
     */
    @Transactional
    public boolean removeSelectedAsset(String sessionId, String ticker) {

        log.info("자산 선택 취소 - 세션: {}, 티커: {}", sessionId, ticker);

        // ===== 1. 선택된 종목인지 확인 =====
        if (!userSelectedAssetsMapper.isAssetSelected(sessionId, ticker)) {
            throw new IllegalArgumentException("선택되지 않은 종목입니다: " + ticker);
        }

        // ===== 2. 선택 삭제 =====
        int deletedCount = userSelectedAssetsMapper.deleteSelectedAsset(sessionId, ticker);
        boolean success = deletedCount > 0;

        log.info("자산 선택 취소 결과 - 티커: {}, 성공: {}", ticker, success);
        return success;
    }

    /**
     * 모든 선택 초기화
     */
    @Transactional
    public boolean clearAllSelectedAssets(String sessionId) {

        log.info("모든 자산 선택 초기화 - 세션: {}", sessionId);

        int deletedCount = userSelectedAssetsMapper.deleteAllSelectedAssets(sessionId);

        log.info("자산 선택 초기화 완료 - 삭제된 개수: {}", deletedCount);
        return deletedCount > 0;
    }

    /**
     * 선택된 자산 개수 조회
     */
    @Transactional(readOnly = true)
    public int getSelectedAssetCount(String sessionId) {
        return userSelectedAssetsMapper.countSelectedAssets(sessionId);
    }

    /**
     * 선택 완료 가능 여부 확인
     * - 비즈니스 규칙: 5개 이상 10개 이하
     */
    @Transactional(readOnly = true)
    public boolean isSelectionComplete(String sessionId) {
        int count = getSelectedAssetCount(sessionId);
        return count >= MIN_SELECTION_COUNT && count <= MAX_SELECTION_COUNT;
    }

    /**
     * 선택 순서 업데이트
     */
    @Transactional
    public boolean updateSelectionOrder(String sessionId, String ticker, Integer newOrder) {

        log.info("선택 순서 업데이트 - 세션: {}, 티커: {}, 새 순서: {}", sessionId, ticker, newOrder);

        // ===== 1. 선택된 종목인지 확인 =====
        if (!userSelectedAssetsMapper.isAssetSelected(sessionId, ticker)) {
            throw new IllegalArgumentException("선택되지 않은 종목입니다: " + ticker);
        }

        // ===== 2. 순서 범위 검증 =====
        if (newOrder < 1 || newOrder > MAX_SELECTION_COUNT) {
            throw new IllegalArgumentException(
                    String.format("선택 순서는 1~%d 범위여야 합니다.", MAX_SELECTION_COUNT));
        }

        // ===== 3. 순서 업데이트 =====
        int updatedCount = userSelectedAssetsMapper.updateSelectionOrder(sessionId, ticker, newOrder);
        return updatedCount > 0;
    }

    /**
     * 엔티티를 응답 DTO로 변환
     * - Entity의 모든 정보를 클라이언트 친화적 형태로 변환
     */
    private AssetSelectionResponse convertToResponse(UserSelectedAssets asset) {
        return AssetSelectionResponse.builder()
                .selectionId(asset.getSelectionId())
                .ticker(asset.getTicker())
                .stockName(asset.getStockName())
                .industry(asset.getIndustry())
                .closePrice(asset.getClosePrice())
                .per(asset.getPer())
                .pbr(asset.getPbr())
                .roe(asset.getRoe())
                .selectionOrder(asset.getSelectionOrder())
                .selectedAt(asset.getSelectedAt())
                .build();
    }













}//class
