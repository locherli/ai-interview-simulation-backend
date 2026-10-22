package com.example.ai_interview.entity;

/** 面试状态，数据库中以小写字符串存储（interviews.status 的 CHECK 约束） */
public enum InterviewStatus {

    ONGOING("ongoing"),
    FINISHED("finished"),
    ABORTED("aborted");

    private final String value;

    InterviewStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static InterviewStatus from(String value) {
        for (InterviewStatus s : values()) {
            if (s.value.equals(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("未知的面试状态: " + value);
    }
}
