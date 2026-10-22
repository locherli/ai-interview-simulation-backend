package com.example.ai_interview.dto;

/** 获取下一题响应 */
public record NextQuestionResponse(
        /** 面试官消息 id */
        Long messageId,
        /** 当前第几题（从 1 开始） */
        int questionNo,
        /** 本场面试总题数 */
        int totalQuestions,
        /** 题目类型：bank=题库题 / project=AI 项目追问 */
        String type,
        /** 题目内容 */
        String content) {
}
