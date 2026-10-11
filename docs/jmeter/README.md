# 秒杀链路压测手册

## 1. 前置条件

1. 启动 MySQL、Redis、RabbitMQ、Nacos，按根目录 README 启动 4 个后端服务（网关 8080）
2. 初始化数据库并导入演示数据（`docs/sql/01~03`）
3. **RabbitMQ 队列变更**：`seckill.order.queue` 新增了死信参数，若之前已创建过旧队列，先删除再启动：
   ```bash
   rabbitmqctl delete_queue seckill.order.queue
   ```
4. 初始化秒杀库存（把数据库库存同步到 Redis，不调用会一直返回"库存不足"）：
   ```bash
   curl -X POST "http://localhost:8080/api/tickets/seckill/stock/1?totalStock=100"
   ```

## 2. 压测计划说明

`seckill-load-test.jmx` 中每个虚拟用户执行：

```
注册独立账号 → 登录取 JWT → 携带 Token 秒杀下单 ×1
```

- 每个用户只抢一次，符合真实抢票场景（Lua 脚本禁止同一用户重复下单）
- 秒杀请求只统计 HTTP 200（正常业务响应，含"排队成功/库存不足"）与 429（Sentinel 限流）为成功
- 结束后自动打印业务分类计数：`queued / no_stock / rate_limited / duplicate / other`

## 3. 执行压测

```bash
# 轮次 A：验证 Sentinel 限流保护（网关默认秒杀 QPS 上限 50）
jmeter -n -t docs/jmeter/seckill-load-test.jmx -Jthreads=200 -Jrampup=10 -JconcertId=1 -l result-a.jtl

# 轮次 B：放开限流上限，验证高并发下不超卖（重启网关时加大上限）
# IDEA/命令行启动 gateway 时加：-Dsentinel.seckill.qps-limit=100000
jmeter -n -t docs/jmeter/seckill-load-test.jmx -Jthreads=200 -Jrampup=10 -JconcertId=1 -l result-b.jtl
```

参数说明：

| 参数 | 默认值 | 含义 |
|------|--------|------|
| `-Jthreads` | 200 | 并发用户数 |
| `-Jrampup` | 10 | 线程启动时间（秒） |
| `-JconcertId` | 1 | 目标演唱会 ID |
| `-Jhost` / `-Jport` | localhost / 8080 | 网关地址 |

## 4. 结果记录（写简历用这几个数）

打开 `result-a.jtl` / 在 GUI 中查看 Summary Report，记录：

| 指标 | 含义 |
|------|------|
| Samples | 总请求数 |
| Throughput (req/sec) | 系统吞吐 |
| Average / 90% Line | 平均响应时间 / 90 分位响应时间 |
| Error % | 非 200/429 的异常比例（应接近 0） |
| 分类计数 | queued（排队成功）、rate_limited（被限流）、no_stock（售罄） |

## 5. 无超卖验证（压测后必做）

等待 5~10 秒让 MQ 消费完毕，然后核对：

```bash
# 1) Redis 秒杀库存剩余
redis-cli GET seckill:stock:1

# 2) 数据库剩余库存
mysql -u root -p -e "SELECT remaining_stock FROM melody_concert.concert WHERE id=1;"

# 3) 成功落单数
mysql -u root -p -e "SELECT COUNT(*) FROM melody_ticket.order WHERE concert_id=1;"
```

判定标准（三者必须同时成立）：

```
初始库存 - Redis剩余  == JMeter 分类计数中的 queued        （预扣环节无超卖）
初始库存 - DB剩余    == 订单表中该演唱会的订单数           （落库环节无超卖）
Redis剩余  >= 0 且 DB剩余 >= 0                             （无负库存）
```

> 注意：消费是异步的，`queued` 数会在压测结束后几秒内持续增长到与订单数一致；若有订单进入死信队列（日志中可见 ERROR 告警），Redis 库存会被自动回滚，需等待回滚完成后再核对。
