package com.example.ai_interview.controller;

import com.example.ai_interview.dto.AuthResponse;
import com.example.ai_interview.dto.LoginRequest;
import com.example.ai_interview.dto.RegisterRequest;
import com.example.ai_interview.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 认证接口：注册 / 登录 */
@Tag(name = "1. 认证", description = "用户注册与登录（无需 token）")
@SecurityRequirements // 覆盖全局认证要求：注册/登录接口本身不需要 token
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 注册（成功后自动登录，返回 JWT） */
    @Operation(summary = "注册", description = "注册新用户并自动登录，返回 JWT token；邮箱重复返回 409")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /** 登录 */
    @Operation(summary = "登录", description = "邮箱 + 密码登录，返回 JWT token；凭证错误返回 401")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
