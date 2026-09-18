package com.melody.ticket.dto;

import com.melody.ticket.entity.Ticket;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long concertId;
    private String concertName;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private List<Ticket> tickets;
}