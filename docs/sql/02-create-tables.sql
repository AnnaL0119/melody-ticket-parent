-- =============================================
-- MelodyTicket 数据表创建脚本
-- 在每个对应的数据库中执行
-- =============================================

-- ==================== melody_user 库 ====================

USE melody_user;

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色: USER普通用户, ADMIN管理员',
    `status` INT DEFAULT 1 COMMENT '状态: 1正常, 0禁用',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除: 0未删除, 1已删除',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- ==================== melody_concert 库 ====================

USE melody_concert;

CREATE TABLE IF NOT EXISTS `concert` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(255) NOT NULL COMMENT '演唱会名称',
    `singer` VARCHAR(255) DEFAULT NULL COMMENT '歌手',
    `venue` VARCHAR(255) DEFAULT NULL COMMENT '场馆',
    `city` VARCHAR(255) DEFAULT NULL COMMENT '城市',
    `show_time` DATETIME DEFAULT NULL COMMENT '演出时间',
    `total_stock` INT DEFAULT 0 COMMENT '总库存',
    `remaining_stock` INT DEFAULT 0 COMMENT '剩余库存',
    `price` DECIMAL(10,2) DEFAULT NULL COMMENT '票价',
    `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL',
    `status` INT DEFAULT 1 COMMENT '状态: 1上架, 0下架',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除: 0未删除, 1已删除',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_show_time` (`show_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='演唱会表';


-- ==================== melody_ticket 库 ====================

USE melody_ticket;

CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(64) DEFAULT NULL COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `concert_id` BIGINT NOT NULL COMMENT '演唱会ID',
    `quantity` INT DEFAULT 1 COMMENT '购买数量',
    `total_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '总金额',
    `status` INT DEFAULT 0 COMMENT '状态: 0待支付, 1已支付, 2已取消, 3已退款',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_concert_id` (`concert_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE IF NOT EXISTS `ticket` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `ticket_no` VARCHAR(64) DEFAULT NULL COMMENT '电子票编号',
    `order_id` BIGINT DEFAULT NULL COMMENT '订单ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `concert_id` BIGINT DEFAULT NULL COMMENT '演唱会ID',
    `concert_name` VARCHAR(255) DEFAULT NULL COMMENT '演唱会名称',
    `seat_info` VARCHAR(255) DEFAULT NULL COMMENT '座位信息',
    `qr_code` VARCHAR(500) DEFAULT NULL COMMENT '二维码内容',
    `status` INT DEFAULT 0 COMMENT '状态: 0未使用, 1已使用, 2已过期',
    `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ticket_no` (`ticket_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子票表';
