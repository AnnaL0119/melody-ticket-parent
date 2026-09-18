-- 创建 Nacos 配置库
CREATE DATABASE IF NOT EXISTS nacos_config DEFAULT CHARACTER SET utf8mb4;

-- 创建 Seata 库
CREATE DATABASE IF NOT EXISTS seata DEFAULT CHARACTER SET utf8mb4;

-- 创建 SkyWalking 库
CREATE DATABASE IF NOT EXISTS sw_skywalking DEFAULT CHARACTER SET utf8mb4;

-- 创建业务库
CREATE DATABASE IF NOT EXISTS melody_user DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS melody_concert DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS melody_ticket DEFAULT CHARACTER SET utf8mb4;

-- undo_log 表（每个业务库都需要）
CREATE TABLE IF NOT EXISTS melody_concert.undo_log (
    branch_id BIGINT NOT NULL,
    xid VARCHAR(128) NOT NULL,
    context VARCHAR(128) NOT NULL,
    rollback_info LONGBLOB NOT NULL,
    log_status INT NOT NULL,
    log_created DATETIME(6) NOT NULL,
    log_modified DATETIME(6) NOT NULL,
    ext VARCHAR(100) DEFAULT NULL,
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS melody_ticket.undo_log LIKE melody_concert.undo_log;
CREATE TABLE IF NOT EXISTS melody_user.undo_log LIKE melody_concert.undo_log;

-- Seata Server 表
CREATE TABLE IF NOT EXISTS seata.global_table (
    xid VARCHAR(128) NOT NULL,
    transaction_id BIGINT DEFAULT NULL,
    status TINYINT NOT NULL,
    application_id VARCHAR(32) DEFAULT NULL,
    transaction_service_group VARCHAR(32) DEFAULT NULL,
    transaction_name VARCHAR(128) DEFAULT NULL,
    timeout INT DEFAULT NULL,
    begin_time BIGINT DEFAULT NULL,
    application_data VARCHAR(2000) DEFAULT NULL,
    gmt_create DATETIME DEFAULT NULL,
    gmt_modified DATETIME DEFAULT NULL,
    PRIMARY KEY (xid),
    KEY idx_status_gmt_modified (status, gmt_modified),
    KEY idx_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seata.branch_table (
    branch_id BIGINT NOT NULL,
    xid VARCHAR(128) NOT NULL,
    transaction_id BIGINT DEFAULT NULL,
    resource_group_id VARCHAR(32) DEFAULT NULL,
    resource_id VARCHAR(256) DEFAULT NULL,
    branch_type VARCHAR(8) DEFAULT NULL,
    status TINYINT DEFAULT NULL,
    client_id VARCHAR(64) DEFAULT NULL,
    application_data VARCHAR(2000) DEFAULT NULL,
    gmt_create DATETIME(6) DEFAULT NULL,
    gmt_modified DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (branch_id),
    KEY idx_xid (xid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seata.lock_table (
    row_key VARCHAR(128) NOT NULL,
    xid VARCHAR(128) DEFAULT NULL,
    transaction_id BIGINT DEFAULT NULL,
    branch_id BIGINT NOT NULL,
    resource_id VARCHAR(256) DEFAULT NULL,
    table_name VARCHAR(32) DEFAULT NULL,
    pk VARCHAR(36) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT '0',
    gmt_create DATETIME DEFAULT NULL,
    gmt_modified DATETIME DEFAULT NULL,
    PRIMARY KEY (row_key),
    KEY idx_branch_id (branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
