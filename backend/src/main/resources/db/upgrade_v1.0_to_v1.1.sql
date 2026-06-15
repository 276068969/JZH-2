-- ============================================================
-- 监狱管理平台数据库升级脚本 V1.1
-- 功能：新增系统日志表 sys_logs
-- 适用：从 V1.0 版本升级（已有旧库，不使用 DROP TABLE）
-- 执行方式：在已有的 prison_db 数据库中执行
-- ============================================================

USE prison_db;

-- 1. 新增系统日志表（如果不存在）
CREATE TABLE IF NOT EXISTS sys_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_username VARCHAR(50) COMMENT '操作人用户名',
    operator_real_name VARCHAR(50) COMMENT '操作人姓名',
    module VARCHAR(50) NOT NULL COMMENT '业务模块',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    detail TEXT COMMENT '操作详情',
    target_type VARCHAR(50) COMMENT '操作对象类型',
    target_id BIGINT COMMENT '操作对象ID',
    target_name VARCHAR(200) COMMENT '操作对象名称',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作状态: SUCCESS, FAILURE',
    fail_reason TEXT COMMENT '失败原因',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(255) COMMENT '请求URL',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 新增索引（如果不存在）
-- MySQL 5.7+ 支持 CREATE INDEX IF NOT EXISTS，低版本请手动执行
SET @exist_index := (SELECT COUNT(1) FROM information_schema.STATISTICS
                     WHERE TABLE_SCHEMA = 'prison_db'
                       AND TABLE_NAME = 'sys_logs'
                       AND INDEX_NAME = 'idx_operator');
SET @sqlstmt := IF(@exist_index = 0,
    'CREATE INDEX idx_operator ON sys_logs(operator_username)',
    'SELECT "idx_operator already exists" AS msg');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_index := (SELECT COUNT(1) FROM information_schema.STATISTICS
                     WHERE TABLE_SCHEMA = 'prison_db'
                       AND TABLE_NAME = 'sys_logs'
                       AND INDEX_NAME = 'idx_module');
SET @sqlstmt := IF(@exist_index = 0,
    'CREATE INDEX idx_module ON sys_logs(module)',
    'SELECT "idx_module already exists" AS msg');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_index := (SELECT COUNT(1) FROM information_schema.STATISTICS
                     WHERE TABLE_SCHEMA = 'prison_db'
                       AND TABLE_NAME = 'sys_logs'
                       AND INDEX_NAME = 'idx_create_time');
SET @sqlstmt := IF(@exist_index = 0,
    'CREATE INDEX idx_create_time ON sys_logs(create_time)',
    'SELECT "idx_create_time already exists" AS msg');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_index := (SELECT COUNT(1) FROM information_schema.STATISTICS
                     WHERE TABLE_SCHEMA = 'prison_db'
                       AND TABLE_NAME = 'sys_logs'
                       AND INDEX_NAME = 'idx_target');
SET @sqlstmt := IF(@exist_index = 0,
    'CREATE INDEX idx_target ON sys_logs(target_type, target_id)',
    'SELECT "idx_target already exists" AS msg');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 升级完成
-- V1.1 新增功能：系统日志真实业务落库
-- 覆盖模块：登录认证、服刑人员管理、服刑人员调动、
--          事件上报与处置、访客审批
-- ============================================================
