package com.melody.ticket.mq;

import com.melody.common.Result;
import com.melody.common.exception.BizException;
import com.melody.ticket.client.ConcertFeignClient;
import com.melody.ticket.config.SeckillKeys;
import com.melody.ticket.entity.Order;
import com.melody.ticket.entity.Ticket;
import com.melody.ticket.mapper.OrderMapper;
import com.melody.ticket.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀订单消费者
 * 幂等设计：Redis 消费标记（seckill:done:{orderNo}）拦截重复投递，
 *          数据库 order_no 唯一索引作为最终兜底，重复插入抛 DuplicateKeyException 视为已处理
 * 可靠性设计：消费失败抛异常触发 Spring AMQP 重试（3 次），仍失败则进入死信队列，
 *           由 {@link SeckillDeadLetterConsumer} 回滚 Redis 预扣库存并释放限购标记
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderMapper orderMapper;
    private final TicketMapper ticketMapper;
    private final ConcertFeignClient concertFeignClient;
    private final StringRedisTemplate redisTemplate;

    @RabbitListener(queues = "seckill.order.queue")
    @Transactional(rollbackFor = Exception.class)
    public void processOrder(Map<String, Object> message) {
        Long userId = Long.valueOf(message.get("userId").toString());
        Long concertId = Long.valueOf(message.get("concertId").toString());
        Integer quantity = Integer.valueOf(message.get("quantity").toString());
        String orderNo = (String) message.get("orderNo");

        String doneKey = SeckillKeys.done(orderNo);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(doneKey))) {
            log.info("重复消费，直接跳过: orderNo={}", orderNo);
            return;
        }

        log.info("消费秒杀订单: orderNo={}, userId={}, concertId={}, quantity={}", orderNo, userId, concertId, quantity);

        try {
            // 1. 获取演唱会信息（用于价格和名称）
            Result<?> concertResult = concertFeignClient.getConcertById(concertId);
            BigDecimal price = BigDecimal.valueOf(688);
            String concertName = "演唱会";
            if (concertResult.getCode() == 200 && concertResult.getData() != null) {
                Map<String, Object> concertData = (Map<String, Object>) concertResult.getData();
                price = new BigDecimal(concertData.getOrDefault("price", "688").toString());
                concertName = (String) concertData.getOrDefault("name", "演唱会");
            }

            // 2. 调用 concert-service 扣减数据库库存（orderNo 幂等键，重试不会重复扣减）
            Result<Boolean> decreaseResult = concertFeignClient.decreaseStock(concertId, quantity, orderNo);
            if (decreaseResult.getCode() != 200 || !decreaseResult.getData()) {
                log.error("扣减数据库库存失败: concertId={}, quantity={}", concertId, quantity);
                throw new BizException("扣减库存失败");
            }

            // 3. 创建订单
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setConcertId(concertId);
            order.setQuantity(quantity);
            order.setTotalAmount(price.multiply(BigDecimal.valueOf(quantity)));
            order.setStatus(1);
            orderMapper.insert(order);

            // 4. 创建电子票
            Ticket ticket = new Ticket();
            ticket.setTicketNo(generateTicketNo());
            ticket.setOrderId(order.getId());
            ticket.setUserId(userId);
            ticket.setConcertId(concertId);
            ticket.setConcertName(concertName);
            ticket.setStatus(0);
            ticketMapper.insert(ticket);

            // 5. 标记消费完成，后续重复投递直接跳过
            redisTemplate.opsForValue().set(doneKey, "1", 24, TimeUnit.HOURS);

            log.info("秒杀订单处理成功: orderNo={}", orderNo);

        } catch (DuplicateKeyException e) {
            // order_no 唯一索引兜底：说明该订单已落库，按成功处理，避免重试导致重复扣库存
            log.warn("订单已存在（唯一索引幂等拦截）: orderNo={}", orderNo);
            redisTemplate.opsForValue().set(doneKey, "1", 24, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("秒杀订单处理失败（等待重试）: orderNo={}, error={}", orderNo, e.getMessage());
            throw e;
        }
    }

    private String generateTicketNo() {
        return "TCK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
