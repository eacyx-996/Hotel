package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long roomId;
    private Long roomTypeId;
    private String guestName;
    private String guestPhone;
    private String guestIdCard;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer days;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private Integer payStatus;
    private Integer orderStatus;
    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String roomNumber;

    @TableField(exist = false)
    private String roomTypeName;
}
