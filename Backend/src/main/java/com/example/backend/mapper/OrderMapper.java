package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT o.*, r.room_number, rt.name AS room_type_name " +
            "FROM orders o " +
            "LEFT JOIN room r ON o.room_id = r.id " +
            "LEFT JOIN room_type rt ON o.room_type_id = rt.id " +
            "WHERE o.deleted = 0 " +
            "ORDER BY o.create_time DESC")
    List<Map<String, Object>> selectOrderWithDetail();

    @Select("SELECT DATE(create_time) AS date, COUNT(*) AS order_count, SUM(total_amount) AS revenue " +
            "FROM orders WHERE deleted = 0 AND create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> selectDailyStats();

    @Select("SELECT order_status, COUNT(*) AS count FROM orders WHERE deleted = 0 GROUP BY order_status")
    List<Map<String, Object>> selectOrderStatusCount();
}
