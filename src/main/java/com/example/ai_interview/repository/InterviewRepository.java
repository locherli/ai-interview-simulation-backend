package com.example.ai_interview.repository;

import com.example.ai_interview.entity.Interview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** 面试会话 DAO */
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    /** 个人面试历史，按创建时间倒序（利用 idx_interviews_user 索引） */
    Page<Interview> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
}
