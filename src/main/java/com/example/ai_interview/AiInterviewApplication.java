package com.example.ai_interview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

// 使用自定义 JWT 无状态认证，排除 Spring Security 默认的内存用户自动配置（避免生成随机密码的告警日志）
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class AiInterviewApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiInterviewApplication.class, args);
	}

}
