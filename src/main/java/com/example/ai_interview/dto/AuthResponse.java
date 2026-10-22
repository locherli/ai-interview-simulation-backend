package com.example.ai_interview.dto;

/** 认证响应：JWT + 用户基本信息 */
public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String email) {

    public static AuthResponse of(String token, Long userId, String email) {
        return new AuthResponse(token, "Bearer", userId, email);
    }
}
