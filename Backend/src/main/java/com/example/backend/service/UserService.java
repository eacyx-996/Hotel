package com.example.backend.service;

import com.example.backend.common.PageResult;
import com.example.backend.entity.User;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Map<String, Object> login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 将用户信息缓存到 Redis
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("role", user.getRole());
        userInfo.put("phone", user.getPhone());
        userInfo.put("email", user.getEmail());
        redisTemplate.opsForValue().set("user:info:" + user.getId(), userInfo, 24, TimeUnit.HOURS);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userInfo);
        return result;
    }

    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    public PageResult<User> page(long pageNum, long pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public void save(User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) user.setRole("STAFF");
        if (user.getStatus() == null) user.setStatus(1);
        userMapper.insert(user);
    }

    public void update(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        userMapper.updateById(user);
    }

    public void resetPassword(Long id, String newPassword) {
        User user = new User();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    public void removeById(Long id) {
        userMapper.deleteById(id);
        redisTemplate.delete("user:info:" + id);
    }
}
