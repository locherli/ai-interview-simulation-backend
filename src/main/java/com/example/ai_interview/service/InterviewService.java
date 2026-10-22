package com.example.ai_interview.service;

import com.example.ai_interview.ai.AiClient;
import com.example.ai_interview.common.ApiException;
import com.example.ai_interview.config.AppProperties;
import com.example.ai_interview.dto.AnswerRequest;
import com.example.ai_interview.dto.AnswerResponse;
import com.example.ai_interview.dto.InterviewDetailDto;
import com.example.ai_interview.dto.InterviewDto;
import com.example.ai_interview.dto.MessageDto;
import com.example.ai_interview.dto.NextQuestionResponse;
import com.example.ai_interview.dto.PageResponse;
import com.example.ai_interview.dto.StartInterviewRequest;
import com.example.ai_interview.entity.Interview;
import com.example.ai_interview.entity.InterviewStatus;
import com.example.ai_interview.entity.Message;
import com.example.ai_interview.entity.MessageRole;
import com.example.ai_interview.entity.Question;
import com.example.ai_interview.repository.InterviewRepository;
import com.example.ai_interview.repository.MessageRepository;
import com.example.ai_interview.repository.QuestionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 面试核心业务：开始面试、抽题（题库随机 + AI 项目追问）、AI 单题评分、
 * 总分与总结报告、历史查询。
 */
@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final MessageRepository messageRepository;
    private final QuestionRepository questionRepository;
    private final AiClient aiClient;
    private final AppProperties props;

    public InterviewService(InterviewRepository interviewRepository,
                            MessageRepository messageRepository,
                            QuestionRepository questionRepository,
                            AiClient aiClient,
                            AppProperties props) {
        this.interviewRepository = interviewRepository;
        this.messageRepository = messageRepository;
        this.questionRepository = questionRepository;
        this.aiClient = aiClient;
        this.props = props;
    }

    /** 开始一场面试 */
    @Transactional
    public InterviewDto start(Long userId, StartInterviewRequest request) {
        String category = (request.category() == null || request.category().isBlank())
                ? null : request.category().trim();
        int difficulty = request.difficulty() == null ? 3 : request.difficulty();
        if (category != null && questionRepository.findByCategory(category).isEmpty()) {
            throw ApiException.badRequest("题库分类不存在或暂无题目: " + category);
        }
        Interview interview = new Interview();
        interview.setUserId(userId);
        interview.setJobTitle(request.jobTitle().trim());
        interview.setCategory(category);
        interview.setDifficulty(difficulty);
        interview.setStatus(InterviewStatus.ONGOING);
        return toDto(interviewRepository.save(interview));
    }
    /** 获取下一题：前 bankCount 题从题库随机抽取，之后 projectCount 题由 AI 针对个人项目生成 */
    @Transactional
    public NextQuestionResponse nextQuestion(Long userId, Long interviewId) {
        Interview interview = loadOwned(userId, interviewId);
        requireOngoing(interview);

        int bankCount = props.getInterview().getBankQuestionCount();
        int projectCount = props.getInterview().getProjectQuestionCount();
        int total = bankCount + projectCount;
        long asked = messageRepository.countByInterviewIdAndRole(interviewId, MessageRole.INTERVIEWER);
        if (asked >= total) {
            throw ApiException.conflict("题目已全部问完，请调用 finish 接口结束面试并生成报告");
        }

        Question bankQuestion = asked < bankCount ? pickRandomQuestion(interview) : null;
        Message message = new Message();
        message.setInterviewId(interviewId);
        message.setRole(MessageRole.INTERVIEWER);
        String type;
        if (bankQuestion != null) {
            message.setQuestionId(bankQuestion.getId());
            message.setContent(bankQuestion.getContent());
            type = "bank";
        } else {
            // 题库题已问完（或该分类题目已抽尽）→ AI 项目追问
            String question = aiClient.generateProjectQuestion(
                    interview.getJobTitle(), projectContext(interviewId));
            message.setContent(question);
            type = "project";
        }
        message = messageRepository.save(message);
        return new NextQuestionResponse(message.getId(), (int) asked + 1, total, type, message.getContent());
    }

    /** 提交当前题目的回答，由 AI 打分并给出点评 */
    @Transactional
    public AnswerResponse answer(Long userId, Long interviewId, AnswerRequest request) {
        Interview interview = loadOwned(userId, interviewId);
        requireOngoing(interview);

        Message last = messageRepository.findTopByInterviewIdOrderByIdDesc(interviewId)
                .orElseThrow(() -> ApiException.conflict("请先获取题目再作答"));
        if (last.getRole() != MessageRole.INTERVIEWER) {
            throw ApiException.conflict("当前题目已回答，请先获取下一题");
        }

        String answerText = request.content().trim();
        AiClient.ScoreResult result = aiClient.scoreAnswer(
                interview.getJobTitle(), last.getContent(), answerText);
        int score = Math.clamp(result.score(), 0, 100);

        Message answerMessage = new Message();
        answerMessage.setInterviewId(interviewId);
        answerMessage.setQuestionId(last.getQuestionId());
        answerMessage.setRole(MessageRole.CANDIDATE);
        answerMessage.setContent(answerText);
        answerMessage.setScore(score);
        messageRepository.save(answerMessage);

        long answered = messageRepository.countByInterviewIdAndRole(interviewId, MessageRole.CANDIDATE);
        int total = props.getInterview().getBankQuestionCount() + props.getInterview().getProjectQuestionCount();
        return new AnswerResponse(score, result.feedback(), (int) answered, total);
    }
    /** 结束面试：计算总分（各题均分）并由 AI 生成总结报告 */
    @Transactional
    public InterviewDetailDto finish(Long userId, Long interviewId) {
        Interview interview = loadOwned(userId, interviewId);
        requireOngoing(interview);

        List<Message> messages = messageRepository.findByInterviewIdOrderByIdAsc(interviewId);
        List<AiClient.QaScore> transcript = buildTranscript(messages);
        if (transcript.isEmpty()) {
            throw ApiException.badRequest("尚无任何回答，无法生成面试报告");
        }
        int overall = (int) Math.round(transcript.stream()
                .mapToInt(q -> q.score() == null ? 0 : q.score())
                .average().orElse(0));
        overall = Math.clamp(overall, 0, 100);
        String summary = aiClient.generateSummary(interview.getJobTitle(), transcript, overall);

        interview.setScore(overall);
        interview.setSummary(summary);
        interview.setStatus(InterviewStatus.FINISHED);
        interview.setFinishedAt(OffsetDateTime.now());
        interviewRepository.save(interview);
        return toDetailDto(interview, messages);
    }

    /** 中止面试（不生成报告） */
    @Transactional
    public InterviewDto abort(Long userId, Long interviewId) {
        Interview interview = loadOwned(userId, interviewId);
        requireOngoing(interview);
        interview.setStatus(InterviewStatus.ABORTED);
        interview.setFinishedAt(OffsetDateTime.now());
        return toDto(interviewRepository.save(interview));
    }

    /** 个人面试历史（分页，按时间倒序） */
    @Transactional(readOnly = true)
    public PageResponse<InterviewDto> history(Long userId, int page, int size) {
        int p = Math.max(page, 0);
        int s = Math.clamp(size, 1, 50);
        return PageResponse.of(interviewRepository
                .findByUserIdOrderByIdDesc(userId, PageRequest.of(p, s))
                .map(this::toDto));
    }

    /** 面试详情（含完整问答记录与单题得分） */
    @Transactional(readOnly = true)
    public InterviewDetailDto detail(Long userId, Long interviewId) {
        Interview interview = loadOwned(userId, interviewId);
        return toDetailDto(interview, messageRepository.findByInterviewIdOrderByIdAsc(interviewId));
    }
    /** 加载属于当前用户的面试，不存在或不属于该用户统一返回 404，避免 id 枚举 */
    private Interview loadOwned(Long userId, Long interviewId) {
        return interviewRepository.findById(interviewId)
                .filter(i -> i.getUserId().equals(userId))
                .orElseThrow(() -> ApiException.notFound("面试不存在"));
    }

    private void requireOngoing(Interview interview) {
        if (interview.getStatus() != InterviewStatus.ONGOING) {
            throw ApiException.conflict("面试已结束（当前状态: " + interview.getStatus().getValue() + "），不能继续操作");
        }
    }

    /** 从题库随机抽一道未问过的题：按分类过滤，优先难度在目标 ±1 范围内的题目 */
    private Question pickRandomQuestion(Interview interview) {
        List<Question> pool = interview.getCategory() != null
                ? questionRepository.findByCategory(interview.getCategory())
                : questionRepository.findAll();
        Set<Long> usedIds = new HashSet<>(messageRepository.findAskedQuestionIds(interview.getId()));
        List<Question> candidates = pool.stream()
                .filter(q -> !usedIds.contains(q.getId()))
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }
        int target = interview.getDifficulty() == null ? 3 : interview.getDifficulty();
        List<Question> preferred = candidates.stream()
                .filter(q -> q.getDifficulty() != null && Math.abs(q.getDifficulty() - target) <= 1)
                .toList();
        List<Question> pickFrom = preferred.isEmpty() ? candidates : preferred;
        return pickFrom.get(ThreadLocalRandom.current().nextInt(pickFrom.size()));
    }

    /** 已进行的项目问答（question_id 为 null 的消息），用于 AI 生成追问上下文 */
    private List<AiClient.QaPair> projectContext(Long interviewId) {
        List<Message> messages = messageRepository.findByInterviewIdOrderByIdAsc(interviewId);
        List<AiClient.QaPair> pairs = new ArrayList<>();
        Message pendingQuestion = null;
        for (Message m : messages) {
            if (m.getQuestionId() != null) {
                continue;
            }
            if (m.getRole() == MessageRole.INTERVIEWER) {
                pendingQuestion = m;
            } else if (pendingQuestion != null) {
                pairs.add(new AiClient.QaPair(pendingQuestion.getContent(), m.getContent()));
                pendingQuestion = null;
            }
        }
        return pairs;
    }

    /** 将消息序列配对成 (问题, 回答, 得分) 记录 */
    private List<AiClient.QaScore> buildTranscript(List<Message> messages) {
        List<AiClient.QaScore> transcript = new ArrayList<>();
        Message pendingQuestion = null;
        for (Message m : messages) {
            if (m.getRole() == MessageRole.INTERVIEWER) {
                pendingQuestion = m;
            } else if (pendingQuestion != null) {
                transcript.add(new AiClient.QaScore(pendingQuestion.getContent(), m.getContent(), m.getScore()));
                pendingQuestion = null;
            }
        }
        return transcript;
    }

    private InterviewDto toDto(Interview i) {
        return new InterviewDto(i.getId(), i.getJobTitle(), i.getCategory(), i.getDifficulty(),
                i.getStatus().getValue(), i.getScore(), i.getSummary(),
                i.getCreatedAt(), i.getFinishedAt());
    }

    private InterviewDetailDto toDetailDto(Interview interview, List<Message> messages) {
        List<MessageDto> messageDtos = messages.stream()
                .map(m -> new MessageDto(m.getId(), m.getRole().getValue(), m.getContent(),
                        m.getScore(), m.getQuestionId(), m.getCreatedAt()))
                .toList();
        return new InterviewDetailDto(toDto(interview), messageDtos);
    }
}
