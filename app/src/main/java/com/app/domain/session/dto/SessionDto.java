package com.app.domain.session.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


public class SessionDto {


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String userIp;
        private String userAgent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String sessionId;
        private String userIp;
        private LocalDateTime createdAt;
        private LocalDateTime lastAccessed;
        private boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String sessionId;
        private String userIp;
        private String userAgent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private String sessionId;
        private String userIp;
        private LocalDateTime createdAt;
        private LocalDateTime lastAccessed;
        private boolean isActive;
    }



}//class
