package com.app.app.global.util;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionUtil {


    private final JdbcTemplate jdbcTemplate;

    // 세션 속성 키
    private static final String BUSINESS_SESSION_KEY = "businessSessionId";

    /**
     * HTTP 세션에서 비즈니스 세션 ID 추출
     * 없으면 생성하고 DB에도 저장
     */
    public String getBusinessSessionId(HttpSession httpSession) {
        String businessSessionId = (String) httpSession.getAttribute(BUSINESS_SESSION_KEY);

        if (businessSessionId == null) {
            // 비즈니스 세션 ID 생성
            businessSessionId = generateSessionId();

            // HTTP 세션에 저장
            httpSession.setAttribute(BUSINESS_SESSION_KEY, businessSessionId);

            // DB에 세션 정보 저장 (반드시 성공해야 함)
            saveSessionToDatabase(businessSessionId);

            log.info("새 비즈니스 세션 생성 및 DB 저장: {}", businessSessionId);
        } else {
            // 기존 세션이 DB에 존재하는지 확인
            ensureSessionExistsInDatabase(businessSessionId);
        }

        return businessSessionId;
    }

    /**
     * 세션 ID 생성
     */
    private String generateSessionId() {
        return "SES_" + System.currentTimeMillis() + "_"
                + String.format("%06d", (int)(Math.random() * 1000000));
    }

    /**
     * user_session 테이블에 세션 저장
     * 실패 시 예외 발생
     */
    private void saveSessionToDatabase(String sessionId) {
        try {
            String checkSql = "SELECT COUNT(*) FROM user_session WHERE session_id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, sessionId);

            if (count != null && count > 0) {
                log.debug("세션이 이미 존재함: {}", sessionId);
                return;
            }

            String insertSql = "INSERT INTO user_session (session_id, user_ip, user_agent, created_at, last_accessed, is_active) " +
                    "VALUES (?, ?, ?, NOW(), NOW(), TRUE)";

            int rowsAffected = jdbcTemplate.update(insertSql, sessionId, "unknown", "unknown");

            if (rowsAffected > 0) {
                log.info("세션 DB 저장 완료: {}", sessionId);
            } else {
                log.error("세션 DB 저장 실패: 영향받은 행 없음 - {}", sessionId);
                throw new RuntimeException("세션 저장에 실패했습니다.");
            }

        } catch (DataAccessException e) {
            log.error("세션 DB 저장 중 데이터베이스 오류: sessionId={}, error={}", sessionId, e.getMessage());
            throw new RuntimeException("세션 저장에 실패했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("세션 DB 저장 중 알 수 없는 오류: sessionId={}, error={}", sessionId, e.getMessage());
            throw new RuntimeException("세션 저장에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 세션이 DB에 존재하는지 확인하고, 없으면 생성
     */
    private void ensureSessionExistsInDatabase(String sessionId) {
        try {
            String sql = "SELECT COUNT(*) FROM user_session WHERE session_id = ? AND is_active = TRUE";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, sessionId);

            if (count == null || count == 0) {
                log.warn("세션이 DB에 존재하지 않음. 재생성: {}", sessionId);
                saveSessionToDatabase(sessionId);
            } else {
                // 마지막 접근 시간 업데이트
                String updateSql = "UPDATE user_session SET last_accessed = NOW() WHERE session_id = ?";
                jdbcTemplate.update(updateSql, sessionId);
            }
        } catch (Exception e) {
            log.error("세션 확인/업데이트 중 오류: {}", e.getMessage());
            // 세션 확인 실패 시 재생성 시도
            saveSessionToDatabase(sessionId);
        }
    }

    /**
     * 세션 유효성 확인
     */
    public boolean isValidSession(String sessionId) {
        try {
            String sql = "SELECT COUNT(*) FROM user_session WHERE session_id = ? AND is_active = TRUE";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, sessionId);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("세션 유효성 확인 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 세션 삭제 (비활성화)
     */
    public void deactivateSession(String sessionId) {
        try {
            String sql = "UPDATE user_session SET is_active = FALSE WHERE session_id = ?";
            jdbcTemplate.update(sql, sessionId);
            log.info("세션 비활성화: {}", sessionId);
        } catch (Exception e) {
            log.error("세션 비활성화 실패: {}", e.getMessage());
        }
    }



}//class
