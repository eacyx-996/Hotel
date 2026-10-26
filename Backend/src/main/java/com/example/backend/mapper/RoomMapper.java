package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface RoomMapper extends BaseMapper<Room> {

    @Select("SELECT r.*, rt.name AS room_type_name, rt.price AS room_type_price " +
            "FROM room r LEFT JOIN room_type rt ON r.room_type_id = rt.id " +
            "WHERE r.deleted = 0 " +
            "ORDER BY r.room_number")
    List<Map<String, Object>> selectRoomWithTypeName();

    @Select("SELECT COUNT(*) AS total, SUM(CASE WHEN r.status = 0 THEN 1 ELSE 0 END) AS available, " +
            "SUM(CASE WHEN r.status IN (1, 2) THEN 1 ELSE 0 END) AS occupied " +
            "FROM room r WHERE r.deleted = 0")
    Map<String, Object> selectRoomStats();
}
