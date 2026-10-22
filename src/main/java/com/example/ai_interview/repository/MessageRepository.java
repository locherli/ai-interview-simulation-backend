package com.example.ai_interview.repository;

import com.example.ai_interview.entity.Message;
import com.example.ai_interview.entity.MessageRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** 对话消息 DAO */
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByInterviewIdOrderByIdAsc(Long interviewId);

    Optional<Message> findTopByInterviewIdOrderByIdDesc(Long interviewId);

    long countByInterviewIdAndRole(Long interviewId, MessageRole role);

    /** 本场面试已抽取过的题库题 id（用于避免重复抽题） */
    @Query("select m.questionId from Message m where m.interviewId = :interviewId and m.questionId is not null")
    List<Long> findAskedQuestionIds(@Param("interviewId") Long interviewId);
}
