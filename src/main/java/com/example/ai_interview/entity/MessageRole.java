package com.example.ai_interview.entity;

/** 消息角色，数据库中以小写字符串存储（messages.role 的 CHECK 约束） */
public enum MessageRole {

    /** 面试官（含 AI 生成的问题） */
    INTERVIEWER("interviewer"),
    /** 候选人 */
    CANDIDATE("candidate");

    private final String value;

    MessageRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MessageRole from(String value) {
        for (MessageRole r : values()) {
            if (r.value.equals(value)) {
                return r;
            }
        }
        throw new IllegalArgumentException("未知的消息角色: " + value);
    }
}
