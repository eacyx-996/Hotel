package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.PageResult;
import com.example.backend.entity.Order;
import com.example.backend.entity.Room;
import com.example.backend.entity.RoomType;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.OrderMapper;
import com.example.backend.mapper.RoomMapper;
import com.example.backend.mapper.RoomTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final RoomMapper roomMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ROOM_STATUS_KEY = "room:status:";

    @Transactional
    public Map<String, Object> createOrder(Order order) {
        // 查询房间
        Room room = roomMapper.selectById(order.getRoomId());
        if (room == null) {
            throw new BusinessException("客房不存在");
        }
        if (room.getStatus() != 0) {
            throw new BusinessException("该房间当前不可预订");
        }

        // 从 Redis 二次校验房态(防高并发)
        @SuppressWarnings("unchecked")
        Map<String, Object> cachedStatus = (Map<String, Object>) redisTemplate.opsForValue().get(ROOM_STATUS_KEY + room.getId());
        if (cachedStatus != null && (Integer) cachedStatus.get("status") != 0) {
            throw new BusinessException("该房间状态已变更,请刷新页面");
        }

        // 查询房型
        RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
        if (roomType == null) {
            throw new BusinessException("房型不存在");
        }

        // 计算天数和总价
        long days = ChronoUnit.DAYS.between(order.getCheckInDate(), order.getCheckOutDate());
        if (days <= 0) {
            throw new BusinessException("离店日期必须晚于入住日期");
        }
        order.setOrderNo("H" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        order.setRoomTypeId(roomType.getId());
        order.setPrice(roomType.getPrice());
        order.setDays((int) days);
        order.setTotalAmount(roomType.getPrice().multiply(BigDecimal.valueOf(days)));
        order.setOrderStatus(0); // 待确认
        order.setPayStatus(0);   // 未支付

        // 保存订单
        orderMapper.insert(order);

        // 更新房间状态为已预订
        room.setStatus(1);
        roomMapper.updateById(room);

        // 更新 Redis 缓存
        Map<String, Object> statusMap = new java.util.HashMap<>();
        statusMap.put("status", 1);
        redisTemplate.opsForValue().set(ROOM_STATUS_KEY + room.getId(), statusMap, 24, TimeUnit.HOURS);
        redisTemplate.delete("room:available");
        redisTemplate.delete("room:stats");

        return Map.of("orderNo", order.getOrderNo());
    }

    @Transactional
    public void updateOrderStatus(Long id, Integer status) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        order.setOrderStatus(status);

        // 根据状态变更房间状态
        Room room = roomMapper.selectById(order.getRoomId());
        if (room == null) {
            throw new BusinessException("房间不存在");
        }

        switch (status) {
            case 1: // 已确认(房间保持已预订)
                break;
            case 2: // 已入住
                room.setStatus(2);
                break;
            case 3: // 已退房
                room.setStatus(0);
                break;
            case 4: // 已取消
                // 取消订单,房间释放
                room.setStatus(0);
                break;
        }
        roomMapper.updateById(room);
        orderMapper.updateById(order);

        // 清理 Redis 缓存
        redisTemplate.delete(ROOM_STATUS_KEY + room.getId());
        redisTemplate.delete("room:available");
        redisTemplate.delete("room:stats");
    }

    @Transactional
    public void updatePayStatus(Long id, Integer payStatus) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setPayStatus(payStatus);
        orderMapper.updateById(order);
    }

    public PageResult<Order> page(long pageNum, long pageSize, String keyword, Integer orderStatus) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Order::getOrderNo, keyword)
                    .or().like(Order::getGuestName, keyword)
                    .or().like(Order::getGuestPhone, keyword));
        }
        if (orderStatus != null) {
            wrapper.eq(Order::getOrderStatus, orderStatus);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> result = orderMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public List<Map<String, Object>> listWithDetail() {
        return orderMapper.selectOrderWithDetail();
    }

    public Order getById(Long id) {
        return orderMapper.selectById(id);
    }

    @Transactional
    public void removeById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order != null && order.getOrderStatus() < 3) {
            // 未退房的订单删除时释放房间
            Room room = roomMapper.selectById(order.getRoomId());
            if (room != null) {
                room.setStatus(0);
                roomMapper.updateById(room);
                redisTemplate.delete(ROOM_STATUS_KEY + room.getId());
            }
        }
        orderMapper.deleteById(id);
    }
}
