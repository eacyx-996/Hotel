package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("room_type")
public class RoomType {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal area;
    private Integer capacity;
    private String facilities;
    private String image;
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
