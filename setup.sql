create database ai_interview

\c ai_interview

BEGIN;

-- ---------- 1. 用户 ----------
CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ---------- 2. 题库 ----------
CREATE TABLE questions (
    id          BIGSERIAL   PRIMARY KEY,
    category    VARCHAR(50) NOT NULL,                          -- 题库分组：'Java后端' / '前端' / 'HR通用'
    content     TEXT        NOT NULL,                          -- 题目正文
    difficulty  SMALLINT    NOT NULL DEFAULT 3
                CHECK (difficulty BETWEEN 1 AND 5),            -- 难度 1~5
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_questions_pick ON questions (category, difficulty);

-- ---------- 3. 面试会话 ----------
CREATE TABLE interviews (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_title   VARCHAR(100) NOT NULL,
    category    VARCHAR(50),                                   -- 抽题范围，对应 questions.category
    difficulty  SMALLINT     NOT NULL DEFAULT 3
                CHECK (difficulty BETWEEN 1 AND 5),
    status      VARCHAR(16)  NOT NULL DEFAULT 'ongoing'
                CHECK (status IN ('ongoing', 'finished', 'aborted')),
    score       SMALLINT     CHECK (score BETWEEN 0 AND 100),
    summary     TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    finished_at TIMESTAMPTZ
);

CREATE INDEX idx_interviews_user ON interviews (user_id, created_at DESC);

-- ---------- 4. 对话消息 ----------
CREATE TABLE messages (
    id           BIGSERIAL   PRIMARY KEY,
    interview_id BIGINT      NOT NULL REFERENCES interviews(id) ON DELETE CASCADE,
    question_id  BIGINT      REFERENCES questions(id) ON DELETE SET NULL,  -- 关联题库题
    role         VARCHAR(16) NOT NULL
                 CHECK (role IN ('interviewer', 'candidate')),
    content      TEXT        NOT NULL,
    score        SMALLINT    CHECK (score BETWEEN 0 AND 100),  -- 单题评分（仅 candidate）
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_messages_interview ON messages (interview_id, id);
CREATE INDEX idx_messages_question  ON messages (question_id);

COMMIT;