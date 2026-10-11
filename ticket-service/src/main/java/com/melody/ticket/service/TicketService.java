package com.melody.ticket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.melody.common.Result;
import com.melody.common.exception.BizException;
import com.melody.ticket.client.ConcertFeignClient;
import com.melody.ticket.config.RabbitMQConfig;
import com.melody.ticket.config.SeckillKeys;
import com.melody.ticket.dto.CreateOrderDTO;
import com.melody.ticket.dto.OrderVO;
import com.melody.ticket.entity.Order;
import com.melody.ticket.entity.Ticket;
import com.melody.ticket.mapper.OrderMapper;
import com.melody.ticket.mapper.TicketMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final OrderMapper orderMapper;
    private final TicketMapper ticketMapper;
    private final ConcertFeignClient concertFeignClient;
    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final String SECKILL_STOCK_KEY_PREFIX = SeckillKeys.STOCK_PREFIX;
    private static final String SECKILL_USER_KEY_PREFIX = SeckillKeys.USER_PREFIX;
    private DefaultRedisScript<List> seckillScript;

    @PostConstruct
    public void init() {
        seckillScript = new DefaultRedisScript<>();
        seckillScript.setLocation(new ClassPathResource("seckill.lua"));
        seckillScript.setResultType(List.class);
    }

    /**
     * 秒杀下单（Redis Lua 预扣 + RabbitMQ 异步处理）
     */
    public Map<String, Object> seckill(CreateOrderDTO dto) {
        String orderNo = generateOrderNo();
        String userKey = SECKILL_USER_KEY_PREFIX + dto.getConcertId() + ":" + dto.getUserId();
        String stockKey = SECKILL_STOCK_KEY_PREFIX + dto.getConcertId();

        // 1. Lua 脚本原子操作：检查重复 + 预扣库存
        List<Object> result = redisTemplate.execute(seckillScript,
                Arrays.asList(userKey, stockKey),
                String.valueOf(dto.getQuantity()), orderNo);

        Long success = (Long) result.get(0);
        String message = (String) result.get(1);

        if (success != 1) {
            throw new BizException(message);
        }

        // 2. 发送到消息队列异步处理（CorrelationData 携带订单号，配合 publisher confirm 记录投递结果）
        Map<String, Object> msg = new HashMap<>();
        msg.put("userId", dto.getUserId());
        msg.put("concertId", dto.getConcertId());
        msg.put("quantity", dto.getQuantity());
        msg.put("orderNo", orderNo);
        msg.put("timestamp", System.currentTimeMillis());

        rabbitTemplate.convertAndSend(RabbitMQConfig.SECKILL_EXCHANGE,
                RabbitMQConfig.SECKILL_ROUTING_KEY, msg, new CorrelationData(orderNo));

        log.info("秒杀订单已排队: orderNo={}, userId={}, concertId={}", orderNo, dto.getUserId(), dto.getConcertId());

        // 3. 返回排队中状态
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("orderNo", orderNo);
        resultMap.put("status", 0);
        resultMap.put("message", "订单已提交，正在处理中");
        return resultMap;
    }

    /**
     * 查询订单状态（用于前端轮询）
     */
    public OrderVO getOrderByOrderNo(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) return null;
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    /**
     * 初始化秒杀库存到 Redis
     */
    public void initSeckillStock(Long concertId, Integer totalStock) {
        String key = SECKILL_STOCK_KEY_PREFIX + concertId;
        redisTemplate.opsForValue().set(key, String.valueOf(totalStock));
        log.info("初始化秒杀库存: concertId={}, stock={}", concertId, totalStock);
    }

    /**
     * 创建订单（带库存扣减）
     * 事务范围：扣减库存(concert-service) + 创建订单(ticket-service) + 生成电子票(ticket-service)
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(CreateOrderDTO dto) {
        log.info("创建订单: userId={}, concertId={}, quantity={}", dto.getUserId(), dto.getConcertId(), dto.getQuantity());
        String orderNo = generateOrderNo();

        // 1. 获取演唱会信息
        Result<?> concertResult = concertFeignClient.getConcertById(dto.getConcertId());
        if (concertResult.getCode() == 503) {
            throw new BizException(503, "演唱会服务不可用，请稍后重试");
        }
        if (concertResult.getCode() != 200 || concertResult.getData() == null) {
            throw new BizException("演唱会不存在");
        }
        Map<String, Object> concertData = (Map<String, Object>) concertResult.getData();
        BigDecimal price = new BigDecimal(concertData.getOrDefault("price", "688").toString());
        String concertName = (String) concertData.getOrDefault("name", "演唱会");

        // 2. 获取余票
        Result<Integer> stockResult = concertFeignClient.getRemainingStock(dto.getConcertId());
        if (stockResult.getCode() == 503) {
            throw new BizException(503, "演唱会服务不可用，请稍后重试");
        }
        if (stockResult.getCode() != 200 || stockResult.getData() < dto.getQuantity()) {
            throw new BizException("库存不足");
        }

        // 3. 扣减库存（orderNo 作为幂等键，接口重试不会重复扣减）
        Result<Boolean> decreaseResult = concertFeignClient.decreaseStock(dto.getConcertId(), dto.getQuantity(), orderNo);
        if (decreaseResult.getCode() == 503) {
            throw new BizException(503, "演唱会服务不可用，请稍后重试");
        }
        if (decreaseResult.getCode() != 200 || !decreaseResult.getData()) {
            throw new BizException("扣减库存失败");
        }

        // 4. 计算总金额
        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(dto.getQuantity()));

        // 5. 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(dto.getUserId());
        order.setConcertId(dto.getConcertId());
        order.setQuantity(dto.getQuantity());
        order.setTotalAmount(totalAmount);
        order.setStatus(0);

        orderMapper.insert(order);

        // 6. 创建电子票
        Ticket ticket = new Ticket();
        ticket.setTicketNo(generateTicketNo());
        ticket.setOrderId(order.getId());
        ticket.setUserId(dto.getUserId());
        ticket.setConcertId(dto.getConcertId());
        ticket.setConcertName(concertName);
        ticket.setStatus(0);

        ticketMapper.insert(ticket);

        log.info("订单创建成功: orderNo={}, userId={}", order.getOrderNo(), dto.getUserId());

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    /**
     * 获取用户订单列表
     */
    public List<OrderVO> getUserOrders(Long userId) {
        List<Order> orders = orderMapper.selectByUserId(userId);
        return orders.stream()
                .map(order -> {
                    OrderVO vo = new OrderVO();
                    BeanUtils.copyProperties(order, vo);
                    List<Ticket> tickets = ticketMapper.selectList(
                            new LambdaQueryWrapper<Ticket>().eq(Ticket::getOrderId, order.getId())
                    );
                    vo.setTickets(tickets);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取订单详情
     */
    public OrderVO getOrderById(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("订单不存在");
        }
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    /**
     * 取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BizException("订单状态不允许取消");
        }

        // 更新订单状态
        orderMapper.updateStatus(orderId, 2);

        // 恢复库存
        Result<Boolean> result = concertFeignClient.increaseStock(order.getConcertId(), order.getQuantity());
        if (result.getCode() != 200) {
            log.warn("恢复库存失败: concertId={}, quantity={}", order.getConcertId(), order.getQuantity());
        }

        log.info("订单取消成功: orderId={}, userId={}", orderId, userId);
    }

    /**
     * 支付订单（模拟支付）
     */
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BizException("订单状态不允许支付");
        }

        // 更新订单状态为已支付
        orderMapper.updateStatusWithPayTime(orderId, 1);

        // 更新电子票状态，生成二维码凭证
        List<Ticket> tickets = ticketMapper.selectList(
                new LambdaQueryWrapper<Ticket>().eq(Ticket::getOrderId, orderId)
        );
        for (Ticket ticket : tickets) {
            ticket.setStatus(0);
            ticket.setQrCode("TICKET:" + ticket.getTicketNo());
            ticketMapper.updateById(ticket);
        }

        log.info("订单支付成功: orderId={}, userId={}", orderId, userId);
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateTicketNo() {
        return "TCK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}