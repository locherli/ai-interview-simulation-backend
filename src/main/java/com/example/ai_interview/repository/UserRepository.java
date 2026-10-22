package com.example.ai_interview.repository;

import com.example.ai_interview.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** 用户 DAO */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
