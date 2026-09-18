package com.melody.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ticket")
public class Ticket {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String ticketNo;
    private Long orderId;
    private Long userId;
    private Long concertId;
    private String concertName;
    private String seatInfo;
    private String qrCode;
    private Integer status;  // 0未使用 1已使用 2已过期
    private LocalDateTime useTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}