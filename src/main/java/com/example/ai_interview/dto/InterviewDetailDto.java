package com.example.ai_interview.dto;

import java.util.List;

/** 面试详情：会话信息 + 完整问答记录 */
public record InterviewDetailDto(
        InterviewDto interview,
        List<MessageDto> messages) {
}
