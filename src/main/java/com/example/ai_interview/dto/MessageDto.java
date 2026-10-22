package com.example.ai_interview.dto;

import java.time.OffsetDateTime;

/** 对话消息 */
public record MessageDto(
        Long id,
        /** interviewer / candidate */
        String role,
        String content,
        /** 单题评分（仅 candidate 消息有值） */
        Integer score,
        /** 关联题库题 id（AI 项目追问为 null） */
        Long questionId,
        OffsetDateTime createdAt) {
}
