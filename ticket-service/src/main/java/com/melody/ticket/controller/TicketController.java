package com.melody.ticket.controller;

import com.melody.common.Result;
import com.melody.ticket.dto.CreateOrderDTO;
import com.melody.ticket.dto.OrderVO;
import com.melody.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/orders")
    public Result<OrderVO> createOrder(@RequestBody CreateOrderDTO dto) {
        return Result.success(ticketService.createOrder(dto));
    }

    @PostMapping("/seckill")
    public Result<Map<String, Object>> seckill(@RequestBody CreateOrderDTO dto) {
        return Result.success(ticketService.seckill(dto));
    }

    @GetMapping("/orders/status/{orderNo}")
    public Result<OrderVO> getOrderStatus(@PathVariable("orderNo") String orderNo) {
        OrderVO vo = ticketService.getOrderByOrderNo(orderNo);
        if (vo == null) {
            return Result.error(404, "订单不存在或正在处理中");
        }
        return Result.success(vo);
    }

    // 修复：添加 @PathVariable 的 name 属性
    @GetMapping("/orders/user/{userId}")
    public Result<List<OrderVO>> getUserOrders(@PathVariable(name = "userId") Long userId) {
        return Result.success(ticketService.getUserOrders(userId));
    }

    // 修复：添加 @PathVariable 的 name 属性
    @GetMapping("/orders/{orderId}")
    public Result<OrderVO> getOrderById(
            @PathVariable(name = "orderId") Long orderId,
            @RequestParam(name = "userId") Long userId) {
        return Result.success(ticketService.getOrderById(orderId, userId));
    }

    // 修复：添加 @RequestParam 的 name 属性
    @PutMapping("/orders/{orderId}/cancel")
    public Result<Void> cancelOrder(
            @PathVariable(name = "orderId") Long orderId,
            @RequestParam(name = "userId") Long userId) {
        ticketService.cancelOrder(orderId, userId);
        return Result.success("取消成功", null);
    }

    @PutMapping("/orders/{orderId}/pay")
    public Result<Void> payOrder(
            @PathVariable(name = "orderId") Long orderId,
            @RequestParam(name = "userId") Long userId) {
        ticketService.payOrder(orderId, userId);
        return Result.success("支付成功", null);
    }

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("ticket-service is running!");
    }
}