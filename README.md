# AI Interview Simulator (AI 面试模拟器后端)

## 项目简介 / Project Introduction

**[中文]**
本项目是一个基于人工智能的面试模拟器后端服务。它旨在帮助候选人通过与AI面试官的对话来进行模拟面试，提升面试技巧。系统提供了用户注册与登录、题库抽题、面试流程控制、以及与AI进行追问对话和评分等功能。配套的前端项目可以在这里找到：[AI Interview Simulation Frontend](https://github.com/locherli/ai-interview-simulation-frontend)。

**[English]**
This project is the backend service for an AI-based interview simulator. It is designed to help candidates practice and improve their interviewing skills through simulated conversations with an AI interviewer. The system provides features such as user registration and login, question bank management, interview workflow control, follow-up questioning, and answer scoring. The companion frontend project can be found here: [AI Interview Simulation Frontend](https://github.com/locherli/ai-interview-simulation-frontend).

## 项目架构 / Project Architecture

**[中文]**
本项目采用以下技术栈和架构：
- **核心框架**：基于 Spring Boot (Java 21) 构建。
- **数据库**：使用 PostgreSQL 进行数据持久化，包含用户(Users)、题库(Questions)、面试(Interviews)和消息(Messages)等核心数据表。
- **安全认证**：采用基于 JWT (JSON Web Token) 的无状态认证机制，并集成了 Spring Security。
- **AI 交互**：通过接入兼容 OpenAI 格式的 API（默认使用 GPT-4o-mini）来生成面试追问和评分。如果没有配置 API Key，系统会自动使用内置的 Mock AI 服务。
- **API 文档**：集成了 Springdoc OpenAPI 3 (Swagger UI)，便于开发与接口调试。

**[English]**
This project is built with the following technology stack and architecture:
- **Core Framework**: Built on Spring Boot (Java 21).
- **Database**: Uses PostgreSQL for data persistence, including core tables for Users, Questions, Interviews, and Messages.
- **Security**: Employs stateless authentication based on JWT (JSON Web Token) integrated with Spring Security.
- **AI Integration**: Connects to OpenAI-compatible APIs (defaults to GPT-4o-mini) to generate interview follow-ups and score answers. If no API Key is configured, it automatically falls back to a built-in Mock AI service.
- **API Documentation**: Integrates Springdoc OpenAPI 3 (Swagger UI) for easy development and endpoint testing.

## 对接前端项目 / Frontend Integration

**[中文]**
本后端项目专为以下前端项目设计，请前往该仓库获取前端代码：
前端项目地址：[https://github.com/locherli/ai-interview-simulation-frontend](https://github.com/locherli/ai-interview-simulation-frontend)

**[English]**
This backend project is designed to work seamlessly with the following frontend project. Please visit the repository for the frontend code:
Frontend Project URL: [https://github.com/locherli/ai-interview-simulation-frontend](https://github.com/locherli/ai-interview-simulation-frontend)

## 如何使用 / How to Use

**[中文]**
1. **环境准备**：
   - 确保系统中安装了 Java 21 和 PostgreSQL。
2. **初始化数据库**：
   - 在 PostgreSQL 中执行根目录下的 `setup.sql` 文件，以创建数据库 `ai_interview` 及所有必要的表结构。
3. **配置应用**：
   - 如果需要真实的 AI 交互，请在 `src/main/resources/application.properties` 中配置你的 `ai.api-key`。留空则会使用内置的 Mock 功能。
   - 检查并配置正确的 PostgreSQL 用户名和密码。
4. **运行项目**：
   - 使用 Maven Wrapper 启动项目：
     ```bash
     ./mvnw spring-boot:run
     ```
5. **访问 API 文档**：
   - 项目启动成功后，可以在浏览器中访问 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) 查看和测试所有可用接口。

**[English]**
1. **Prerequisites**:
   - Ensure Java 21 and PostgreSQL are installed on your system.
2. **Initialize Database**:
   - Execute the `setup.sql` file located in the root directory within PostgreSQL to create the `ai_interview` database and all necessary tables.
3. **Configure Application**:
   - If you want real AI interactions, configure your `ai.api-key` in `src/main/resources/application.properties`. Leave it blank to use the built-in Mock AI functionality.
   - Verify and configure the correct PostgreSQL username and password.
4. **Run the Project**:
   - Start the project using the Maven Wrapper:
     ```bash
     ./mvnw spring-boot:run
     ```
5. **Access API Documentation**:
   - Once the project is successfully running, you can visit [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) in your browser to view and test all available endpoints.
