# 🎵 LinMelodyTicket - 旋律票务系统

基于 **Spring Cloud Alibaba** 微服务架构的在线音乐会票务系统，涵盖用户管理、演唱会展示、在线购票、秒杀抢票、电子票管理等完整业务流程。

---

## 📋 项目简介

LinMelodyTicket 是一个前后端分离的分布式票务系统，采用 Spring Cloud Alibaba 微服务架构，实现了以下核心功能：

- **用户服务**：注册 / 登录 / JWT 鉴权
- **演唱会服务**：演唱会管理、库存管理、搜索
- **票务服务**：普通下单、秒杀抢票（Redis + RabbitMQ 异步）、电子票生成
- **API 网关**：统一入口、路由转发、Sentinel 限流熔断
- **前端**：Vue 3 + Element Plus 响应式 SPA

---

## 🏗 技术栈

| 层级 | 技术 |
|------|------|
| **后端框架** | Spring Boot 3.2.5, Spring Cloud 2023.0.1, Spring Cloud Alibaba 2023.0.1.0 |
| **服务注册/配置** | Nacos 2.3.2 |
| **API 网关** | Spring Cloud Gateway |
| **服务调用** | OpenFeign + Spring Cloud LoadBalancer |
| **限流熔断** | Sentinel 1.8.6 |
| **分布式事务** | Seata 2.0.0 |
| **ORM** | MyBatis-Plus 3.5.7 |
| **数据库** | MySQL 8.0 |
| **缓存** | Redis 7 |
| **消息队列** | RabbitMQ（秒杀异步处理） |
| **链路追踪** | SkyWalking 9.7.0 |
| **前端** | Vue 3.4 + Vite 5.2 + Element Plus 2.7 |
| **认证** | JWT (HS256) + BCrypt |
| **测试** | JUnit 5 + Mockito + Spring Boot Test |
| **容器化** | Docker + Docker Compose |

---

## 📁 项目结构

```
melody-ticket-parent/
├── .env.example               # 环境变量模板（密钥/密码不入库）
├── common/                    # 公共模块（Result封装、全局异常处理 + 单元测试）
├── user-service/              # 用户服务 (port: 8081)
│   └── controller/service/mapper/entity/utils(JWT, BCrypt) + 单元测试
├── concert-service/           # 演唱会服务 (port: 8082)
│   └── controller/service/mapper/entity
├── ticket-service/            # 票务服务 (port: 8083)
│   └── controller/service/mapper/entity/feign/mq
│   └── seckill.lua            # 秒杀 Redis Lua 脚本
├── web-client/                # API 网关 (port: 8080)
│   └── Spring Cloud Gateway + Sentinel
├── web-vue/                   # Vue 3 前端
│   └── src/views/ (Login, Register, Home, ConcertDetail, UserOrders, AdminConcerts, MusicPlayer)
├── docker/                    # Docker 部署配置
│   ├── docker-compose.yml
│   ├── Dockerfile.* (4个服务)
│   ├── init.sql
│   └── start.sh
├── docs/sql/                  # 数据库初始化脚本
│   ├── 01-create-databases.sql
│   ├── 02-create-tables.sql
│   └── 03-insert-test-data.sql
├── seata/                     # Seata 分布式事务配置
└── skywalking/                # SkyWalking 链路追踪配置
```

---

## 🚀 快速启动

### 方式一：本地开发环境（推荐）

#### 1. 前置依赖

确保本地已安装并启动以下服务：

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 & 秒杀库存 |
| RabbitMQ | 5672 | 消息队列 |
| Nacos | 8848 | 服务注册发现 |
| Sentinel Dashboard | 8010 | 限流熔断控制台（可选） |

#### 2. 配置环境变量

所有敏感配置（数据库密码、JWT 密钥等）均通过环境变量注入，仓库中不保存任何明文密钥：

```bash
# 复制模板并填入真实值
cp .env.example .env

# 至少需要设置这两项（JWT 密钥建议 32 字节以上）
# MYSQL_PASSWORD=你的MySQL密码
# JWT_SECRET=$(openssl rand -base64 32)

# Linux / macOS
set -a && source .env && set +a

# Windows PowerShell
Get-Content .env | ForEach-Object { if ($_ -match '^\s*([^#=]+)=(.*)$') { [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process') } }
```

也可直接在 IDEA 的 Run Configuration → Environment variables 中配置。缺少 `MYSQL_PASSWORD` 或 `JWT_SECRET` 时服务会启动失败（快速失败，避免带着默认密钥运行）。

#### 3. 初始化数据库

在 MySQL 中执行：

```bash
# 创建数据库
mysql -u root -p < docs/sql/01-create-databases.sql

# 创建表
mysql -u root -p < docs/sql/02-create-tables.sql

# 插入测试数据
mysql -u root -p < docs/sql/03-insert-test-data.sql
```

#### 4. 启动后端服务（按顺序）

```bash
# 1. 安装公共模块
cd common && mvn clean install -DskipTests

# 2. 启动用户服务
cd user-service && mvn spring-boot:run

# 3. 启动演唱会服务
cd concert-service && mvn spring-boot:run

# 4. 启动票务服务
cd ticket-service && mvn spring-boot:run

# 5. 启动 API 网关
cd web-client && mvn spring-boot:run
```

> 💡 也可以在 IDEA 中同时配置 4 个 Spring Boot Run Configuration 并行启动。

#### 5. 启动前端

```bash
cd web-vue

# 安装依赖
npm install

# 启动开发服务器（端口 3000，自动代理到网关 8080）
npm run dev
```

#### 6. 访问系统

打开浏览器访问 **http://localhost:3000**

演示账号见初始化脚本 `docs/sql/03-insert-test-data.sql`（含一个普通用户与一个管理员），账号密码仅用于本地体验，部署前请重置。

---

### 方式二：Docker Compose 一键部署

```bash
cd docker

# 配置环境变量（compose 自动读取 docker/.env）
cp ../.env.example .env
vim .env   # 填入 MYSQL_ROOT_PASSWORD、JWT_SECRET、SW_DATA_SOURCE_PASSWORD 等

# 构建并启动所有服务（11个容器）
docker-compose up -d --build

# 查看服务状态
bash start.sh status

# 查看日志
bash start.sh logs

# 停止所有服务
bash start.sh down
```

> ⚠️ Docker 部署需要较高资源（建议 8GB+ 内存），SkyWalking 相关配置需根据实际情况调整。

---

## 📡 服务端口一览

| 服务 | 端口 | 说明 |
|------|------|------|
| Gateway | 8080 | API 网关统一入口 |
| user-service | 8081 | 用户服务 |
| concert-service | 8082 | 演唱会服务 |
| ticket-service | 8083 | 票务服务 |
| Nacos | 8848 | 服务注册中心 |
| Sentinel | 8010 | 限流熔断控制台 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| RabbitMQ | 5672 | 消息队列 |

---

## 🔌 API 接口

所有请求通过 Gateway（8080）统一入口，路由前缀 `/api/`。

### 用户服务 `/api/users/`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/users/register` | 用户注册 |
| POST | `/users/login` | 用户登录（返回 JWT） |
| GET | `/users/me` | 获取当前用户信息 |
| GET | `/users/{id}` | 根据 ID 获取用户 |

### 演唱会服务 `/api/concerts/`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/concerts` | 演唱会列表 |
| GET | `/concerts/{id}` | 演唱会详情 |
| GET | `/concerts/{id}/stock` | 查询余票 |
| GET | `/concerts/decrease-stock` | 扣减库存 |
| GET | `/concerts/increase-stock` | 恢复库存 |
| POST | `/concerts` | 创建演唱会（管理员） |
| PUT | `/concerts/{id}` | 更新演唱会（管理员） |
| DELETE | `/concerts/{id}` | 删除演唱会（管理员） |

### 票务服务 `/api/tickets/`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/tickets/orders` | 普通下单 |
| POST | `/tickets/seckill` | 秒杀下单 |
| GET | `/tickets/orders/status/{orderNo}` | 查询订单状态 |
| GET | `/tickets/orders/user/{userId}` | 用户订单列表 |
| GET | `/tickets/orders/{orderId}` | 订单详情 |
| PUT | `/tickets/orders/{orderId}/pay` | 支付订单（模拟） |
| PUT | `/tickets/orders/{orderId}/cancel` | 取消订单 |

---

## 🗄 数据库设计

### melody_user - 用户库

| 表名 | 说明 |
|------|------|
| `user` | 用户表（id, username, password, nickname, role, status） |

### melody_concert - 演唱会库

| 表名 | 说明 |
|------|------|
| `concert` | 演唱会表（id, name, singer, venue, city, show_time, price, total_stock, remaining_stock, status） |

### melody_ticket - 票务库

| 表名 | 说明 |
|------|------|
| `order` | 订单表（id, order_no, user_id, concert_id, status: 0待支付/1已支付/2已取消/3已退款） |
| `ticket` | 电子票表（id, ticket_no, order_id, qr_code, status: 0未使用/1已使用/2已过期） |

---

## 📐 系统架构

```
                          ┌─────────────┐
                          │   浏览器     │
                          │  Vue 3 SPA  │
                          └──────┬──────┘
                                 │
                          ┌──────▼──────┐
                          │   Gateway   │
                          │  (port:8080)│
                          │  路由+限流   │
                          └──────┬──────┘
                    ┌────────────┼────────────┐
              ┌─────▼─────┐ ┌───▼────┐ ┌─────▼─────┐
              │   User    │ │Concert │ │  Ticket   │
              │ Service   │ │Service │ │  Service  │
              │ (port:8081│ │(port:  │ │(port:8083)│
              └─────┬─────┘ │ 8082)  └─────┬─────┘
                    │       └───┬────┘       │
              ┌─────▼─────┐    │      ┌─────▼─────┐
              │melody_user│    │      │melody_ticket│
              │   (MySQL) │    │      │   (MySQL)   │
              └───────────┘    │      └───────────┘
                         ┌─────▼──────┐
                         │melody_concert│
                         │   (MySQL)   │
                         └────────────┘

  基础设施: Nacos(注册) | Sentinel(限流) | Redis(缓存) | RabbitMQ(MQ) | SkyWalking(链路)
```

---

## ✅ 单元测试

项目使用 JUnit 5 + Mockito + Spring Boot Test，核心工具类与公共模块均有单元测试覆盖：

| 测试类 | 覆盖内容 |
|--------|----------|
| `JwtUtilTest` | Token 签发与解析、密钥不匹配拒绝、篡改 Token 拒绝、过期 Token 拒绝、非法格式兜底 |
| `PasswordEncoderTest` | BCrypt 加密/校验、随机盐、错误密码拒绝 |
| `ResultTest` | 统一响应封装、全局异常处理器（业务异常状态码映射、未知异常 500 兜底） |

```bash
# 根目录执行，运行全部模块测试
mvn test
```

---

## ⚙️ 配置说明

所有敏感配置通过环境变量注入，模板见 `.env.example`（`.env` 已加入 `.gitignore`）：

| 环境变量 | 说明 | 必填 |
|----------|------|------|
| `MYSQL_PASSWORD` | 各服务连接 MySQL 的密码 | 是 |
| `JWT_SECRET` | JWT 签名密钥（HS256，≥32 字节） | 是 |
| `NACOS_SERVER_ADDR` / `NACOS_USERNAME` / `NACOS_PASSWORD` | Nacos 注册中心连接信息 | 否（默认 localhost + nacos） |
| `SEATA_ENABLED` | Seata 分布式事务开关 | 否（默认 false） |
| `MYSQL_ROOT_PASSWORD` / `SW_DATA_SOURCE_PASSWORD` 等 | Docker Compose 部署所需变量 | Compose 部署时 |

其他说明：

1. **控制台**：Nacos `http://localhost:8848/nacos`、Sentinel `http://localhost:8010`，账号密码以各自中间件的部署配置为准，生产环境务必修改默认凭据
2. **RabbitMQ**：本地需提前安装或通过 Docker 启动（秒杀异步链路依赖）
3. **前端代理**：开发模式下 Vite 将 `/api` 代理到 `localhost:8080`（见 `vite.config.js`）
4. **演示数据**：`docs/sql/03-insert-test-data.sql` 中的账号仅用于本地体验，部署前请重置密码

---

## 📄 License

MIT License

---

> 🎶 LinMelodyTicket — 让每一场演出都不被辜负
