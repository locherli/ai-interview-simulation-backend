package com.example.ai_interview;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OpenAPI 文档与 Swagger UI 集成测试。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocsTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiDocsAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").isNotEmpty())
                .andExpect(jsonPath("$.info.title").value("AI 模拟面试系统 API"))
                // 全部业务端点都已生成文档
                .andExpect(jsonPath("$.paths['/api/auth/register'].post").isNotEmpty())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post").isNotEmpty())
                .andExpect(jsonPath("$.paths['/api/users/me'].get").isNotEmpty())
                .andExpect(jsonPath("$.paths['/api/interviews'].post").isNotEmpty())
                .andExpect(jsonPath("$.paths['/api/interviews'].get").isNotEmpty())
                .andExpect(jsonPath("$.paths['/api/interviews/{id}'].get").isNotEmpty())
                .andExpect(jsonPath("$.paths['/api/interviews/{id}/next-question'].post").isNotEmpty())
                .andExpect(jsonPath("$.paths['/api/interviews/{id}/answers'].post").isNotEmpty())
                .andExpect(jsonPath("$.paths['/api/interviews/{id}/finish'].post").isNotEmpty())
                .andExpect(jsonPath("$.paths['/api/interviews/{id}/abort'].post").isNotEmpty())
                // JWT 认证方案已声明且全局生效
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.security[0].bearerAuth").isArray());
    }

    @Test
    void swaggerUiAvailable() throws Exception {
        // 入口地址 302 跳转到 UI 页面
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
        // UI 静态页面可匿名访问
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
