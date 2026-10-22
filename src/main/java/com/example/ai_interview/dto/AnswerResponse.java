package com.example.ai_interview.dto;

/** AI 单题评分响应 */
public record AnswerResponse(
        /** 本题得分 0~100 */
        Integer score,
        /** AI 点评 */
        String feedback,
        /** 已回答题数 */
        int answeredCount,
        /** 本场面试总题数 */
        int totalQuestions) {
}
