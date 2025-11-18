package com.app.domain.session.mapper;

import com.app.domain.session.entity.UserSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    //------25.11.19 추가------//
//
//    /**
//     * HTTP 세션 ID로 비즈니스 세션 ID 조회
//     * @param httpSessionId Spring Security가 생성한 HTTP 세션 ID (JSESSIONID)
//     * @return 비즈니스 세션 ID (SES_xxx 형식)
//     */
//    String findBusinessSessionIdByHttpSessionId(@Param("httpSessionId") String httpSessionId);
//
//    /**
//     * 비즈니스 세션 ID로 HTTP 세션 ID 조회
//     * @param businessSessionId 비즈니스 세션 ID (SES_xxx)
//     * @return HTTP 세션 ID
//     */
//    String findHttpSessionIdByBusinessSessionId(@Param("businessSessionId") String businessSessionId);



}//interface