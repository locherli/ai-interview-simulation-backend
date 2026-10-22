package com.example.ai_interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 提交答案请求 */
public record AnswerRequest(

        @NotBlank(message = "回答内容不能为空")
        @Size(max = 5000, message = "回答内容过长（最多 5000 字）")
        String content) {
}
