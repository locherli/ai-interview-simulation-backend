package com.example.ai_interview.repository;

import com.example.ai_interview.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 题库 DAO */
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByCategory(String category);
}
