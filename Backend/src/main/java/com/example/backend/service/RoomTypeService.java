package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.PageResult;
import com.example.backend.entity.RoomType;
import com.example.backend.mapper.RoomTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RoomTypeService {

    private final RoomTypeMapper roomTypeMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY = "roomtype:all";

    public List<RoomType> listAll() {
        @SuppressWarnings("unchecked")
        List<RoomType> cached = (List<RoomType>) redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        LambdaQueryWrapper<RoomType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomType::getStatus, 1);
        wrapper.orderByAsc(RoomType::getPrice);
        List<RoomType> list = roomTypeMapper.selectList(wrapper);
        redisTemplate.opsForValue().set(CACHE_KEY, list, 30, TimeUnit.MINUTES);
        return list;
    }

    public PageResult<RoomType> page(long pageNum, long pageSize, String keyword) {
        Page<RoomType> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<RoomType> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(RoomType::getName, keyword);
        }
        wrapper.orderByDesc(RoomType::getCreateTime);
        Page<RoomType> result = roomTypeMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public RoomType getById(Long id) {
        return roomTypeMapper.selectById(id);
    }

    public void save(RoomType roomType) {
        roomTypeMapper.insert(roomType);
        clearCache();
    }

    public void update(RoomType roomType) {
        roomTypeMapper.updateById(roomType);
        clearCache();
    }

    public void removeById(Long id) {
        roomTypeMapper.deleteById(id);
        clearCache();
    }

    private void clearCache() {
        redisTemplate.delete(CACHE_KEY);
    }
}
