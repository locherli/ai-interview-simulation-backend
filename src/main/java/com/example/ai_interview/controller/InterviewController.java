package com.example.ai_interview.controller;

import com.example.ai_interview.dto.AnswerRequest;
import com.example.ai_interview.dto.AnswerResponse;
import com.example.ai_interview.dto.InterviewDetailDto;
import com.example.ai_interview.dto.InterviewDto;
import com.example.ai_interview.dto.NextQuestionResponse;
import com.example.ai_interview.dto.PageResponse;
import com.example.ai_interview.dto.StartInterviewRequest;
import com.example.ai_interview.security.LoginUser;
import com.example.ai_interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 面试接口：开始面试、抽题、答题（AI 评分）、结束（总分+报告）、历史查询 */
@Tag(name = "3. 面试", description = "面试全流程：开始 → 取题 → 答题（AI 评分）→ 结束（总分 + 总结报告）→ 历史/详情查询")
@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    /** 开始一场面试 */
    @Operation(summary = "开始面试", description = "创建一场面试（状态 ongoing）。category 可选（如 Java后端 / 前端 / HR通用），不传则从全部题库抽题；difficulty 1~5")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InterviewDto start(@AuthenticationPrincipal LoginUser user,
                              @Valid @RequestBody StartInterviewRequest request) {
        return interviewService.start(user.id(), request);
    }

    /** 获取下一题（题库随机题 + AI 项目追问） */
    @Operation(summary = "获取下一题", description = "前 N 题从题库随机抽取（type=bank），之后由 AI 结合项目问答生成追问（type=project）；题目问完返回 409")
    @PostMapping("/{id}/next-question")
    public NextQuestionResponse nextQuestion(@AuthenticationPrincipal LoginUser user,
                                             @Parameter(description = "面试 ID") @PathVariable Long id) {
        return interviewService.nextQuestion(user.id(), id);
    }

    /** 提交当前题目的回答，AI 打分并返回点评 */
    @Operation(summary = "提交回答", description = "提交当前题目的回答，AI 实时评分（0~100）并返回点评；必须先取题，重复提交返回 409")
    @PostMapping("/{id}/answers")
    public AnswerResponse answer(@AuthenticationPrincipal LoginUser user,
                                 @Parameter(description = "面试 ID") @PathVariable Long id,
                                 @Valid @RequestBody AnswerRequest request) {
        return interviewService.answer(user.id(), id, request);
    }

    /** 结束面试：生成总分与 AI 总结报告 */
    @Operation(summary = "结束面试", description = "计算总分（各题得分均值）并由 AI 生成总结报告，面试状态变为 finished；无任何回答返回 400")
    @PostMapping("/{id}/finish")
    public InterviewDetailDto finish(@AuthenticationPrincipal LoginUser user,
                                     @Parameter(description = "面试 ID") @PathVariable Long id) {
        return interviewService.finish(user.id(), id);
    }

    /** 中止面试 */
    @Operation(summary = "中止面试", description = "放弃当前面试（状态变为 aborted，不生成报告）")
    @PostMapping("/{id}/abort")
    public InterviewDto abort(@AuthenticationPrincipal LoginUser user,
                              @Parameter(description = "面试 ID") @PathVariable Long id) {
        return interviewService.abort(user.id(), id);
    }

    /** 个人面试历史（分页） */
    @Operation(summary = "面试历史", description = "分页查询当前用户的面试历史（按时间倒序），size 上限 50")
    @GetMapping
    public PageResponse<InterviewDto> history(@AuthenticationPrincipal LoginUser user,
                                              @Parameter(description = "页码，从 0 开始") @RequestParam(defaultValue = "0") int page,
                                              @Parameter(description = "每页条数（1~50）") @RequestParam(defaultValue = "10") int size) {
        return interviewService.history(user.id(), page, size);
    }

    /** 面试详情（含完整问答记录与单题得分） */
    @Operation(summary = "面试详情", description = "查询单场面试详情，包含完整问答记录与每题得分；仅可查询本人的面试，否则返回 404")
    @GetMapping("/{id}")
    public InterviewDetailDto detail(@AuthenticationPrincipal LoginUser user,
                                     @Parameter(description = "面试 ID") @PathVariable Long id) {
        return interviewService.detail(user.id(), id);
    }
}
