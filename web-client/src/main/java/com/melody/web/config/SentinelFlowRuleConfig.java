package com.melody.web.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 网关侧流量治理规则：
 * 1. 秒杀接口 QPS 限流 —— 突发流量在网关直接拦截，保护 ticket-service 与数据库
 * 2. 秒杀接口熔断降级 —— 下游响应变慢（RT 超阈值比例达标）时快速失败，避免线程堆积
 */
@Slf4j
@Configuration
public class SentinelFlowRuleConfig {

    public static final String SECKILL_API_NAME = "seckillApi";

    /** 秒杀接口每秒放行的请求数，可通过配置动态调整 */
    @Value("${sentinel.seckill.qps-limit:50}")
    private int seckillQpsLimit;

    @PostConstruct
    public void initRules() {
        // 1. 将秒杀 URL 定义为一个 API 资源（前缀匹配），便于统一挂载限流/熔断规则
        ApiPathPredicateItem pathItem = new ApiPathPredicateItem()
                .setPattern("/api/tickets/seckill")
                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX);
        Set<ApiPredicateItem> predicateItems = new HashSet<>();
        predicateItems.add(pathItem);
        ApiDefinition seckillApi = new ApiDefinition(SECKILL_API_NAME).setPredicateItems(predicateItems);
        Set<ApiDefinition> apiDefinitions = new HashSet<>();
        apiDefinitions.add(seckillApi);
        GatewayApiDefinitionManager.loadApiDefinitions(apiDefinitions);

        // 2. 网关 QPS 限流：每秒最多放行 seckillQpsLimit 个请求，超出由 BlockRequestHandler 返回 429
        GatewayFlowRule flowRule = new GatewayFlowRule(SECKILL_API_NAME)
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(seckillQpsLimit)
                .setIntervalSec(1)
                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        Set<GatewayFlowRule> flowRules = new HashSet<>();
        flowRules.add(flowRule);
        GatewayRuleManager.loadRules(flowRules);

        // 3. 熔断降级：1s 统计窗口内 RT>200ms 的请求占比超过 60% 时，熔断 10s 快速失败
        DegradeRule degradeRule = new DegradeRule(SECKILL_API_NAME)
                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                .setCount(200)
                .setTimeWindow(10)
                .setStatIntervalMs(1000)
                .setMinRequestAmount(20)
                .setSlowRatioThreshold(0.6);
        DegradeRuleManager.loadRules(List.of(degradeRule));

        log.info("Sentinel 规则加载完成: seckillApi QPS限制={}, 熔断策略=RT>200ms占比>60%持续10s", seckillQpsLimit);
    }
}
