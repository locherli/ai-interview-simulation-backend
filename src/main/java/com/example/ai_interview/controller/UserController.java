package com.example.ai_interview.controller;

import com.example.ai_interview.dto.UserDto;
import com.example.ai_interview.security.LoginUser;
import com.example.ai_interview.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 用户接口 */
@Tag(name = "2. 用户", description = "当前登录用户信息")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 当前登录用户信息 */
    @Operation(summary = "当前用户", description = "根据 JWT token 返回当前登录用户信息")
    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal LoginUser user) {
        return userService.me(user.id());
    }
}
