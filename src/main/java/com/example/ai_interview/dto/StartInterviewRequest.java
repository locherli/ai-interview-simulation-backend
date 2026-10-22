package com.example.ai_interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 开始面试请求 */
public record StartInterviewRequest(

        /** 应聘岗位，如 "Java后端工程师" */
        @NotBlank(message = "应聘岗位不能为空")
        @Size(max = 100, message = "岗位名称过长")
        String jobTitle,

        /** 抽题范围（题库分类），可为空表示不限分类 */
        @Size(max = 50, message = "分类名称过长")
        String category,

        /** 目标难度 1~5，默认 3 */
        @Min(value = 1, message = "难度最小为 1")
        @Max(value = 5, message = "难度最大为 5")
        Integer difficulty) {
}
