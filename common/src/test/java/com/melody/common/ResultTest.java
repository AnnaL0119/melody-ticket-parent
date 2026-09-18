package com.melody.common;

import com.melody.common.exception.BizException;
import com.melody.common.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResultTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("success 返回 200 与数据")
    void successCarriesData() {
        Result<String> result = Result.success("ok");

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("ok", result.getData());
    }

    @Test
    @DisplayName("error 返回自定义状态码且 data 为空")
    void errorCarriesCode() {
        Result<String> result = Result.error(401, "未登录");

        assertEquals(401, result.getCode());
        assertEquals("未登录", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("业务异常被全局处理器映射为其携带的状态码")
    void bizExceptionIsMappedToItsCode() {
        Result<String> result = handler.handleBizException(new BizException(403, "无权限"));

        assertEquals(403, result.getCode());
        assertEquals("无权限", result.getMessage());
    }

    @Test
    @DisplayName("未知异常统一兜底为 500")
    void unknownExceptionIsMappedTo500() {
        Result<String> result = handler.handleException(new IllegalStateException("boom"));

        assertEquals(500, result.getCode());
    }
}
