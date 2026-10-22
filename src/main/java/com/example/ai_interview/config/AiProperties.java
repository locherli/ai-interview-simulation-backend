package com.example.ai_interview.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** AI 服务配置（ai.* 前缀），兼容 OpenAI Chat Completions 协议 */
@Data
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** API 基础地址，如 https://api.openai.com/v1 */
    private String baseUrl = "https://api.openai.com/v1";

    /** API 密钥；为空时自动降级为内置 Mock 实现 */
    private String apiKey = "";

    /** 模型名称 */
    private String model = "gpt-4o-mini";

    /** 采样温度 */
    private double temperature = 0.3;

    /** 请求超时（秒） */
    private int timeoutSeconds = 60;
}
