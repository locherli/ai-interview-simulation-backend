package com.example.ai_interview.dto;

import java.time.OffsetDateTime;

/** 用户信息 */
public record UserDto(
        Long id,
        String email,
        OffsetDateTime createdAt) {
}
