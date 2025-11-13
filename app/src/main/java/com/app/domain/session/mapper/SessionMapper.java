package com.app.domain.session.mapper;

import com.app.domain.session.entity.UserSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SessionMapper {


    /**
     * 새로운 세션 생성
     */
    int insertSession(UserSession userSession);

    /**
     * 세션 ID로 세션 조회
     */
    UserSession findBySessionId(@Param("sessionId") String sessionId);

    /**
     * 세션 마지막 접근 시간 업데이트 (MySQL 자동 갱신 트리거용)
     */
    int updateLastAccessed(@Param("sessionId") String sessionId);

    /**
     * 세션 비활성화
     */
    int deactivateSession(@Param("sessionId") String sessionId);

    /**
     * 만료된 세션 정리 (24시간 이상 비활성)
     */
    int cleanupExpiredSessions(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 사용자 IP별 활성 세션 조회
     */
    List<UserSession> findActiveSessionsByUserIp(@Param("userIp") String userIp);

    /**
     * 세션 존재 여부 확인
     */
    boolean existsBySessionId(@Param("sessionId") String sessionId);



}//class
