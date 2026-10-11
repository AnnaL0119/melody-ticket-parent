package com.melody.ticket.config;

/**
 * 秒杀相关 Redis key 统一定义，保证生产方与消费方/补偿方使用同一套编码
 */
public final class SeckillKeys {

    public static final String STOCK_PREFIX = "seckill:stock:";
    public static final String USER_PREFIX = "seckill:user:";
    public static final String DONE_PREFIX = "seckill:done:";

    private SeckillKeys() {
    }

    public static String stock(Long concertId) {
        return STOCK_PREFIX + concertId;
    }

    public static String user(Long concertId, Long userId) {
        return USER_PREFIX + concertId + ":" + userId;
    }

    public static String done(String orderNo) {
        return DONE_PREFIX + orderNo;
    }
}
