package com.melody.ticket.mq;

import com.melody.common.Result;
import com.melody.common.exception.BizException;
import com.melody.ticket.client.ConcertFeignClient;
import com.melody.ticket.config.SeckillKeys;
import com.melody.ticket.entity.Order;
import com.melody.ticket.entity.Ticket;
import com.melody.ticket.mapper.OrderMapper;
import com.melody.ticket.mapper.TicketMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderConsumerTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private TicketMapper ticketMapper;
    @Mock
    private ConcertFeignClient concertFeignClient;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private OrderConsumer orderConsumer;

    private Map<String, Object> message;

    @BeforeEach
    void setUp() {
        message = new HashMap<>();
        message.put("userId", 1L);
        message.put("concertId", 100L);
        message.put("quantity", 1);
        message.put("orderNo", "ORD123456");
    }

    @Test
    @DisplayName("已消费过的消息直接跳过，不重复落单")
    void duplicateMessageIsSkipped() {
        when(redisTemplate.hasKey(SeckillKeys.done("ORD123456"))).thenReturn(true);

        orderConsumer.processOrder(message);

        verify(orderMapper, never()).insert(any(Order.class));
        verify(concertFeignClient, never()).decreaseStock(any(), any(), any());
    }

    @Test
    @DisplayName("正常消费：扣库存、落单、生成电子票并写入幂等标记")
    void normalConsumptionCreatesOrder() {
        when(redisTemplate.hasKey(SeckillKeys.done("ORD123456"))).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doReturn(Result.success(concert())).when(concertFeignClient).getConcertById(100L);
        when(concertFeignClient.decreaseStock(eq(100L), eq(1), eq("ORD123456")))
                .thenReturn(Result.success(true));

        orderConsumer.processOrder(message);

        verify(orderMapper).insert(any(Order.class));
        verify(ticketMapper).insert(any(Ticket.class));
        verify(valueOperations).set(eq(SeckillKeys.done("ORD123456")), eq("1"), eq(24L), any());
    }

    @Test
    @DisplayName("订单唯一索引冲突视为已处理，不再向上抛出触发重试")
    void duplicateKeyIsTreatedAsSuccess() {
        when(redisTemplate.hasKey(SeckillKeys.done("ORD123456"))).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doReturn(Result.success(concert())).when(concertFeignClient).getConcertById(100L);
        when(concertFeignClient.decreaseStock(eq(100L), eq(1), eq("ORD123456")))
                .thenReturn(Result.success(true));
        doThrow(new DuplicateKeyException("Duplicate entry 'ORD123456'"))
                .when(orderMapper).insert(any(Order.class));

        orderConsumer.processOrder(message);

        verify(valueOperations).set(eq(SeckillKeys.done("ORD123456")), eq("1"), eq(24L), any());
    }

    @Test
    @DisplayName("数据库库存扣减失败时抛出异常，交由 MQ 重试/死信补偿")
    void stockDeductFailureTriggersRetry() {
        when(redisTemplate.hasKey(SeckillKeys.done("ORD123456"))).thenReturn(false);
        doReturn(Result.success(concert())).when(concertFeignClient).getConcertById(100L);
        when(concertFeignClient.decreaseStock(eq(100L), eq(1), eq("ORD123456")))
                .thenReturn(Result.error(400, "库存不足"));

        assertThrows(BizException.class, () -> orderConsumer.processOrder(message));

        verify(orderMapper, never()).insert(any(Order.class));
        verify(valueOperations, never()).set(anyString(), anyString(), any(Long.class), any());
    }

    private Map<String, Object> concert() {
        Map<String, Object> data = new HashMap<>();
        data.put("price", "1280");
        data.put("name", "测试演唱会");
        return data;
    }
}
