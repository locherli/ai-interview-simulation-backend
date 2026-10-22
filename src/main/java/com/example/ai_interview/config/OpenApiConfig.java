package com.example.ai_interview.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI（Swagger UI）文档配置：
 * 声明 JWT Bearer 认证方案并全局应用，可在 Swagger UI 中点击 Authorize 填入 token 调试接口。
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI aiInterviewOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI 模拟面试系统 API")
                        .description("""
                                AI 模拟面试系统后端接口文档：认证、面试流程、AI 评分与总结、历史查询。

                                ## 使用步骤
                                1. 调用 `POST /api/auth/register` 注册（或 `POST /api/auth/login` 登录），从响应中复制 `token`
                                2. 点击页面右上角 **Authorize** 按钮，粘贴 token（无需添加 `Bearer ` 前缀）
                                3. 即可调试其余需要登录的接口

                                ## 面试流程
                                开始面试 → 循环【取下一题 → 提交答案（AI 实时评分）】→ 结束面试（生成总分与 AI 总结报告）→ 查询历史/详情。
                                每场面试前 3 题来自题库随机抽取，后 2 题为 AI 针对项目经历的追问（数量可在配置中调整）。
                                """)
                        .version("v1.0.0"))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("注册/登录接口返回的 JWT token")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME));
    }
}
