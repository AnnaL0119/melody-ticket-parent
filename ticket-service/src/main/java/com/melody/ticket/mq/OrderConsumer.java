package com.melody.ticket.mq;

import com.melody.common.Result;
import com.melody.common.exception.BizException;
import com.melody.ticket.client.ConcertFeignClient;
import com.melody.ticket.entity.Order;
import com.melody.ticket.entity.Ticket;
import com.melody.ticket.mapper.OrderMapper;
import com.melody.ticket.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderMapper orderMapper;
    private final TicketMapper ticketMapper;
    private final ConcertFeignClient concertFeignClient;

    @RabbitListener(queues = "seckill.order.queue")
    @Transactional(rollbackFor = Exception.class)
    public void processOrder(Map<String, Object> message) {
        Long userId = Long.valueOf(message.get("userId").toString());
        Long concertId = Long.valueOf(message.get("concertId").toString());
        Integer quantity = Integer.valueOf(message.get("quantity").toString());
        String orderNo = (String) message.get("orderNo");

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

            // 2. 调用 concert-service 扣减数据库库存
            Result<Boolean> decreaseResult = concertFeignClient.decreaseStock(concertId, quantity);
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

            log.info("秒杀订单处理成功: orderNo={}", orderNo);

        } catch (Exception e) {
            log.error("秒杀订单处理失败: orderNo={}, error={}", orderNo, e.getMessage());
            throw e;
        }
    }

    private String generateTicketNo() {
        return "TCK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
