package com.melody.concert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melody.concert.entity.Concert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ConcertMapper extends BaseMapper<Concert> {

    @Update("UPDATE concert SET remaining_stock = remaining_stock - #{quantity} " +
            "WHERE id = #{concertId} AND remaining_stock >= #{quantity}")
    int decreaseStock(@Param("concertId") Long concertId,
                      @Param("quantity") Integer quantity);

    @Update("UPDATE concert SET remaining_stock = remaining_stock + #{quantity} " +
            "WHERE id = #{concertId}")
    int increaseStock(@Param("concertId") Long concertId,
                      @Param("quantity") Integer quantity);
}