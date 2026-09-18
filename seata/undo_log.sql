-- 每个业务数据库都需要创建 undo_log 表
-- 需要在 melody_concert、melody_ticket、melody_user 三个数据库中分别执行

CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    `ext`           VARCHAR(100) DEFAULT NULL COMMENT 'ext',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';

-- Seata Server 需要的表（在 seata 数据库中创建）
-- CREATE DATABASE IF NOT EXISTS `seata` DEFAULT CHARACTER SET utf8mb4;
