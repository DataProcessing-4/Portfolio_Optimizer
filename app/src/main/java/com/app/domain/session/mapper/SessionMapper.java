package com.app.domain.session.mapper;

import com.app.domain.session.entity.UserSession;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SessionMapper {

    /**
     * 새로운 세션을 DB에 삽입
     */
    int insertSession(UserSession session);

    /**
     * 세션 ID로 세션 조회
     */
    UserSession findBySessionId(String sessionId);

    /**
     * 세션 마지막 접근 시간 업데이트
     */
    int updateLastAccessed(String sessionId);

    /**
     * 세션 무효화
     */
    int invalidateSession(String sessionId);

    /**
     * 모든 활성 세션 조회
     */
    List<UserSession> findAllActiveSessions();

    /**
     * 만료된 세션들 제거
     */
    int cleanupExpiredSessions();

    /**
     * 세션 존재 여부 확인
     */
    boolean existsBySessionId(String sessionId);
}