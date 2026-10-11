package com.melody.ticket.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;

/**
 * 消息投递可靠性保障：
 * - confirm 回调：Broker 是否成功接收消息
 * - returns 回调：消息到达 Broker 但无法路由到队列时触发（配合 mandatory=true）
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitMQReliabilityConfig {

    private final RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void initCallbacks() {
        rabbitTemplate.setMandatory(true);

        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("消息已被 Broker 确认接收: correlation={}", correlationData);
            } else {
                log.error("消息投递失败（Broker 未确认）: correlation={}, cause={}", correlationData, cause);
            }
        });

        rabbitTemplate.setReturnsCallback(returned -> log.error(
                "消息无法路由（已退回）: exchange={}, routingKey={}, replyText={}",
                returned.getExchange(), returned.getRoutingKey(), returned.getReplyText()));
    }
}
