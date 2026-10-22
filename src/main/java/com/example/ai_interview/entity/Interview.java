package com.example.ai_interview.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/** 面试会话，对应 setup.sql 中的 interviews */
@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 应聘岗位 */
    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;

    /** 抽题范围，对应 questions.category，可为空（不限分类） */
    @Column(name = "category", length = 50)
    private String category;

    /** 目标难度 1~5 */
    @Column(name = "difficulty", nullable = false)
    private Integer difficulty = 3;

    @Column(name = "status", nullable = false, length = 16)
    private InterviewStatus status = InterviewStatus.ONGOING;

    /** 面试总分 0~100，结束后写入 */
    @Column(name = "score")
    private Integer score;

    /** AI 总结报告，结束后写入 */
    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
