package com.example.backend.controller;

import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/page")
    public Result<PageResult<User>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(userService.page(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody User user) {
        userService.save(user);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody User user) {
        userService.update(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }

    @PutMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String password = (String) body.get("password");
        userService.resetPassword(id, password);
        return Result.success();
    }
}
