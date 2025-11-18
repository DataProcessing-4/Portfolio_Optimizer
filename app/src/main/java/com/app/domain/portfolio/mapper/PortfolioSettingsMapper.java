package com.app.domain.portfolio.mapper;


import com.app.domain.portfolio.entity.PortfolioSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 포트폴리오 설정 Mapper
 */
@Mapper
public interface PortfolioSettingsMapper {


    /**
     * 세션 ID로 설정 조회
     * @param sessionId 세션 ID
     * @return 포트폴리오 설정 (없으면 null)
     */
    PortfolioSettings findBySessionId(@Param("sessionId") String sessionId);

    /**
     * 새로운 설정 추가
     * @param settings 포트폴리오 설정
     * @return 삽입된 행 수
     */
    int insert(PortfolioSettings settings);

    /**
     * 기존 설정 업데이트
     * @param settings 포트폴리오 설정
     * @return 업데이트된 행 수
     */
    int update(PortfolioSettings settings);

    /**
     * 세션 ID로 설정 삭제
     * @param sessionId 세션 ID
     * @return 삭제된 행 수
     */
    int deleteBySessionId(@Param("sessionId") String sessionId);

    /**
     * 설정 ID로 조회
     * @param settingId 설정 ID
     * @return 포트폴리오 설정 (없으면 null)
     */
    PortfolioSettings findById(@Param("settingId") Long settingId);



}//interface
