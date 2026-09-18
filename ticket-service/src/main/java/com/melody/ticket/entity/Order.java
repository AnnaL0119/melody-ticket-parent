package com.melody.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long userId;
    private Long concertId;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Integer status;  // 0待支付 1已支付 2已取消 3已退款
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}