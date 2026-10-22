package com.example.ai_interview.service;

import com.example.ai_interview.common.ApiException;
import com.example.ai_interview.dto.UserDto;
import com.example.ai_interview.entity.User;
import com.example.ai_interview.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用户服务 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserDto me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("用户不存在"));
        return new UserDto(user.getId(), user.getEmail(), user.getCreatedAt());
    }
}
