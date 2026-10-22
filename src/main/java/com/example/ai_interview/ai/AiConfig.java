package com.example.ai_interview.ai;

import com.example.ai_interview.config.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/** AI 客户端装配：配置了 ai.api-key 使用 OpenAI 兼容服务，否则使用内置 Mock 实现 */
@Configuration
public class AiConfig {

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    @Bean
    public AiClient aiClient(AiProperties props) {
        if (StringUtils.hasText(props.getApiKey())) {
            return new OpenAiClient(props);
        }
        log.warn("未配置 ai.api-key，使用内置 MockAiClient（启发式评分）。" +
                "如需接入真实大模型，请设置 ai.base-url / ai.api-key / ai.model");
        return new MockAiClient();
    }
}
