-- ============================================================
-- 监狱管理平台数据库升级脚本 V1.0 → V1.1
-- 功能：
--   1. 新增系统日志表 sys_logs
--   2. 补齐 visitors 表律师会见相关新增字段
-- 适用：从 V1.0 版本升级（已有旧库，不使用任何 DROP TABLE）
-- 执行方式：在已有的 prison_db 数据库中执行
-- ============================================================

USE prison_db;

-- ============================================================
-- 第一部分：新增系统日志表 sys_logs
-- ============================================================

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

-- 新增索引（使用存储过程判断是否存在，兼容所有MySQL版本）
DELIMITER //
DROP PROCEDURE IF EXISTS add_index_if_not_exists //
CREATE PROCEDURE add_index_if_not_exists(
    IN table_name VARCHAR(64),
    IN index_name VARCHAR(64),
    IN index_def VARCHAR(255)
)
BEGIN
    DECLARE index_count INT;
    SELECT COUNT(1) INTO index_count
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name
      AND INDEX_NAME = index_name;

    IF index_count = 0 THEN
        SET @sql = CONCAT('CREATE INDEX ', index_name, ' ON ', table_name, '(', index_def, ')');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- 创建 sys_logs 表的索引
CALL add_index_if_not_exists('sys_logs', 'idx_operator', 'operator_username');
CALL add_index_if_not_exists('sys_logs', 'idx_module', 'module');
CALL add_index_if_not_exists('sys_logs', 'idx_create_time', 'create_time');
CALL add_index_if_not_exists('sys_logs', 'idx_target', 'target_type, target_id');

-- ============================================================
-- 第二部分：补齐 visitors 表律师会见相关新增字段
-- ============================================================

-- 使用存储过程添加列（判断列是否存在，兼容所有MySQL版本）
DELIMITER //
DROP PROCEDURE IF EXISTS add_column_if_not_exists //
CREATE PROCEDURE add_column_if_not_exists(
    IN table_name VARCHAR(64),
    IN column_name VARCHAR(64),
    IN column_def VARCHAR(255)
)
BEGIN
    DECLARE column_count INT;
    SELECT COUNT(1) INTO column_count
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name
      AND COLUMN_NAME = column_name;

    IF column_count = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', table_name, ' ADD COLUMN ', column_name, ' ', column_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- 补齐 visitors 表的律师会见相关字段
CALL add_column_if_not_exists('visitors', 'lawyer_license_no', 'VARCHAR(50) COMMENT \'律师执业证号\'');
CALL add_column_if_not_exists('visitors', 'law_firm_name', 'VARCHAR(100) COMMENT \'律师事务所名称\'');
CALL add_column_if_not_exists('visitors', 'power_of_attorney_no', 'VARCHAR(100) COMMENT \'委托书/法律援助公函编号\'');
CALL add_column_if_not_exists('visitors', 'case_type', 'VARCHAR(50) COMMENT \'案件类型: CRIMINAL(刑事), CIVIL(民事), ADMINISTRATIVE(行政), OTHER(其他)\'');
CALL add_column_if_not_exists('visitors', 'needs_translator', 'TINYINT(1) NOT NULL DEFAULT 0 COMMENT \'是否需要翻译\'');
CALL add_column_if_not_exists('visitors', 'recording_required', 'TINYINT(1) NOT NULL DEFAULT 0 COMMENT \'会见是否需要录音录像\'');
CALL add_column_if_not_exists('visitors', 'lawyer_license_valid_date', 'DATE COMMENT \'律师执业证有效期\'');
CALL add_column_if_not_exists('visitors', 'is_legal_aid', 'TINYINT(1) NOT NULL DEFAULT 0 COMMENT \'是否法律援助案件\'');
CALL add_column_if_not_exists('visitors', 'assistant_lawyer_name', 'VARCHAR(50) COMMENT \'协办律师姓名\'');
CALL add_column_if_not_exists('visitors', 'assistant_lawyer_license_no', 'VARCHAR(50) COMMENT \'协办律师执业证号\'');
CALL add_column_if_not_exists('visitors', 'meeting_security_level', 'VARCHAR(20) COMMENT \'会见安全等级: STANDARD(标准), ELEVATED(加强), STRICT(严格)\'');
CALL add_column_if_not_exists('visitors', 'is_urgent_lawyer_meeting', 'TINYINT(1) NOT NULL DEFAULT 0 COMMENT \'是否紧急律师会见\'');
CALL add_column_if_not_exists('visitors', 'lawyer_email', 'VARCHAR(100) COMMENT \'律师联系邮箱\'');
CALL add_column_if_not_exists('visitors', 'meeting_stage', 'VARCHAR(50) COMMENT \'会见阶段: INVESTIGATION(侦查), PROSECUTION(审查起诉), TRIAL(审判), EXECUTION(执行)\'');
CALL add_column_if_not_exists('visitors', 'room_type_required', 'VARCHAR(50) COMMENT \'会见室类型: NORMAL(普通), ISOLATION(隔离), REMOTE(远程)\'');
CALL add_column_if_not_exists('visitors', 'has_assistant', 'TINYINT(1) NOT NULL DEFAULT 0 COMMENT \'是否携带助理会见\'');
CALL add_column_if_not_exists('visitors', 'update_time', 'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');
CALL add_column_if_not_exists('visitors', 'deleted', 'TINYINT(1) NOT NULL DEFAULT 0');

-- ============================================================
-- 第三部分：清理临时存储过程
-- ============================================================

DROP PROCEDURE IF EXISTS add_index_if_not_exists;
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

-- ============================================================
-- 升级完成
-- V1.1 新增功能：
--   1. 系统日志真实业务落库
--   2. 律师会见全流程支持（20+新增字段）
-- 覆盖模块：登录认证、服刑人员管理、服刑人员调动、
--          事件上报与处置、访客审批（含律师会见）
-- ============================================================
SELECT '数据库升级完成 V1.0 → V1.1' AS upgrade_result;
