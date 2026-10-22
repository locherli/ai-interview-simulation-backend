package com.example.ai_interview.service;

import com.example.ai_interview.common.ApiException;
import com.example.ai_interview.dto.AuthResponse;
import com.example.ai_interview.dto.LoginRequest;
import com.example.ai_interview.dto.RegisterRequest;
import com.example.ai_interview.entity.User;
import com.example.ai_interview.repository.UserRepository;
import com.example.ai_interview.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 认证服务：注册 / 登录 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** 注册成功后直接返回 JWT（自动登录） */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("该邮箱已被注册");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);
        String token = jwtService.issue(user.getId(), user.getEmail());
        return AuthResponse.of(token, user.getId(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.unauthorized("邮箱或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("邮箱或密码错误");
        }
        String token = jwtService.issue(user.getId(), user.getEmail());
        return AuthResponse.of(token, user.getId(), user.getEmail());
    }
}
