package com.melody.ticket.client;

import com.melody.common.Result;
import com.melody.ticket.client.ConcertFeignFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "concert-service", fallbackFactory = ConcertFeignFallbackFactory.class)
public interface ConcertFeignClient {

    @GetMapping("/concerts/{id}")
    Result<?> getConcertById(@PathVariable("id") Long id);

    @GetMapping("/concerts/{id}/stock")
    Result<Integer> getRemainingStock(@PathVariable("id") Long id);

    /**
     * 扣减库存，bizNo（订单号）作为幂等键：同一订单重复调用只扣一次
     */
    @GetMapping("/concerts/decrease-stock")
    Result<Boolean> decreaseStock(
            @RequestParam("concertId") Long concertId,
            @RequestParam("quantity") Integer quantity,
            @RequestParam(value = "bizNo", required = false) String bizNo
    );

    @GetMapping("/concerts/increase-stock")
    Result<Boolean> increaseStock(
            @RequestParam("concertId") Long concertId,
            @RequestParam("quantity") Integer quantity
    );
}