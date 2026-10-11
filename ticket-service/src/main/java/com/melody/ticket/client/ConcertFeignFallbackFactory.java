package com.melody.ticket.client;

import com.melody.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConcertFeignFallbackFactory implements FallbackFactory<ConcertFeignClient> {

    @Override
    public ConcertFeignClient create(Throwable cause) {
        log.error("concert-service 调用失败: {}", cause.getMessage());

        return new ConcertFeignClient() {
            @Override
            public Result<?> getConcertById(Long id) {
                return Result.error(503, "演唱会服务不可用，请稍后重试");
            }

            @Override
            public Result<Integer> getRemainingStock(Long id) {
                return Result.error(503, "演唱会服务不可用，请稍后重试");
            }

            @Override
            public Result<Boolean> decreaseStock(Long concertId, Integer quantity, String bizNo) {
                return Result.error(503, "演唱会服务不可用，请稍后重试");
            }

            @Override
            public Result<Boolean> increaseStock(Long concertId, Integer quantity) {
                return Result.error(503, "演唱会服务不可用，请稍后重试");
            }
        };
    }
}