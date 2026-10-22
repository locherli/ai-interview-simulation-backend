package com.example.ai_interview.common;

import java.util.Map;

/** 统一错误响应体 */
public record ApiError(
        int code,
        String message,
        /** 字段级校验错误，可为 null */
        Map<String, String> details) {

    public static ApiError of(int code, String message) {
        return new ApiError(code, message, null);
    }
}
