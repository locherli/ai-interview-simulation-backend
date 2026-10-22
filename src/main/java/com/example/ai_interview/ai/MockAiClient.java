package com.example.ai_interview.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 内置 Mock AI 实现：不调用任何外部服务，使用启发式规则评分。
 * 在 ai.api-key 未配置时使用，同时也作为 OpenAiClient 调用失败时的降级方案。
 */
public class MockAiClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(MockAiClient.class);

    private static final String[] PROJECT_QUESTIONS = {
            "请介绍一个你最有代表性的个人项目：它解决了什么问题？你在其中承担什么角色、负责哪些模块？",
            "在这个项目中，你遇到的最大技术难点是什么？你是如何分析并解决的？",
            "这个项目的整体架构是怎样的？为什么做这样的技术选型？如果重新设计你会做哪些改进？",
            "这个项目的用户量或数据量大概是什么级别？你做过哪些性能优化，效果如何量化？",
            "在项目中你如何保证代码质量？请谈谈你在测试、Code Review 或上线流程方面的实践。"
    };

    private static final String[] KEYWORDS = {
            "因为", "原理", "例如", "实践", "优化", "架构", "流程", "方案", "对比", "总结"
    };

    public MockAiClient() {
        log.info("MockAiClient 已启用（启发式评分，不调用外部 AI 服务）");
    }

    @Override
    public ScoreResult scoreAnswer(String jobTitle, String question, String answer) {
        String text = answer == null ? "" : answer.trim();
        int score = 45 + Math.min(30, text.length() / 25);
        for (String kw : KEYWORDS) {
            if (text.contains(kw)) {
                score += 2;
            }
        }
        // 依据内容哈希加入稳定的小幅波动，避免所有回答分数雷同
        score += Math.floorMod(text.hashCode(), 6);
        score = Math.clamp(score, 35, 95);

        String feedback;
        if (text.length() < 20) {
            feedback = "回答过于简短，未能展现对知识点的理解深度，建议展开说明原理并结合具体实例。";
        } else if (score >= 85) {
            feedback = "回答结构清晰、内容充实，展现了对该知识点的深入理解，请继续保持。";
        } else if (score >= 70) {
            feedback = "回答基本到位，覆盖了主要知识点，但在原理深度或实例佐证上还有提升空间。";
        } else {
            feedback = "回答方向基本正确，但内容较为空泛，建议补充具体细节、原理说明和实际案例。";
        }
        return new ScoreResult(score, feedback);
    }

    @Override
    public String generateProjectQuestion(String jobTitle, List<QaPair> previousProjectQa) {
        int idx = Math.floorMod(previousProjectQa == null ? 0 : previousProjectQa.size(),
                PROJECT_QUESTIONS.length);
        return PROJECT_QUESTIONS[idx];
    }

    @Override
    public String generateSummary(String jobTitle, List<QaScore> transcript, int overallScore) {
        int total = transcript == null ? 0 : transcript.size();
        long good = transcript == null ? 0 : transcript.stream().filter(q -> q.score() != null && q.score() >= 80).count();
        long weak = transcript == null ? 0 : transcript.stream().filter(q -> q.score() != null && q.score() < 60).count();

        String level;
        if (overallScore >= 85) {
            level = "表现优秀，具备扎实的岗位能力";
        } else if (overallScore >= 70) {
            level = "表现良好，基本满足岗位要求";
        } else if (overallScore >= 55) {
            level = "表现一般，部分能力项仍需加强";
        } else {
            level = "表现欠佳，与岗位要求存在明显差距";
        }
        return """
                【面试总结报告】
                应聘岗位：%s
                本次面试共回答 %d 道题目，其中 %d 题达到优秀（≥80分），%d 题低于及格线（<60分），综合得分 %d 分。
                总体表现：%s。
                亮点：回答整体有一定的结构性，能够围绕问题给出正面回应。
                不足：部分回答缺少具体实例与数据支撑，技术细节的深度有待加强。
                建议：结合真实项目经历准备典型案例，回答时遵循"结论-原理-实例-效果"的结构，并对薄弱知识点做针对性补强。"""
                .formatted(jobTitle, total, good, weak, overallScore, level);
    }
}
