package com.melody.ticket.mapper;

import com.melody.ticket.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO `order`(order_no, user_id, concert_id, quantity, total_amount, status, create_time) " +
            "VALUES(#{orderNo}, #{userId}, #{concertId}, #{quantity}, #{totalAmount}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Select("SELECT * FROM `order` WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Order> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM `order` WHERE id = #{id}")
    Order selectById(@Param("id") Long id);

    @Select("SELECT * FROM `order` WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    @Update("UPDATE `order` SET status = #{status}, cancel_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE `order` SET status = #{status}, pay_time = NOW() WHERE id = #{id}")
    int updateStatusWithPayTime(@Param("id") Long id, @Param("status") Integer status);
}