package com.example.ai_interview.security;

/** 已认证用户主体（从 JWT 解析，不查库） */
public record LoginUser(Long id, String email) {
}
