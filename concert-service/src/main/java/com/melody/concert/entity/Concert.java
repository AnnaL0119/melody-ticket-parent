package com.melody.concert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("concert")
public class Concert {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String singer;
    private String venue;
    private String city;
    private LocalDateTime showTime;
    private Integer totalStock;
    private Integer remainingStock;
    private BigDecimal price;
    private String coverUrl;
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}