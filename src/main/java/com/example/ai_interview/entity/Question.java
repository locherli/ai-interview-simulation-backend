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

/** 题库，对应 setup.sql 中的 questions */
@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 题库分组：'Java后端' / '前端' / 'HR通用' */
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    /** 题目正文 */
    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    /** 难度 1~5 */
    @Column(name = "difficulty", nullable = false)
    private Integer difficulty = 3;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Question(String category, String content, Integer difficulty) {
        this.category = category;
        this.content = content;
        this.difficulty = difficulty;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
