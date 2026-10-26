package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("room")
public class Room {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roomNumber;
    private Long roomTypeId;
    private Integer floor;
    private Integer status;
    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String roomTypeName;

    @TableField(exist = false)
    private java.math.BigDecimal roomTypePrice;
}
