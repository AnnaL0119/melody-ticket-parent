package com.melody.ticket.mq;

import com.melody.ticket.config.SeckillKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeckillDeadLetterConsumerTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SeckillDeadLetterConsumer consumer;

    private Map<String, Object> message;

    @BeforeEach
    void setUp() {
        message = new HashMap<>();
        message.put("userId", 7L);
        message.put("concertId", 100L);
        message.put("quantity", 2);
        message.put("orderNo", "ORD_DEAD_1");
    }

    @Test
    @DisplayName("死信消息触发库存回滚并释放限购标记")
    void deadLetterRollsBackRedisStock() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(SeckillKeys.stock(100L), 2)).thenReturn(42L);

        consumer.handleDeadLetter(message);

        verify(valueOperations).increment(SeckillKeys.stock(100L), 2);
        verify(redisTemplate).delete(SeckillKeys.user(100L, 7L));
    }
}
