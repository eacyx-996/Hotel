package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.PageResult;
import com.example.backend.entity.Room;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomMapper roomMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_AVAILABLE = "room:available";
    private static final String CACHE_KEY_STATS = "room:stats";

    public PageResult<Room> page(long pageNum, long pageSize, String keyword, Long roomTypeId, Integer status) {
        Page<Room> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Room::getRoomNumber, keyword);
        }
        if (roomTypeId != null) {
            wrapper.eq(Room::getRoomTypeId, roomTypeId);
        }
        if (status != null) {
            wrapper.eq(Room::getStatus, status);
        }
        wrapper.orderByAsc(Room::getRoomNumber);
        Page<Room> result = roomMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public Room getById(Long id) {
        return roomMapper.selectById(id);
    }

    public List<Map<String, Object>> listWithTypeName() {
        return roomMapper.selectRoomWithTypeName();
    }

    public List<Room> listAvailable() {
        @SuppressWarnings("unchecked")
        List<Room> cached = (List<Room>) redisTemplate.opsForValue().get(CACHE_KEY_AVAILABLE);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getStatus, 0).orderByAsc(Room::getRoomNumber);
        List<Room> list = roomMapper.selectList(wrapper);
        redisTemplate.opsForValue().set(CACHE_KEY_AVAILABLE, list, 10, TimeUnit.MINUTES);
        return list;
    }

    public Map<String, Object> stats() {
        @SuppressWarnings("unchecked")
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(CACHE_KEY_STATS);
        if (cached != null) return cached;
        Map<String, Object> stats = roomMapper.selectRoomStats();
        redisTemplate.opsForValue().set(CACHE_KEY_STATS, stats, 5, TimeUnit.MINUTES);
        return stats;
    }

    public void save(Room room) {
        // 检查房号是否重复
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getRoomNumber, room.getRoomNumber());
        if (roomMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("房间号已存在");
        }
        if (room.getStatus() == null) room.setStatus(0);
        roomMapper.insert(room);
        clearCache();
    }

    @Transactional
    public void update(Room room) {
        roomMapper.updateById(room);
        clearCache();
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        Room room = new Room();
        room.setId(id);
        room.setStatus(status);
        roomMapper.updateById(room);
        clearCache();
    }

    public void removeById(Long id) {
        roomMapper.deleteById(id);
        clearCache();
    }

    private void clearCache() {
        redisTemplate.delete(CACHE_KEY_AVAILABLE);
        redisTemplate.delete(CACHE_KEY_STATS);
    }
}
