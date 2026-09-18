-- =============================================
-- MelodyTicket 数据库创建脚本
-- 使用方法: 在 MySQL 中逐行执行
-- =============================================

CREATE DATABASE IF NOT EXISTS melody_user
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS melody_concert
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS melody_ticket
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 确认创建成功
SELECT 'melody_user' AS database_name, SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = 'melody_user'
UNION ALL
SELECT 'melody_concert', SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = 'melody_concert'
UNION ALL
SELECT 'melody_ticket', SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = 'melody_ticket';
