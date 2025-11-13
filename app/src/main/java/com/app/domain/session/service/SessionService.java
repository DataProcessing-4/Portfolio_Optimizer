package com.app.domain.session.service;


import com.app.domain.session.dto.SessionDto;
import com.app.domain.session.entity.UserSession;
import com.app.domain.session.mapper.SessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SessionService {



    private final SessionMapper sessionMapper;

    /**
     * 새로운 세션 생성
     */
    @Transactional
    public SessionDto.Response createSession(SessionDto.CreateRequest request) {
        String sessionId = generateSessionId();

        UserSession userSession = UserSession.builder()
                .sessionId(sessionId)
                .userIp(request.getUserIp())
                .userAgent(request.getUserAgent())
                .isActive(true)
                .build();

        sessionMapper.insertSession(userSession);

        log.info("새로운 세션 생성: sessionId={}, userIp={}", sessionId, request.getUserIp());

        // MySQL에서 자동 생성된 timestamp 포함한 정보 조회
        UserSession created = sessionMapper.findBySessionId(sessionId);
        return convertToResponse(created);
    }

    /**
     * 세션 조회 및 마지막 접근 시간 업데이트
     * MySQL의 ON UPDATE CURRENT_TIMESTAMP 활용
     */
    @Transactional
    public SessionDto.Response getSessionAndUpdateAccess(String sessionId) {
        UserSession session = sessionMapper.findBySessionId(sessionId);

        if (session == null || !session.isActive()) {
            log.warn("유효하지 않은 세션: sessionId={}", sessionId);
            return null;
        }

        // 마지막 접근 시간 업데이트 (MySQL 자동 갱신 트리거)
        sessionMapper.updateLastAccessed(sessionId);

        // 업데이트된 세션 정보 반환
        UserSession updated = sessionMapper.findBySessionId(sessionId);
        return convertToResponse(updated);
    }

    /**
     * 세션 유효성 검증
     */
    public boolean isValidSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }
        return sessionMapper.existsBySessionId(sessionId);
    }

    /**
     * 세션 비활성화
     */
    @Transactional
    public boolean deactivateSession(String sessionId) {
        int result = sessionMapper.deactivateSession(sessionId);
        log.info("세션 비활성화: sessionId={}, result={}", sessionId, result);
        return result > 0;
    }

    /**
     * 만료된 세션 정리 (24시간 이상 비활성)
     */
    @Transactional
    public int cleanupExpiredSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        int cleaned = sessionMapper.cleanupExpiredSessions(cutoffTime);
        log.info("만료된 세션 정리 완료: {} 개", cleaned);
        return cleaned;
    }

    /**
     * 세션 ID 생성
     */
    private String generateSessionId() {
        return "SES_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * UserSession을 SessionDto.Response로 변환
     */
    private SessionDto.Response convertToResponse(UserSession session) {
        return SessionDto.Response.builder()
                .sessionId(session.getSessionId())
                .userIp(session.getUserIp())
                .createdAt(session.getCreatedAt())
                .lastAccessed(session.getLastAccessed())
                .isActive(session.isActive())
                .build();
    }




}//class
