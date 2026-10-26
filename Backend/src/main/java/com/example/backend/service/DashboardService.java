package com.example.backend.service;

import com.example.backend.mapper.OrderMapper;
import com.example.backend.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderMapper orderMapper;
    private final RoomMapper roomMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY = "dashboard:stats";

    public Map<String, Object> getStats() {
        @SuppressWarnings("unchecked")
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) return cached;

        Map<String, Object> result = new HashMap<>();

        // 客房统计
        Map<String, Object> roomStats = roomMapper.selectRoomStats();
        result.put("roomStats", roomStats);

        // 总订单数
        Long totalOrders = orderMapper.selectCount(null);
        result.put("totalOrders", totalOrders);

        // 总营收(已支付)
        List<Map<String, Object>> allOrders = orderMapper.selectOrderWithDetail();
        double totalRevenue = allOrders.stream()
                .filter(o -> o.get("pay_status") != null && (Integer) o.get("pay_status") == 1)
                .mapToDouble(o -> ((Number) o.get("total_amount")).doubleValue())
                .sum();
        result.put("totalRevenue", totalRevenue);

        // 订单状态统计
        List<Map<String, Object>> statusCount = orderMapper.selectOrderStatusCount();
        result.put("orderStatusCount", statusCount);

        // 近7天订单和营收
        List<Map<String, Object>> dailyStats = orderMapper.selectDailyStats();
        result.put("dailyStats", dailyStats);

        redisTemplate.opsForValue().set(CACHE_KEY, result, 3, TimeUnit.MINUTES);
        return result;
    }
}
