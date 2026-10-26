package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }
        return Result.success(userService.login(username, password));
    }

    @GetMapping("/info")
    public Result<Object> info() {
        Long userId = com.example.backend.utils.UserContext.getUserId();
        return Result.success(userService.getById(userId));
    }
}
