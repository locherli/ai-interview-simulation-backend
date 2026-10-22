package com.example.ai_interview.ai;

import java.util.List;

/**
 * AI 能力抽象：单题评分、项目追问生成、面试总结报告。
 * 默认提供 OpenAI 兼容实现（OpenAiClient）与本地启发式实现（MockAiClient）。
 */
public interface AiClient {

    /** 单题评分结果 */
    record ScoreResult(int score, String feedback) {
    }

    /** 问答对（用于给 AI 提供上下文） */
    record QaPair(String question, String answer) {
    }

    /** 带分数的问答记录（用于生成总结报告） */
    record QaScore(String question, String answer, Integer score) {
    }

    /**
     * 对候选人回答进行评分。
     *
     * @param jobTitle 应聘岗位
     * @param question 面试题目
     * @param answer   候选人回答
     * @return 0~100 的分数与点评
     */
    ScoreResult scoreAnswer(String jobTitle, String question, String answer);

    /**
     * 生成一个针对"个人项目经历"的面试问题。
     *
     * @param jobTitle          应聘岗位
     * @param previousProjectQa 已问过的项目问答（避免重复提问）
     */
    String generateProjectQuestion(String jobTitle, List<QaPair> previousProjectQa);

    /**
     * 根据完整问答记录生成面试总结报告。
     *
     * @param jobTitle    应聘岗位
     * @param transcript  全部问答记录（含单题得分）
     * @param overallScore 各题平均分（供 AI 参考）
     */
    String generateSummary(String jobTitle, List<QaScore> transcript, int overallScore);
}
