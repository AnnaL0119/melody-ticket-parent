package com.melody.common.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.melody.common.Result;
import com.melody.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<String> handleBizException(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(BlockException.class)
    public Result<String> handleBlockException(BlockException e) {
        String message;
        if (e instanceof FlowException) {
            message = "请求过于频繁，请稍后再试";
        } else if (e instanceof DegradeException) {
            message = "服务已熔断，请稍后再试";
        } else if (e instanceof ParamFlowException) {
            message = "热点参数限流";
        } else {
            message = "请求被限流";
        }
        log.warn("Sentinel限流: {}", message);
        return Result.error(429, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统内部错误: " + e.getMessage());
    }
}
