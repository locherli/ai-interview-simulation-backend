package com.example.ai_interview.ai;

import com.example.ai_interview.config.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容的 Chat Completions 客户端。
 * 任何调用/解析失败都会记录日志并降级到 MockAiClient，保证面试流程不中断。
 */
public class OpenAiClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

    private final AiProperties props;
    private final RestClient restClient;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final MockAiClient fallback = new MockAiClient();

    public OpenAiClient(AiProperties props) {
        this.props = props;
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(props.getTimeoutSeconds()));
        requestFactory.setReadTimeout(Duration.ofSeconds(props.getTimeoutSeconds()));
        this.restClient = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "Bearer " + props.getApiKey())
                .build();
        log.info("OpenAiClient 已启用: baseUrl={}, model={}", props.getBaseUrl(), props.getModel());
    }

    @Override
    public ScoreResult scoreAnswer(String jobTitle, String question, String answer) {
        String system = """
                你是一位资深面试官，负责对候选人的回答进行评分。
                请从准确性、完整性、深度和表达四个维度综合评分（0~100 的整数），并给出不超过 100 字的中文点评。
                你必须只输出一个 JSON 对象，格式为：{"score": 分数, "feedback": "点评"}，不要输出任何其他内容。""";
        String user = """
                应聘岗位：%s
                面试题目：%s
                候选人回答：%s""".formatted(jobTitle, question, answer);
        try {
            String content = chat(system, user);
            JsonNode node = extractJson(content);
            int score = Math.clamp(node.path("score").asInt(60), 0, 100);
            String feedback = node.path("feedback").asString("");
            if (feedback.isBlank()) {
                feedback = "回答基本符合预期，可进一步补充原理与实例。";
            }
            return new ScoreResult(score, feedback);
        } catch (RuntimeException e) {
            log.warn("AI 评分失败，降级为本地评分: {}", e.getMessage());
            return fallback.scoreAnswer(jobTitle, question, answer);
        }
    }
    @Override
    public String generateProjectQuestion(String jobTitle, List<QaPair> previousProjectQa) {
        String system = """
                你是一位资深面试官，正在对候选人进行项目经历考察。
                请围绕候选人的个人项目/实战经历提出一个有深度、开放式的问题。
                只输出问题本身，不要输出编号、前缀、解释或任何其他内容。""";
        StringBuilder user = new StringBuilder("应聘岗位：").append(jobTitle).append('\n');
        if (previousProjectQa == null || previousProjectQa.isEmpty()) {
            user.append("这是项目环节的第一个问题，请让候选人介绍其最有代表性的项目。");
        } else {
            user.append("已经问过的项目问题及候选人的回答如下：\n");
            for (QaPair qa : previousProjectQa) {
                user.append("问：").append(qa.question()).append('\n')
                        .append("答：").append(qa.answer()).append('\n');
            }
            user.append("请基于以上回答提出一个不重复的深入追问（例如技术选型、难点解决、量化效果、改进思路等方向）。");
        }
        try {
            String content = chat(system, user.toString()).trim();
            // 去掉可能的编号前缀与引号
            content = content.replaceAll("^[0-9]+[.、)）]\\s*", "");
            content = content.replaceAll("^[\"'“”]+|[\"'“”]+$", "");
            return content.isBlank() ? fallback.generateProjectQuestion(jobTitle, previousProjectQa) : content;
        } catch (RuntimeException e) {
            log.warn("AI 生成项目问题失败，降级为内置问题: {}", e.getMessage());
            return fallback.generateProjectQuestion(jobTitle, previousProjectQa);
        }
    }
    @Override
    public String generateSummary(String jobTitle, List<QaScore> transcript, int overallScore) {
        String system = """
                你是一位资深面试官，请根据完整的面试问答记录撰写一份面试总结报告。
                报告使用中文纯文本，包含四部分：总体表现、亮点、不足、改进建议，总字数不超过 400 字。
                不要输出 JSON，不要使用 Markdown 标记。""";
        StringBuilder user = new StringBuilder();
        user.append("应聘岗位：").append(jobTitle).append('\n')
                .append("综合得分：").append(overallScore).append("/100\n")
                .append("问答记录：\n");
        int no = 1;
        for (QaScore qa : transcript) {
            user.append(no++).append(". 问：").append(qa.question()).append('\n')
                    .append("   答：").append(qa.answer()).append('\n')
                    .append("   单题得分：").append(qa.score() == null ? "未评分" : qa.score()).append('\n');
        }
        try {
            String content = chat(system, user.toString()).trim();
            return content.isBlank() ? fallback.generateSummary(jobTitle, transcript, overallScore) : content;
        } catch (RuntimeException e) {
            log.warn("AI 生成总结报告失败，降级为本地模板: {}", e.getMessage());
            return fallback.generateSummary(jobTitle, transcript, overallScore);
        }
    }

    /** 调用 Chat Completions，返回 assistant 消息文本 */
    private String chat(String systemPrompt, String userPrompt) {
        Map<String, Object> body = Map.of(
                "model", props.getModel(),
                "temperature", props.getTemperature(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)));
        JsonNode response = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        if (response == null) {
            throw new IllegalStateException("AI 服务响应为空");
        }
        String content = response.path("choices").path(0).path("message").path("content").asString("");
        if (content.isBlank()) {
            throw new IllegalStateException("AI 服务未返回有效内容: " + response);
        }
        return content;
    }

    /** 从 AI 输出中提取 JSON 对象（容忍 markdown 代码块或多余文本） */
    private JsonNode extractJson(String content) {
        String s = content.trim();
        if (s.startsWith("```")) {
            int nl = s.indexOf('\n');
            if (nl > 0) {
                s = s.substring(nl + 1);
            }
            int fence = s.lastIndexOf("```");
            if (fence >= 0) {
                s = s.substring(0, fence);
            }
        }
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("AI 输出中未找到 JSON: " + content);
        }
        return jsonMapper.readTree(s.substring(start, end + 1));
    }
}
