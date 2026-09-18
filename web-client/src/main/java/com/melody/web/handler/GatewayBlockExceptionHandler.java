package com.melody.web.handler;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class GatewayBlockExceptionHandler implements BlockRequestHandler {

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable ex) {
        String errorMsg;
        if (ex instanceof FlowException) {
            errorMsg = "请求过于频繁，请稍后再试";
        } else if (ex instanceof DegradeException) {
            errorMsg = "服务已熔断，请稍后再试";
        } else if (ex instanceof ParamFlowException) {
            errorMsg = "热点参数限流";
        } else {
            errorMsg = "请求被限流";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 429);
        result.put("message", errorMsg);
        result.put("data", null);

        return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result);
    }
}
