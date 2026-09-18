package com.melody.concert.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ConcertDTO {
    private Long id;
    private String name;
    private String singer;
    private String venue;
    private String city;
    private LocalDateTime showTime;
    private Integer remainingStock;
    private BigDecimal price;
    private String coverUrl;
    private Integer totalStock;      // 添加这个字段
}