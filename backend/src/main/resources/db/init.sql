CREATE DATABASE IF NOT EXISTS prison_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE prison_db;

DROP TABLE IF EXISTS medical_records;
DROP TABLE IF EXISTS visitors;
DROP TABLE IF EXISTS incidents;
DROP TABLE IF EXISTS patrols;
DROP TABLE IF EXISTS prisoners;
DROP TABLE IF EXISTS guards;
DROP TABLE IF EXISTS cells;
DROP TABLE IF EXISTS prison_areas;
DROP TABLE IF EXISTS users;

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
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED',
    id_card_photo VARCHAR(255),
    visitor_count INT NOT NULL DEFAULT 1,
    purpose VARCHAR(255),
    approve_guard_id BIGINT,
    remark TEXT,
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
('P20240005', '周某', '男', '440105199507071234', '1995-07-07', '广东省佛山市', '贩毒罪', 120, '2024-05-15', '2034-05-14', 4, 9, '高中', '未婚', 'HIGH', 'INCARCERATED');

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

INSERT INTO incidents (incident_title, incident_type, severity, area_id, report_guard_id, related_prisoner_id, occur_time, description, status) VALUES
('服刑人员口角纠纷', 'DISCIPLINE', 'LOW', 1, 1, 1, '2024-05-20 10:30:00', '两名服刑人员因生活琐事发生口角，已及时制止', 'RESOLVED'),
('服刑人员突发腹痛', 'MEDICAL', 'MEDIUM', 2, 2, 3, '2024-05-25 15:00:00', '服刑人员孙某突发腹痛，送医务室处理后好转', 'RESOLVED'),
('发现可疑物品', 'OTHER', 'MEDIUM', 4, 4, 5, '2024-06-01 09:00:00', '巡查中发现不明金属物品，正在调查来源', 'PROCESSING');

INSERT INTO visitors (visitor_name, id_card, phone, relation, prisoner_id, visit_date, visit_time_slot, status, visitor_count, purpose, approve_guard_id) VALUES
('赵某母亲', '440101196501010000', '13800000011', 'PARENT', 1, '2024-06-05', 'AM', 'APPROVED', 1, '探视', 1),
('钱某妻子', '440102199001010000', '13800000012', 'SPOUSE', 2, '2024-06-06', 'PM', 'PENDING', 2, '探视并送物品', NULL),
('孙某律师', '440103198006010000', '13800000013', 'LAWYER', 3, '2024-06-07', 'AM', 'APPROVED', 1, '法律咨询', 2);

INSERT INTO medical_records (prisoner_id, record_date, diagnosis, treatment, hospital, doctor_name, medical_type, result, medicine) VALUES
(1, '2024-05-10', '上呼吸道感染', '给予抗感染治疗，嘱休息多饮水', '监狱医务室', '张医生', 'OUTPATIENT', 'RECOVERED', '阿莫西林0.5g tid'),
(2, '2024-05-15', '高血压病', '给予降压治疗，定期监测血压', '监狱医务室', '张医生', 'OUTPATIENT', 'TREATING', '硝苯地平控释片30mg qd'),
(3, '2024-05-25', '急性胃肠炎', '补液、止泻、抗感染治疗', '监狱医务室', '李医生', 'EMERGENCY', 'RECOVERED', '蒙脱石散3g tid，左氧氟沙星0.2g bid');