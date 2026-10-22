package com.example.ai_interview.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 应用配置（app.* 前缀） */
@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Interview interview = new Interview();

    @Data
    public static class Jwt {
        /** HS256 签名密钥，至少 32 字节 */
        private String secret = "ai-interview-dev-secret-key-change-me-in-production-0123456789";
        /** token 有效期（小时） */
        private long expirationHours = 72;
    }

    @Data
    public static class Interview {
        /** 每场面试从题库抽取的题目数 */
        private int bankQuestionCount = 3;
        /** 每场面试由 AI 生成的项目追问数 */
        private int projectQuestionCount = 2;
    }
}
