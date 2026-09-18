package com.melody.ticket.dto;

import lombok.Data;

@Data
public class CreateOrderDTO {
    private Long userId;
    private Long concertId;
    private Integer quantity;
}