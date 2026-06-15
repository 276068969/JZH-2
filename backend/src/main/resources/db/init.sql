CREATE DATABASE IF NOT EXISTS prison_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE prison_db;

DROP TABLE IF EXISTS medical_records;
DROP TABLE IF EXISTS visitors;
DROP TABLE IF EXISTS incidents;
DROP TABLE IF EXISTS patrol_handovers;
DROP TABLE IF EXISTS patrols;
DROP TABLE IF EXISTS prisoner_transfers;
DROP TABLE IF EXISTS prisoners;
DROP TABLE IF EXISTS guards;
DROP TABLE IF EXISTS cells;
DROP TABLE IF EXISTS prison_areas;
DROP TABLE IF EXISTS sys_logs;
DROP TABLE IF EXISTS users;

CREATE TABLE sys_logs (
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
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    INDEX idx_operator (operator_username),
    INDEX idx_module (module),
    INDEX idx_create_time (create_time),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    role VARCHAR(50) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE prison_areas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    area_name VARCHAR(100) NOT NULL,
    area_code VARCHAR(50) NOT NULL UNIQUE,
    area_type VARCHAR(50) NOT NULL COMMENT '监区类型: MALE(男监), FEMALE(女监), JUVENILE(少管所), HIGH_SECURITY(高度戒备)',
    capacity INT NOT NULL DEFAULT 0,
    current_population INT NOT NULL DEFAULT 0,
    address VARCHAR(255),
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cells (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cell_number VARCHAR(50) NOT NULL,
    area_id BIGINT NOT NULL,
    cell_type VARCHAR(50) NOT NULL COMMENT 'SINGLE(单人), DOUBLE(双人), MULTI(多人), ISOLATION(禁闭)',
    capacity INT NOT NULL DEFAULT 1,
    current_occupancy INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE, FULL, MAINTENANCE',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (area_id) REFERENCES prison_areas(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE prisoners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prisoner_number VARCHAR(50) NOT NULL UNIQUE COMMENT '服刑人员编号',
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    id_card VARCHAR(18) NOT NULL UNIQUE,
    birth_date DATE,
    native_place VARCHAR(100),
    crime_type VARCHAR(100) COMMENT '罪名类型',
    sentence_term INT COMMENT '刑期(月)',
    entry_date DATE COMMENT '入狱日期',
    release_date DATE COMMENT '预计释放日期',
    area_id BIGINT,
    cell_id BIGINT,
    education_level VARCHAR(50),
    marital_status VARCHAR(20),
    occupation VARCHAR(100),
    health_status VARCHAR(50),
    danger_level VARCHAR(20) COMMENT 'LOW, MEDIUM, HIGH, EXTREME',
    status VARCHAR(20) NOT NULL DEFAULT 'INCARCERATED' COMMENT 'INCARCERATED, RELEASED, TRANSFERRED, MEDICAL_PAROLE',
    photo_url VARCHAR(255),
    remark TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (area_id) REFERENCES prison_areas(id),
    FOREIGN KEY (cell_id) REFERENCES cells(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE prisoner_transfers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prisoner_id BIGINT NOT NULL COMMENT '服刑人员ID',
    prisoner_number VARCHAR(50) NOT NULL COMMENT '服刑人员编号',
    prisoner_name VARCHAR(50) NOT NULL COMMENT '服刑人员姓名',
    from_area_id BIGINT COMMENT '原监区ID',
    from_area_name VARCHAR(100) COMMENT '原监区名称',
    from_cell_id BIGINT COMMENT '原监舍ID',
    from_cell_number VARCHAR(50) COMMENT '原监舍编号',
    to_area_id BIGINT COMMENT '新监区ID',
    to_area_name VARCHAR(100) COMMENT '新监区名称',
    to_cell_id BIGINT COMMENT '新监舍ID',
    to_cell_number VARCHAR(50) COMMENT '新监舍编号',
    transfer_type VARCHAR(20) NOT NULL COMMENT '调动类型: AREA_TRANSFER(调监), CELL_TRANSFER(调舍), BOTH(监区监舍都调)',
    transfer_time DATETIME NOT NULL COMMENT '调动时间',
    transfer_reason VARCHAR(255) COMMENT '调动原因',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    remark TEXT COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (prisoner_id) REFERENCES prisoners(id),
    FOREIGN KEY (from_area_id) REFERENCES prison_areas(id),
    FOREIGN KEY (from_cell_id) REFERENCES cells(id),
    FOREIGN KEY (to_area_id) REFERENCES prison_areas(id),
    FOREIGN KEY (to_cell_id) REFERENCES cells(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE guards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guard_number VARCHAR(50) NOT NULL UNIQUE COMMENT '警号',
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    id_card VARCHAR(18) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    position VARCHAR(50) COMMENT 'GUARD(普通狱警), CAPTAIN(队长), CHIEF(监区长), DIRECTOR(监狱长)',
    area_id BIGINT,
    entry_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, LEAVE, RESIGNED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (area_id) REFERENCES prison_areas(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE patrols (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patrol_time DATETIME NOT NULL,
    guard_id BIGINT NOT NULL,
    area_id BIGINT NOT NULL,
    patrol_type VARCHAR(50) NOT NULL COMMENT 'ROUTINE(常规), KEY_AREA(重点区域), NIGHT(夜间), EMERGENCY(紧急)',
    result VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL, ABNORMAL, INCIDENT',
    description TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (guard_id) REFERENCES guards(id),
    FOREIGN KEY (area_id) REFERENCES prison_areas(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE patrol_handovers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    area_id BIGINT NOT NULL COMMENT '监区ID',
    area_name VARCHAR(100) NOT NULL COMMENT '监区名称',
    shift_type VARCHAR(20) NOT NULL COMMENT '班次类型: MORNING(早班), AFTERNOON(中班), NIGHT(晚班)',
    shift_start_time DATETIME NOT NULL COMMENT '班次开始时间',
    shift_end_time DATETIME NOT NULL COMMENT '班次结束时间',
    outgoing_guard_id BIGINT NOT NULL COMMENT '交班警员ID',
    outgoing_guard_name VARCHAR(50) NOT NULL COMMENT '交班警员姓名',
    incoming_guard_id BIGINT COMMENT '接班警员ID',
    incoming_guard_name VARCHAR(50) COMMENT '接班警员姓名',
    key_area_status TEXT COMMENT '重点区域情况',
    unfinished_items TEXT COMMENT '未完成事项',
    risk_points TEXT COMMENT '待跟进风险点',
    patrol_count INT NOT NULL DEFAULT 0 COMMENT '本班巡查次数',
    abnormal_count INT NOT NULL DEFAULT 0 COMMENT '本班异常次数',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING(待接班), CONFIRMED(已确认)',
    handover_time DATETIME COMMENT '交接班时间',
    remark TEXT COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (area_id) REFERENCES prison_areas(id),
    FOREIGN KEY (outgoing_guard_id) REFERENCES guards(id),
    FOREIGN KEY (incoming_guard_id) REFERENCES guards(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE incidents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_title VARCHAR(200) NOT NULL,
    incident_type VARCHAR(50) NOT NULL COMMENT 'FIGHT(斗殴), ESCAPE_ATTEMPT(越狱企图), MEDICAL(医疗), DISCIPLINE(违纪), OTHER(其他)',
    severity VARCHAR(20) NOT NULL COMMENT 'LOW, MEDIUM, HIGH, CRITICAL',
    area_id BIGINT,
    report_guard_id BIGINT,
    related_prisoner_id BIGINT,
    occur_time DATETIME NOT NULL,
    description TEXT,
    handler_result TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, PROCESSING, RESOLVED, CLOSED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (area_id) REFERENCES prison_areas(id),
    FOREIGN KEY (report_guard_id) REFERENCES guards(id),
    FOREIGN KEY (related_prisoner_id) REFERENCES prisoners(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE visitors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    visitor_name VARCHAR(50) NOT NULL,
    id_card VARCHAR(18) NOT NULL,
    phone VARCHAR(20),
    relation VARCHAR(50) NOT NULL COMMENT 'PARENT, SPOUSE, SIBLING, CHILD, FRIEND, LAWYER, OTHER',
    prisoner_id BIGINT NOT NULL,
    visit_date DATE NOT NULL,
    visit_time_slot VARCHAR(20) NOT NULL COMMENT 'AM(上午), PM(下午)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING(待审核), APPROVED(已通过), REJECTED(已驳回), IN_PROGRESS(会见中), COMPLETED(已完成), CANCELLED(已取消)',
    id_card_photo VARCHAR(255),
    visitor_count INT NOT NULL DEFAULT 1,
    purpose VARCHAR(255),
    approve_guard_id BIGINT,
    approve_remark TEXT COMMENT '审批意见',
    approve_time DATETIME COMMENT '审批时间',
    visit_type VARCHAR(20) COMMENT 'FAMILY(家属会见), LAWYER(律师会见), OTHER(其他)',
    actual_start_time DATETIME COMMENT '实际开始时间',
    actual_end_time DATETIME COMMENT '实际结束时间',
    remark TEXT,
    lawyer_license_no VARCHAR(50) COMMENT '律师执业证号',
    law_firm_name VARCHAR(100) COMMENT '律师事务所名称',
    power_of_attorney_no VARCHAR(100) COMMENT '委托书/法律援助公函编号',
    case_type VARCHAR(50) COMMENT '案件类型: CRIMINAL(刑事), CIVIL(民事), ADMINISTRATIVE(行政), OTHER(其他)',
    needs_translator TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否需要翻译',
    recording_required TINYINT(1) NOT NULL DEFAULT 0 COMMENT '会见是否需要录音录像',
    lawyer_license_valid_date DATE COMMENT '律师执业证有效期',
    is_legal_aid TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否法律援助案件',
    assistant_lawyer_name VARCHAR(50) COMMENT '协办律师姓名',
    assistant_lawyer_license_no VARCHAR(50) COMMENT '协办律师执业证号',
    meeting_security_level VARCHAR(20) COMMENT '会见安全等级: STANDARD(标准), ELEVATED(加强), STRICT(严格)',
    is_urgent_lawyer_meeting TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否紧急律师会见',
    lawyer_email VARCHAR(100) COMMENT '律师联系邮箱',
    meeting_stage VARCHAR(50) COMMENT '会见阶段: INVESTIGATION(侦查), PROSECUTION(审查起诉), TRIAL(审判), EXECUTION(执行)',
    room_type_required VARCHAR(50) COMMENT '会见室类型: NORMAL(普通), ISOLATION(隔离), REMOTE(远程)',
    has_assistant TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否携带助理会见',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (prisoner_id) REFERENCES prisoners(id),
    FOREIGN KEY (approve_guard_id) REFERENCES guards(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE medical_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prisoner_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    diagnosis VARCHAR(255),
    treatment TEXT,
    hospital VARCHAR(100),
    doctor_name VARCHAR(50),
    medical_type VARCHAR(50) COMMENT 'PHYSICAL(体检), OUTPATIENT(门诊), EMERGENCY(急诊), HOSPITALIZATION(住院), PSYCHOLOGICAL(心理)',
    result VARCHAR(50) COMMENT 'RECOVERED, TREATING, TRANSFERRED, DECEASED',
    medicine TEXT,
    follow_up_date DATE,
    follow_up_status VARCHAR(20) COMMENT 'PENDING(待复诊), COMPLETED(已复诊), MISSED(未复诊), CANCELLED(已取消)',
    actual_follow_up_date DATE,
    follow_up_result TEXT,
    follow_up_remark TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (prisoner_id) REFERENCES prisoners(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO users (username, password, real_name, phone, email, role) VALUES
('admin', 'TO_BE_ENCRYPTED', '超级管理员', '13800000001', 'admin@prison.com', 'ROLE_ADMIN'),
('manager', 'TO_BE_ENCRYPTED', '监狱管理员', '13800000002', 'manager@prison.com', 'ROLE_MANAGER'),
('guard', 'TO_BE_ENCRYPTED', '狱警张三', '13800000003', 'guard@prison.com', 'ROLE_GUARD'),
('doctor', 'TO_BE_ENCRYPTED', '医务人员李四', '13800000004', 'doctor@prison.com', 'ROLE_DOCTOR'),
('viewer', 'TO_BE_ENCRYPTED', '查看人员王五', '13800000005', 'viewer@prison.com', 'ROLE_VIEWER');

INSERT INTO prison_areas (area_name, area_code, area_type, capacity, current_population, address, description) VALUES
('男监一区', 'M001', 'MALE', 500, 320, '监狱大院A栋', '关押普通男性服刑人员'),
('男监二区', 'M002', 'MALE', 400, 280, '监狱大院B栋', '关押普通男性服刑人员'),
('女监区', 'F001', 'FEMALE', 200, 95, '监狱大院C栋', '关押女性服刑人员'),
('高度戒备区', 'H001', 'HIGH_SECURITY', 100, 45, '监狱大院D栋', '关押高危险级别服刑人员'),
('少管所', 'J001', 'JUVENILE', 150, 60, '监狱大院E栋', '关押未成年服刑人员');

INSERT INTO cells (cell_number, area_id, cell_type, capacity, current_occupancy, status) VALUES
('M001-101', 1, 'MULTI', 8, 6, 'AVAILABLE'),
('M001-102', 1, 'MULTI', 8, 8, 'FULL'),
('M001-103', 1, 'DOUBLE', 2, 2, 'FULL'),
('M002-201', 2, 'MULTI', 8, 5, 'AVAILABLE'),
('M002-202', 2, 'MULTI', 8, 7, 'AVAILABLE'),
('F001-101', 3, 'MULTI', 6, 4, 'AVAILABLE'),
('F001-102', 3, 'DOUBLE', 2, 1, 'AVAILABLE'),
('H001-ISO1', 4, 'ISOLATION', 1, 0, 'AVAILABLE'),
('H001-ISO2', 4, 'ISOLATION', 1, 1, 'FULL'),
('J001-101', 5, 'MULTI', 6, 3, 'AVAILABLE');

INSERT INTO prisoners (prisoner_number, name, gender, id_card, birth_date, native_place, crime_type, sentence_term, entry_date, release_date, area_id, cell_id, education_level, marital_status, danger_level, status) VALUES
('P20240001', '赵某', '男', '440101199001011234', '1990-01-01', '广东省广州市', '盗窃罪', 36, '2024-01-15', '2027-01-14', 1, 1, '初中', '未婚', 'LOW', 'INCARCERATED'),
('P20240002', '钱某', '男', '440102198805056789', '1988-05-05', '广东省深圳市', '抢劫罪', 60, '2024-02-20', '2029-02-19', 1, 2, '高中', '已婚', 'MEDIUM', 'INCARCERATED'),
('P20240003', '孙某', '男', '440103199203084321', '1992-03-08', '广东省东莞市', '故意伤害罪', 48, '2024-03-10', '2028-03-09', 2, 4, '大专', '未婚', 'MEDIUM', 'INCARCERATED'),
('P20240004', '李某', '女', '440104198711225678', '1987-11-22', '广东省珠海市', '诈骗罪', 24, '2024-04-01', '2026-03-31', 3, 6, '本科', '离异', 'LOW', 'INCARCERATED'),
('P20240005', '周某', '男', '440105199507071234', '1995-07-07', '广东省佛山市', '贩毒罪', 120, '2024-05-15', '2034-05-14', 4, 9, '高中', '未婚', 'HIGH', 'INCARCERATED'),
('P20240006', '吴某', '男', '440106199301013456', '1993-01-01', '广东省中山市', '盗窃罪', 18, '2025-01-10', '2026-06-25', 1, 1, '初中', '未婚', 'LOW', 'INCARCERATED'),
('P20240007', '郑某', '男', '440107198906067890', '1989-06-06', '广东省惠州市', '诈骗罪', 30, '2024-08-20', '2026-08-10', 2, 4, '大专', '已婚', 'MEDIUM', 'INCARCERATED'),
('P20240008', '王某', '男', '440108199502024567', '1995-02-02', '广东省江门市', '故意伤害罪', 24, '2025-03-01', '2026-07-20', 1, 2, '高中', '未婚', 'HIGH', 'INCARCERATED'),
('P20240009', '冯某', '女', '440109198810108765', '1988-10-10', '广东省肇庆市', '贪污罪', 36, '2024-06-01', '2026-09-01', 3, 6, '本科', '已婚', 'LOW', 'MEDICAL_PAROLE'),
('P20240010', '陈某', '男', '440110199204042345', '1992-04-04', '广东省汕头市', '抢劫罪', 48, '2024-04-15', '2027-04-14', 4, 9, '高中', '未婚', 'EXTREME', 'INCARCERATED'),
('P20240011', '杨某', '男', '440111199708085678', '1997-08-08', '广东省湛江市', '盗窃罪', 12, '2025-09-01', '2026-06-15', 1, 1, '初中', '未婚', 'LOW', 'TRANSFERRED');

INSERT INTO prisoner_transfers (prisoner_id, prisoner_number, prisoner_name, from_area_id, from_area_name, from_cell_id, from_cell_number, to_area_id, to_area_name, to_cell_id, to_cell_number, transfer_type, transfer_time, transfer_reason, operator_id, operator_name, remark) VALUES
(1, 'P20240001', '赵某', 2, '男监二区', 4, 'M002-201', 1, '男监一区', 1, 'M001-101', 'BOTH', '2024-06-01 10:00:00', '表现良好，调回普通监区', 1, '刘警员', '调监调舍同时进行'),
(2, 'P20240002', '钱某', 1, '男监一区', 1, 'M001-101', 1, '男监一区', 2, 'M001-102', 'CELL_TRANSFER', '2024-05-15 14:30:00', '原监舍满员调整', 2, '陈警员', '监区内调动'),
(11, 'P20240011', '杨某', 2, '男监二区', 4, 'M002-201', 1, '男监一区', 1, 'M001-101', 'BOTH', '2025-10-10 09:00:00', '刑期较短，转至低戒备区', 2, '陈警员', '表现良好，降低戒备等级'),
(8, 'P20240008', '王某', 2, '男监二区', 5, 'M002-202', 1, '男监一区', 2, 'M001-102', 'BOTH', '2025-04-20 11:00:00', '危险等级调整', 1, '刘警员', '因违纪行为上调戒备等级'),
(7, 'P20240007', '郑某', 1, '男监一区', 3, 'M001-103', 2, '男监二区', 4, 'M002-201', 'BOTH', '2024-09-15 16:00:00', '监区人员均衡调整', 2, '陈警员', '常规人员调整');

INSERT INTO guards (guard_number, name, gender, id_card, phone, email, position, area_id, entry_date, status) VALUES
('G2024001', '刘警员', '男', '440106198001011111', '13900000001', 'liu@prison.com', 'GUARD', 1, '2020-01-15', 'ACTIVE'),
('G2024002', '陈警员', '男', '440107198502022222', '13900000002', 'chen@prison.com', 'CAPTAIN', 2, '2018-06-20', 'ACTIVE'),
('G2024003', '黄警员', '女', '440108199003033333', '13900000003', 'huang@prison.com', 'GUARD', 3, '2021-03-10', 'ACTIVE'),
('G2024004', '吴警员', '男', '440109198804044444', '13900000004', 'wu@prison.com', 'CHIEF', 4, '2016-09-01', 'ACTIVE'),
('G2024005', '林警员', '女', '440110199506055555', '13900000005', 'lin@prison.com', 'GUARD', 5, '2022-07-15', 'ACTIVE');

INSERT INTO patrols (patrol_time, guard_id, area_id, patrol_type, result, description) VALUES
('2024-06-01 08:00:00', 1, 1, 'ROUTINE', 'NORMAL', '常规巡查一切正常'),
('2024-06-01 14:00:00', 2, 2, 'ROUTINE', 'NORMAL', '下午常规巡查完成'),
('2024-06-01 20:00:00', 3, 3, 'NIGHT', 'NORMAL', '夜间巡查无异常'),
('2024-06-02 08:00:00', 4, 4, 'KEY_AREA', 'NORMAL', '重点区域巡查正常'),
('2024-06-02 10:00:00', 5, 5, 'ROUTINE', 'NORMAL', '少管所巡查正常');

INSERT INTO patrol_handovers (area_id, area_name, shift_type, shift_start_time, shift_end_time, outgoing_guard_id, outgoing_guard_name, incoming_guard_id, incoming_guard_name, key_area_status, unfinished_items, risk_points, patrol_count, abnormal_count, status, handover_time, remark) VALUES
(1, '男监一区', 'MORNING', '2024-06-01 08:00:00', '2024-06-01 16:00:00', 1, '刘警员', 2, '陈警员', '各监舍秩序良好，重点监控室设备运行正常', '继续观察1号监舍服刑人员赵某情绪状态', '赵某近期情绪波动较大，需密切关注', 4, 0, 'CONFIRMED', '2024-06-01 16:00:00', '早班交接顺利'),
(1, '男监一区', 'AFTERNOON', '2024-06-01 16:00:00', '2024-06-02 00:00:00', 2, '陈警员', 1, '刘警员', '监区秩序平稳，夜间巡查已安排', '待跟进赵某心理疏导安排', '无新增风险点', 3, 0, 'CONFIRMED', '2024-06-01 23:55:00', '中班交接正常'),
(2, '男监二区', 'MORNING', '2024-06-01 08:00:00', '2024-06-01 16:00:00', 2, '陈警员', 1, '刘警员', '劳动车间运行正常，人员清点无误', '202监舍维修进度需跟进', '孙某近期有违纪倾向', 5, 1, 'CONFIRMED', '2024-06-01 16:05:00', '早班有一起小摩擦已处理'),
(3, '女监区', 'NIGHT', '2024-06-01 20:00:00', '2024-06-02 08:00:00', 3, '黄警员', 5, '林警员', '夜间巡查3次，均无异常', '李某心理咨询待跟进', '李某情绪低落，需持续关注', 3, 0, 'CONFIRMED', '2024-06-02 08:00:00', '夜班平稳'),
(4, '高度戒备区', 'MORNING', '2024-06-02 08:00:00', '2024-06-02 16:00:00', 4, '吴警员', NULL, NULL, '高度戒备区一切正常，单人监舍管控到位', '可疑物品调查中，需继续跟进', '5号监舍周某为高危人员，需重点监控', 6, 1, 'PENDING', NULL, '待接班确认');

INSERT INTO incidents (incident_title, incident_type, severity, area_id, report_guard_id, related_prisoner_id, occur_time, description, status) VALUES
('服刑人员口角纠纷', 'DISCIPLINE', 'LOW', 1, 1, 1, '2024-05-20 10:30:00', '两名服刑人员因生活琐事发生口角，已及时制止', 'RESOLVED'),
('服刑人员突发腹痛', 'MEDICAL', 'MEDIUM', 2, 2, 3, '2024-05-25 15:00:00', '服刑人员孙某突发腹痛，送医务室处理后好转', 'RESOLVED'),
('发现可疑物品', 'OTHER', 'MEDIUM', 4, 4, 5, '2024-06-01 09:00:00', '巡查中发现不明金属物品，正在调查来源', 'PROCESSING');

INSERT INTO visitors (visitor_name, id_card, phone, relation, prisoner_id, visit_date, visit_time_slot, status, visitor_count, purpose, approve_guard_id, approve_remark, approve_time, visit_type, actual_start_time, actual_end_time, remark, lawyer_license_no, law_firm_name, power_of_attorney_no, case_type, needs_translator, recording_required, lawyer_license_valid_date, is_legal_aid, assistant_lawyer_name, assistant_lawyer_license_no, meeting_security_level, is_urgent_lawyer_meeting, lawyer_email, meeting_stage, room_type_required, has_assistant) VALUES
('赵某母亲', '440101196501010000', '13800000011', 'PARENT', 1, '2024-06-05', 'AM', 'COMPLETED', 1, '探视', 1, '身份核实无误，同意会见', '2024-06-04 10:30:00', 'FAMILY', '2024-06-05 09:00:00', '2024-06-05 09:30:00', '首次探视，情绪稳定', NULL, NULL, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0),
('钱某妻子', '440102199001010000', '13800000012', 'SPOUSE', 2, '2024-06-06', 'PM', 'APPROVED', 2, '探视并送物品', 2, '结婚证已核验，同意会见', '2024-06-05 14:20:00', 'FAMILY', NULL, NULL, '携带换洗衣物需检查', NULL, NULL, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0),
('孙某律师', '440103198006010000', '13800000013', 'LAWYER', 3, '2024-06-07', 'AM', 'APPROVED', 1, '法律咨询', 2, '律师执业证有效，同意会见', '2024-06-06 09:15:00', 'LAWYER', NULL, NULL, '需携带相关法律文书', '粤律证字第12345号', '广东正义律师事务所', '2024粤律字第001号', 'CRIMINAL', 0, 1, '2026-12-31', 0, '李助理', '粤律证字第54321号', 'STANDARD', 0, 'sunlv@justicelaw.com', 'TRIAL', 'NORMAL', 1),
('李某父亲', '440104196003030000', '13800000014', 'PARENT', 4, '2024-06-08', 'AM', 'PENDING', 1, '探视', NULL, NULL, NULL, 'FAMILY', NULL, NULL, '待审核', NULL, NULL, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0),
('周某律师', '440105197508080000', '13800000015', 'LAWYER', 5, '2024-06-08', 'PM', 'PENDING', 2, '案件辩护', NULL, NULL, NULL, 'LAWYER', NULL, NULL, '两名律师共同会见', '粤律证字第67890号', '广东明德律师事务所', '2024粤律字第002号', 'CRIMINAL', 0, 1, '2027-06-30', 1, '王律师', '粤律证字第09876号', 'ELEVATED', 1, 'zhoulv@mingdelaw.com', 'TRIAL', 'ISOLATION', 0),
('吴某哥哥', '440106198505050000', '13800000016', 'SIBLING', 1, '2024-06-09', 'AM', 'PENDING', 1, '探视', NULL, NULL, NULL, 'FAMILY', NULL, NULL, '待审核', NULL, NULL, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0),
('郑某女儿', '440107201002020000', '13800000017', 'CHILD', 2, '2024-06-09', 'PM', 'REJECTED', 1, '探视', 1, '未成年需监护人陪同，暂不同意会见', '2024-06-08 16:00:00', 'FAMILY', NULL, NULL, '需提供监护人同意书', NULL, NULL, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0),
('王某朋友', '440108199010100000', '13800000018', 'FRIEND', 3, '2024-06-10', 'AM', 'PENDING', 1, '探视', NULL, NULL, NULL, 'OTHER', NULL, NULL, '待审核', NULL, NULL, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0),
('冯某律师', '440109198212120000', '13800000019', 'LAWYER', 4, '2024-06-10', 'PM', 'PENDING', 1, '上诉准备', NULL, NULL, NULL, 'LAWYER', NULL, NULL, '待审核', '粤律证字第23456号', '广东诚信律师事务所', '2024粤律字第003号', 'CRIMINAL', 0, 1, '2026-09-30', 0, NULL, NULL, 'STANDARD', 0, 'fenglv@chengxinlaw.com', 'TRIAL', 'NORMAL', 0),
('陈某母亲', '440110196807070000', '13800000020', 'PARENT', 5, '2024-06-11', 'AM', 'COMPLETED', 2, '探视', 3, '身份核实无误，同意会见', '2024-06-10 11:00:00', 'FAMILY', '2024-06-11 09:30:00', '2024-06-11 10:00:00', '会见过程顺利', NULL, NULL, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, 0);

INSERT INTO medical_records (prisoner_id, record_date, diagnosis, treatment, hospital, doctor_name, medical_type, result, medicine, follow_up_date, follow_up_status, actual_follow_up_date, follow_up_result, follow_up_remark) VALUES
(1, '2024-05-10', '上呼吸道感染', '给予抗感染治疗，嘱休息多饮水', '监狱医务室', '张医生', 'OUTPATIENT', 'RECOVERED', '阿莫西林0.5g tid', NULL, NULL, NULL, NULL, NULL),
(2, '2024-05-15', '高血压病', '给予降压治疗，定期监测血压', '监狱医务室', '张医生', 'OUTPATIENT', 'TREATING', '硝苯地平控释片30mg qd', '2026-06-15', 'PENDING', NULL, NULL, '每月复诊监测血压'),
(3, '2024-05-25', '急性胃肠炎', '补液、止泻、抗感染治疗', '监狱医务室', '李医生', 'EMERGENCY', 'RECOVERED', '蒙脱石散3g tid，左氧氟沙星0.2g bid', NULL, NULL, NULL, NULL, NULL),
(4, '2026-05-20', '糖尿病', '饮食控制+口服降糖药', '市第一人民医院', '王医生', 'OUTPATIENT', 'TREATING', '二甲双胍0.5g bid', '2026-06-20', 'PENDING', NULL, NULL, '需定期监测血糖'),
(5, '2026-04-10', '冠心病', '扩冠、抗血小板聚集治疗', '市中心医院', '赵医生', 'OUTPATIENT', 'TREATING', '阿司匹林100mg qd，单硝酸异山梨酯20mg bid', '2026-05-10', 'MISSED', NULL, NULL, '逾期未复诊，需重点关注'),
(5, '2026-03-05', '高血压合并冠心病', '调整降压方案，加强心功能监测', '市中心医院', '赵医生', 'HOSPITALIZATION', 'TREATING', '缬沙坦80mg qd', '2026-04-05', 'MISSED', NULL, NULL, '连续两次未复诊'),
(6, '2026-05-28', '过敏性鼻炎', '抗过敏治疗', '监狱医务室', '张医生', 'OUTPATIENT', 'TREATING', '氯雷他定10mg qn', '2026-06-28', 'PENDING', NULL, NULL, ''),
(7, '2026-06-01', '慢性胃炎', '抑酸护胃治疗', '监狱医务室', '李医生', 'OUTPATIENT', 'TREATING', '奥美拉唑20mg qd', '2026-07-01', 'PENDING', NULL, NULL, ''),
(8, '2026-05-15', '偏头痛', '对症止痛治疗', '监狱医务室', '张医生', 'OUTPATIENT', 'TREATING', '布洛芬缓释胶囊0.3g prn', '2026-06-15', 'PENDING', NULL, NULL, ''),
(1, '2026-05-08', '慢性支气管炎', '止咳化痰、预防感染', '监狱医务室', '李医生', 'OUTPATIENT', 'TREATING', '氨溴索30mg tid', '2026-06-08', 'COMPLETED', '2026-06-08', '病情稳定，继续原方案', '复诊情况良好'),
(10, '2026-04-20', '腰椎间盘突出', '理疗+止痛治疗', '市中医院', '陈医生', 'OUTPATIENT', 'TREATING', '塞来昔布0.2g qd', '2026-05-20', 'MISSED', NULL, NULL, '未按时复诊'),
(10, '2026-03-15', '腰肌劳损', '理疗康复', '市中医院', '陈医生', 'PHYSICAL', 'TREATING', '外用扶他林乳膏', '2026-04-15', 'MISSED', NULL, NULL, '连续两次未复诊，高危人员需关注'),
(2, '2026-06-05', '常规体检', '血压偏高，余未见异常', '监狱医务室', '张医生', 'PHYSICAL', 'TREATING', '继续服用降压药', '2026-06-11', 'PENDING', NULL, NULL, '今日待复诊'),
(4, '2026-06-09', '心理咨询', '抑郁情绪评估，建议继续心理疏导', '省精神卫生中心', '陈医生', 'PSYCHOLOGICAL', 'TREATING', '舍曲林50mg qd', '2026-06-10', 'PENDING', NULL, NULL, '昨日应复诊，已过期');