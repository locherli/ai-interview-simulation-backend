package com.example.ai_interview.dto;

import java.time.OffsetDateTime;

/** 面试会话信息 */
public record InterviewDto(
        Long id,
        String jobTitle,
        String category,
        Integer difficulty,
        String status,
        Integer score,
        String summary,
        OffsetDateTime createdAt,
        OffsetDateTime finishedAt) {
}
