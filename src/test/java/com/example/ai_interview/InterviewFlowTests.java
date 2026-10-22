package com.example.ai_interview;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证 + 面试完整流程集成测试（使用内置 Mock AI，事务回滚不污染数据库）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InterviewFlowTests {

    private static final String[] ANSWERS = {
            "HashMap 基于数组+链表/红黑树实现，通过 hash 定位桶位置；链表长度超过 8 转红黑树。它是线程不安全的，而 ConcurrentHashMap 在 JDK8 后通过 CAS + synchronized 锁桶头节点保证线程安全，例如在高并发缓存场景中应选用后者。",
            "线程池通过复用线程避免频繁创建销毁的开销，核心参数包括 corePoolSize、maximumPoolSize、keepAliveTime、workQueue 和拒绝策略。IO 密集型任务线程数可以设置为 CPU 核数的两倍左右，例如在我的项目中做过这样的实践和优化。",
            "JVM 内存分为堆、栈、方法区、程序计数器等。垃圾回收算法有标记-清除、标记-整理、复制算法，因为分代收集原理，年轻代用复制算法效率更高，例如 G1 收集器会按 Region 管理堆内存。",
            "我负责过一个校园二手交易平台的后端架构设计与开发，使用 Spring Boot + PostgreSQL，实现了商品发布、搜索和订单流程，日均访问量约 2000，通过引入 Redis 缓存将列表接口响应时间从 300ms 优化到 80ms。",
            "项目中最大的难点是订单超卖问题。我通过数据库乐观锁 + 幂等设计解决，并对比了 Redis 分布式锁方案，最终选择乐观锁因为实现简单且满足业务量级，压测后未再出现超卖。"
    };

    @Autowired
    private MockMvc mockMvc;

    private final JsonMapper json = JsonMapper.builder().build();

    // ---------- 辅助方法 ----------

    private String register(String email, String password) throws Exception {
        String body = json.writeValueAsString(Map.of("email", email, "password", password));
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(email))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return json.readTree(response).path("token").asString();
    }

    private MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Authorization", "Bearer " + token);
    }

    private long startInterview(String token, String jobTitle, String category, int difficulty) throws Exception {
        String body = json.writeValueAsString(Map.of(
                "jobTitle", jobTitle, "category", category, "difficulty", difficulty));
        String response = mockMvc.perform(authed(post("/api/interviews"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ongoing"))
                .andExpect(jsonPath("$.jobTitle").value(jobTitle))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return json.readTree(response).path("id").asLong();
    }

    // ---------- 测试用例 ----------

    @Test
    void registerLoginAndMe() throws Exception {
        register("candidate@example.com", "secret123");

        // 登录成功
        String loginBody = json.writeValueAsString(Map.of(
                "email", "candidate@example.com", "password", "secret123"));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        // 查询当前用户
        String token = login(loginBody);
        mockMvc.perform(authed(get("/api/users/me"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("candidate@example.com"));
    }

    private String login(String loginBody) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return json.readTree(response).path("token").asString();
    }

    @Test
    void authErrors() throws Exception {
        register("dup@example.com", "secret123");

        // 重复注册 → 409
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "email", "dup@example.com", "password", "secret123"))))
                .andExpect(status().isConflict());

        // 密码错误 → 401
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "email", "dup@example.com", "password", "wrong-pass"))))
                .andExpect(status().isUnauthorized());

        // 参数校验失败 → 400
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", "bad-email", "password", "123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").exists());

        // 未携带 token → 401
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());

        // 无效 token → 401
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void fullInterviewFlow() throws Exception {
        String token = register("flow@example.com", "secret123");
        long interviewId = startInterview(token, "Java后端工程师", "Java后端", 3);

        // 5 轮问答：前 3 题来自题库，后 2 题为 AI 项目追问
        for (int i = 0; i < 5; i++) {
            String expectedType = i < 3 ? "bank" : "project";
            mockMvc.perform(authed(post("/api/interviews/{id}/next-question", interviewId), token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").value(expectedType))
                    .andExpect(jsonPath("$.questionNo").value(i + 1))
                    .andExpect(jsonPath("$.totalQuestions").value(5))
                    .andExpect(jsonPath("$.content").isNotEmpty());

            // 重复提交同一题的回答 → 409
            String answerBody = json.writeValueAsString(Map.of("content", ANSWERS[i]));
            if (i == 0) {
                mockMvc.perform(authed(post("/api/interviews/{id}/answers", interviewId), token)
                                .contentType(MediaType.APPLICATION_JSON).content(answerBody))
                        .andExpect(status().isOk());
                mockMvc.perform(authed(post("/api/interviews/{id}/answers", interviewId), token)
                                .contentType(MediaType.APPLICATION_JSON).content(answerBody))
                        .andExpect(status().isConflict());
            } else {
                mockMvc.perform(authed(post("/api/interviews/{id}/answers", interviewId), token)
                                .contentType(MediaType.APPLICATION_JSON).content(answerBody))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.score").isNumber())
                        .andExpect(jsonPath("$.feedback").isNotEmpty())
                        .andExpect(jsonPath("$.answeredCount").value(i + 1));
            }
        }

        // 题目问完后再取题 → 409
        mockMvc.perform(authed(post("/api/interviews/{id}/next-question", interviewId), token))
                .andExpect(status().isConflict());

        // 结束面试 → 总分 + 总结报告
        mockMvc.perform(authed(post("/api/interviews/{id}/finish", interviewId), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interview.status").value("finished"))
                .andExpect(jsonPath("$.interview.score").isNumber())
                .andExpect(jsonPath("$.interview.summary").isNotEmpty())
                .andExpect(jsonPath("$.interview.finishedAt").isNotEmpty())
                .andExpect(jsonPath("$.messages.length()").value(10))
                .andExpect(jsonPath("$.messages[1].role").value("candidate"))
                .andExpect(jsonPath("$.messages[1].score").isNumber());

        // 已结束的面试不能继续操作 → 409
        mockMvc.perform(authed(post("/api/interviews/{id}/finish", interviewId), token))
                .andExpect(status().isConflict());

        // 个人历史
        mockMvc.perform(authed(get("/api/interviews"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(interviewId))
                .andExpect(jsonPath("$.content[0].status").value("finished"));

        // 面试详情
        mockMvc.perform(authed(get("/api/interviews/{id}", interviewId), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interview.summary").isNotEmpty())
                .andExpect(jsonPath("$.messages.length()").value(10));
    }

    @Test
    void ownershipAndFlowErrors() throws Exception {
        String tokenA = register("user-a@example.com", "secret123");
        String tokenB = register("user-b@example.com", "secret123");
        long interviewId = startInterview(tokenA, "前端工程师", "前端", 3);

        // 其他用户访问 → 404
        mockMvc.perform(authed(get("/api/interviews/{id}", interviewId), tokenB))
                .andExpect(status().isNotFound());
        mockMvc.perform(authed(post("/api/interviews/{id}/next-question", interviewId), tokenB))
                .andExpect(status().isNotFound());

        // 未取题直接作答 → 409
        mockMvc.perform(authed(post("/api/interviews/{id}/answers", interviewId), tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("content", "先答后问"))))
                .andExpect(status().isConflict());

        // 无任何回答直接结束 → 400
        mockMvc.perform(authed(post("/api/interviews/{id}/finish", interviewId), tokenA))
                .andExpect(status().isBadRequest());

        // 不存在的题库分类 → 400
        mockMvc.perform(authed(post("/api/interviews"), tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "jobTitle", "算法工程师", "category", "不存在的分类"))))
                .andExpect(status().isBadRequest());

        // 中止面试 → aborted
        mockMvc.perform(authed(post("/api/interviews/{id}/abort", interviewId), tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("aborted"));
    }
}
