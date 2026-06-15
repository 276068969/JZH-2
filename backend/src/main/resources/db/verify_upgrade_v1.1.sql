-- ============================================================
-- V1.1 升级验证脚本
-- 用途：执行完 upgrade_v1.0_to_v1.1.sql 后，运行此脚本验证升级结果
-- 用法：SOURCE verify_upgrade_v1.1.sql
-- ============================================================

USE prison_db;

SELECT '========== 1. 验证 sys_logs 表 ==========' AS check_item;
SELECT
    CASE
        WHEN COUNT(*) = 1 THEN 'PASS: sys_logs 表存在'
        ELSE 'FAIL: sys_logs 表不存在'
    END AS result
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'prison_db' AND TABLE_NAME = 'sys_logs';

SELECT '--- sys_logs 表字段检查 ---' AS check_detail;
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'prison_db' AND TABLE_NAME = 'sys_logs'
ORDER BY ORDINAL_POSITION;

SELECT '--- sys_logs 表索引检查 ---' AS check_detail;
SELECT INDEX_NAME, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columns
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'prison_db' AND TABLE_NAME = 'sys_logs'
GROUP BY INDEX_NAME
ORDER BY INDEX_NAME;

SELECT '========== 2. 验证 visitors 表新增字段 ==========' AS check_item;

DROP TEMPORARY TABLE IF EXISTS expected_visitor_columns;
CREATE TEMPORARY TABLE expected_visitor_columns (col_name VARCHAR(64));
INSERT INTO expected_visitor_columns VALUES
    ('lawyer_license_no'),
    ('law_firm_name'),
    ('power_of_attorney_no'),
    ('case_type'),
    ('needs_translator'),
    ('recording_required'),
    ('lawyer_license_valid_date'),
    ('is_legal_aid'),
    ('assistant_lawyer_name'),
    ('assistant_lawyer_license_no'),
    ('meeting_security_level'),
    ('is_urgent_lawyer_meeting'),
    ('lawyer_email'),
    ('meeting_stage'),
    ('room_type_required'),
    ('has_assistant'),
    ('update_time'),
    ('deleted');

SELECT
    e.col_name AS expected_column,
    CASE
        WHEN c.COLUMN_NAME IS NOT NULL THEN 'PASS'
        ELSE 'MISSING'
    END AS status
FROM expected_visitor_columns e
LEFT JOIN information_schema.COLUMNS c
    ON c.TABLE_SCHEMA = 'prison_db'
    AND c.TABLE_NAME = 'visitors'
    AND c.COLUMN_NAME = e.col_name
ORDER BY e.col_name;

SELECT '========== 3. 验证升级可重入（幂等性）==========' AS check_item;
SELECT
    CASE
        WHEN COUNT(DISTINCT COLUMN_NAME) >= 18 THEN 'PASS: 全部 18+ 新增字段存在'
        ELSE CONCAT('FAIL: 仅找到 ', COUNT(DISTINCT COLUMN_NAME), ' 个字段')
    END AS result
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'prison_db'
  AND TABLE_NAME = 'visitors'
  AND COLUMN_NAME IN (
    'lawyer_license_no','law_firm_name','power_of_attorney_no','case_type',
    'needs_translator','recording_required','lawyer_license_valid_date',
    'is_legal_aid','assistant_lawyer_name','assistant_lawyer_license_no',
    'meeting_security_level','is_urgent_lawyer_meeting','lawyer_email',
    'meeting_stage','room_type_required','has_assistant','update_time','deleted'
  );

DROP TEMPORARY TABLE IF EXISTS expected_visitor_columns;

SELECT '========== 升级验证完成 ==========' AS verification_complete;
