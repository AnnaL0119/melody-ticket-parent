package com.melody.ticket.mq;

import com.melody.ticket.config.RabbitMQConfig;
import com.melody.ticket.config.SeckillKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 死信队列消费者：处理重试耗尽仍失败的秒杀订单
 * 1. 回滚 Redis 预扣库存，保证未成功落单的请求不会永久占用库存
 * 2. 释放该用户的限购标记，允许用户重新参与秒杀
 * 3. 打印 ERROR 告警日志（订单号 + 演唱会 ID），供人工核对数据库库存与订单是否一致
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillDeadLetterConsumer {

    private final StringRedisTemplate redisTemplate;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_DLQ)
    public void handleDeadLetter(Map<String, Object> message) {
        Long userId = Long.valueOf(message.get("userId").toString());
        Long concertId = Long.valueOf(message.get("concertId").toString());
        Integer quantity = Integer.valueOf(message.get("quantity").toString());
        String orderNo = (String) message.get("orderNo");

        // 1. 回滚 Redis 预扣库存
        Long remaining = redisTemplate.opsForValue().increment(SeckillKeys.stock(concertId), quantity);

        // 2. 释放限购标记，用户可重新下单
        redisTemplate.delete(SeckillKeys.user(concertId, userId));

        // 3. 告警：跨服务（Redis/MySQL）库存可能不一致，需人工对账
        log.error("秒杀订单进入死信队列，已回滚 Redis 预扣库存，请人工对账: orderNo={}, userId={}, concertId={}, quantity={}, redis剩余库存={}",
                orderNo, userId, concertId, quantity, remaining);
    }
}
