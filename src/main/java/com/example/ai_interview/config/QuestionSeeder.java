package com.example.ai_interview.config;

import com.example.ai_interview.entity.Question;
import com.example.ai_interview.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** 启动时若题库为空，自动写入一批种子题目（Java后端 / 前端 / HR通用） */
@Component
public class QuestionSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(QuestionSeeder.class);

    private final QuestionRepository questionRepository;

    public QuestionSeeder(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) {
        if (questionRepository.count() > 0) {
            return;
        }
        List<Question> seeds = new ArrayList<>();
        addJavaQuestions(seeds);
        addFrontendQuestions(seeds);
        addHrQuestions(seeds);
        questionRepository.saveAll(seeds);
        log.info("题库为空，已自动初始化种子题目 {} 道", seeds.size());
    }

    private void addJavaQuestions(List<Question> seeds) {
        String c = "Java后端";
        seeds.add(new Question(c, "请说明 Java 中 HashMap 的底层实现原理，它与 ConcurrentHashMap 有什么区别？", 3));
        seeds.add(new Question(c, "Java 线程池的工作原理是什么？核心参数有哪些，如何合理设置？", 4));
        seeds.add(new Question(c, "请描述 JVM 的内存结构以及常见的垃圾回收算法。", 4));
        seeds.add(new Question(c, "数据库事务的 ACID 特性分别指什么？MySQL 的隔离级别有哪些区别？", 3));
        seeds.add(new Question(c, "Spring 的 IOC 和 AOP 是如何实现的？请结合实际项目举例说明。", 3));
        seeds.add(new Question(c, "Redis 为什么能做到高性能？你在哪些场景下会选择使用缓存，如何保证缓存与数据库的一致性？", 4));
        seeds.add(new Question(c, "请谈谈你对 Java 中 volatile 关键字的理解，它与 synchronized 有何区别？", 3));
        seeds.add(new Question(c, "HTTP 常见的状态码有哪些？GET 与 POST 请求有什么区别？", 2));
    }

    private void addFrontendQuestions(List<Question> seeds) {
        String c = "前端";
        seeds.add(new Question(c, "请说明 JavaScript 中 == 与 === 的区别，以及隐式类型转换的规则。", 2));
        seeds.add(new Question(c, "浏览器从接收 HTML 到页面渲染完成经历了哪些过程？什么是重排和重绘？", 4));
        seeds.add(new Question(c, "请谈谈 Vue 或 React 的核心特性，并解释虚拟 DOM 的工作原理。", 3));
        seeds.add(new Question(c, "前端常见的跨域解决方案有哪些？请说明 CORS 的工作原理。", 3));
        seeds.add(new Question(c, "请说明 Promise 的三种状态及 then/catch 的执行时机，async/await 如何使用？", 2));
        seeds.add(new Question(c, "你在前端项目中做过哪些性能优化？请列举具体手段和效果。", 4));
    }

    private void addHrQuestions(List<Question> seeds) {
        String c = "HR通用";
        seeds.add(new Question(c, "请做一个简单的自我介绍，并谈谈你的学习或工作经历。", 1));
        seeds.add(new Question(c, "你为什么选择我们公司以及这个岗位？", 2));
        seeds.add(new Question(c, "请举一个你在团队中遇到困难并成功克服的例子，说明你是如何解决的。", 3));
        seeds.add(new Question(c, "你认为自己最大的优点和缺点分别是什么？", 2));
        seeds.add(new Question(c, "未来 3~5 年你的职业规划是什么？", 2));
        seeds.add(new Question(c, "当与上级或同事意见不一致时，你通常会如何处理？", 3));
    }
}
