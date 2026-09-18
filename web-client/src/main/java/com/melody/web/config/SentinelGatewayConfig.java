package com.melody.web.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.melody.web.handler.GatewayBlockExceptionHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentinelGatewayConfig {

    @PostConstruct
    public void init() {
        GatewayCallbackManager.setBlockHandler(new GatewayBlockExceptionHandler());
    }
}
