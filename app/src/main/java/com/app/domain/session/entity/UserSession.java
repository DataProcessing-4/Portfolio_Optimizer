package com.app.domain.session.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    private String sessionId;      // session_id
    private String userIp;         // user_ip
    private String userAgent;      // user_agent
    private LocalDateTime createdAt;      // created_at
    private LocalDateTime lastAccessed;   // last_accessed
    private boolean isActive;      // is_active


//    //---25.11.19 추가 필드---
//    /**
//     * 세션 만료 여부 확인
//     * @param expirationHours 만료 시간 (시간 단위)
//     * @return 만료 여부
//     */
//    public boolean isExpired(int expirationHours) {
//        if (lastAccessed == null) {
//            return true;
//        }
//        return lastAccessed.plusHours(expirationHours).isBefore(LocalDateTime.now());
//    }
//
//    /**
//     * 세션 활성화
//     */
//    public void activate() {
//        this.isActive = true;
//        this.lastAccessed = LocalDateTime.now();
//    }
//
//    /**
//     * 세션 비활성화
//     */
//    public void deactivate() {
//        this.isActive = false;
//    }









}//class
