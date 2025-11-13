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











}//class
