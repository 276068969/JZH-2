package com.prison.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prison.entity.MedicalRecord;
import com.prison.entity.Prisoner;
import com.prison.entity.User;
import com.prison.mapper.MedicalRecordMapper;
import com.prison.mapper.PrisonerMapper;
import com.prison.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PrisonerMapper prisonerMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            initAdminUser();
        } catch (Exception e) {
            log.error("初始化用户失败", e);
        }
        try {
            initReleaseWarningPrisoners();
        } catch (Exception e) {
            log.error("初始化临释人员数据失败", e);
        }
        try {
            initFollowUpMedicalRecords();
        } catch (Exception e) {
            log.error("初始化复诊医疗数据失败", e);
        }
    }

    private void initAdminUser() {
        List<User> users = userMapper.selectList(null);
        boolean needsUpdate = false;

        for (User user : users) {
            if (!user.getPassword().startsWith("$2a$")) {
                String rawPassword = switch (user.getUsername()) {
                    case "admin" -> "admin123";
                    case "manager" -> "manager123";
                    case "guard" -> "guard123";
                    case "doctor" -> "doctor123";
                    case "viewer" -> "viewer123";
                    default -> null;
                };
                if (rawPassword != null) {
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    userMapper.updateById(user);
                    needsUpdate = true;
                    log.info("用户 {} 密码已加密", user.getUsername());
                }
            }
        }

        if (needsUpdate) {
            log.info("用户密码初始化完成");
        }
    }

    private void initReleaseWarningPrisoners() {
        LocalDate today = LocalDate.now();

        long totalIn90 = countPrisonersInRange(today, today.plusDays(90));
        log.info("当前未来90天内临释人员: {}人, today={}", totalIn90, today);

        if (totalIn90 >= 5) {
            printBreakdown(today);
            log.info("临释人员数据充足，无需补数");
            return;
        }

        log.info("临释人员数据不足，开始补充...");

        insertIfAbsent(buildPrisoner("P20250001", "吴某", "男", "440201199301013456",
                "盗窃罪", 18, today.plusDays(15), "LOW", "INCARCERATED", 1L, 1L,
                "广东省中山市", "表现良好"));
        insertIfAbsent(buildPrisoner("P20250002", "杨某", "男", "440202199708085678",
                "盗窃罪", 12, today.plusDays(5), "LOW", "TRANSFERRED", 1L, 1L,
                "广东省湛江市", "已办理转监手续"));
        insertIfAbsent(buildPrisoner("P20250003", "刘某", "男", "440203199002021111",
                "诈骗罪", 24, today.plusDays(25), "MEDIUM", "INCARCERATED", 2L, 4L,
                "广东省韶关市", ""));
        insertIfAbsent(buildPrisoner("P20250004", "宋某", "女", "440204199104049999",
                "诈骗罪", 18, today.plusDays(20), "LOW", "INCARCERATED", 3L, 6L,
                "广东省云浮市", ""));
        insertIfAbsent(buildPrisoner("P20250005", "王某", "男", "440205199502024567",
                "故意伤害罪", 24, today.plusDays(40), "HIGH", "INCARCERATED", 4L, 9L,
                "广东省江门市", "需重点关注"));
        insertIfAbsent(buildPrisoner("P20250006", "罗某", "男", "440206199205053333",
                "盗窃罪", 12, today.plusDays(35), "LOW", "INCARCERATED", 1L, 2L,
                "广东省河源市", ""));
        insertIfAbsent(buildPrisoner("P20250007", "黄某", "女", "440207198906067777",
                "贪污罪", 36, today.plusDays(48), "LOW", "INCARCERATED", 3L, 6L,
                "广东省清远市", ""));
        insertIfAbsent(buildPrisoner("P20250008", "郑某", "男", "440208198906067890",
                "诈骗罪", 30, today.plusDays(61), "MEDIUM", "INCARCERATED", 2L, 4L,
                "广东省惠州市", ""));
        insertIfAbsent(buildPrisoner("P20250009", "许某", "男", "440209198803032222",
                "抢劫罪", 48, today.plusDays(71), "HIGH", "INCARCERATED", 4L, 9L,
                "广东省梅州市", "有慢性病"));
        insertIfAbsent(buildPrisoner("P20250010", "冯某", "女", "440210198810108765",
                "贪污罪", 36, today.plusDays(83), "LOW", "MEDICAL_PAROLE", 3L, 6L,
                "广东省肇庆市", "保外就医中"));
        insertIfAbsent(buildPrisoner("P20250011", "何某", "男", "440211198707078888",
                "贩毒罪", 60, today.plusDays(81), "EXTREME", "INCARCERATED", 4L, 9L,
                "广东省揭阳市", "高度戒备区"));
        insertIfAbsent(buildPrisoner("P20250012", "唐某", "男", "440212198508085555",
                "抢劫罪", 36, today.plusDays(76), "MEDIUM", "TRANSFERRED", 2L, 5L,
                "广东省潮州市", "转至低戒备区"));
        insertIfAbsent(buildPrisoner("P20250013", "梁某", "男", "440213198309096666",
                "故意伤害罪", 36, today.plusDays(87), "MEDIUM", "INCARCERATED", 1L, 3L,
                "广东省阳江市", ""));

        long afterCount = countPrisonersInRange(today, today.plusDays(90));
        log.info("临释人员数据补充完成: 补充前={}人, 补充后={}人", totalIn90, afterCount);
        printBreakdown(today);
    }

    private void printBreakdown(LocalDate today) {
        long in30 = countPrisonersInRange(today, today.plusDays(30));
        long in60 = countPrisonersInRange(today.plusDays(31), today.plusDays(60));
        long in90 = countPrisonersInRange(today.plusDays(61), today.plusDays(90));
        log.info("=== 临释人员分布统计 === 30天内: {}人, 31-60天: {}人, 61-90天: {}人 ===", in30, in60, in90);
    }

    private long countPrisonersInRange(LocalDate start, LocalDate end) {
        try {
            LambdaQueryWrapper<Prisoner> wrapper = new LambdaQueryWrapper<>();
            wrapper.isNotNull(Prisoner::getReleaseDate)
                    .ge(Prisoner::getReleaseDate, start)
                    .le(Prisoner::getReleaseDate, end)
                    .in(Prisoner::getStatus, "INCARCERATED", "TRANSFERRED", "MEDICAL_PAROLE");
            Long c = prisonerMapper.selectCount(wrapper);
            return c == null ? 0 : c;
        } catch (Exception e) {
            log.error("查询临释人员数量失败", e);
            return 0;
        }
    }

    private LocalDate birthDateFromIdCard(String idCard) {
        try {
            if (idCard != null && idCard.length() >= 14) {
                int year = Integer.parseInt(idCard.substring(6, 10));
                int month = Integer.parseInt(idCard.substring(10, 12));
                int day = Integer.parseInt(idCard.substring(12, 14));
                return LocalDate.of(year, month, day);
            }
        } catch (Exception ignored) {
        }
        return LocalDate.of(1990, 1, 1);
    }

    private Prisoner buildPrisoner(String number, String name, String gender, String idCard,
                                   String crimeType, int sentenceMonths, LocalDate releaseDate,
                                   String dangerLevel, String status, Long areaId, Long cellId,
                                   String nativePlace, String remark) {
        Prisoner p = new Prisoner();
        p.setPrisonerNumber(number);
        p.setName(name);
        p.setGender(gender);
        p.setIdCard(idCard);
        p.setBirthDate(birthDateFromIdCard(idCard));
        p.setNativePlace(nativePlace);
        p.setCrimeType(crimeType);
        p.setSentenceTerm(sentenceMonths);
        p.setEntryDate(releaseDate.minusMonths(sentenceMonths));
        p.setReleaseDate(releaseDate);
        p.setAreaId(areaId);
        p.setCellId(cellId);
        p.setEducationLevel("高中");
        p.setMaritalStatus("未婚");
        p.setOccupation("无");
        p.setHealthStatus("良好");
        p.setDangerLevel(dangerLevel);
        p.setStatus(status);
        p.setRemark(remark);
        p.setDeleted(0);
        return p;
    }

    private void insertIfAbsent(Prisoner prisoner) {
        try {
            LambdaQueryWrapper<Prisoner> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Prisoner::getPrisonerNumber, prisoner.getPrisonerNumber());
            Long existing = prisonerMapper.selectCount(wrapper);
            if (existing != null && existing > 0) {
                log.info("临释人员已存在: {} 编号={}", prisoner.getName(), prisoner.getPrisonerNumber());
                return;
            }
            int rows = prisonerMapper.insert(prisoner);
            if (rows > 0) {
                log.info("插入临释人员成功: {} 释放日期={} 状态={} 危险等级={}",
                        prisoner.getName(), prisoner.getReleaseDate(), prisoner.getStatus(), prisoner.getDangerLevel());
            } else {
                log.warn("插入临释人员影响行数为0: {}", prisoner.getName());
            }
        } catch (Exception e) {
            log.error("插入临释人员失败: {} - {}", prisoner.getName(), e.getMessage(), e);
        }
    }

    private void initFollowUpMedicalRecords() {
        LocalDate today = LocalDate.now();
        Long total = medicalRecordMapper.selectCount(
                new LambdaQueryWrapper<MedicalRecord>().isNotNull(MedicalRecord::getFollowUpDate)
        );
        if (total != null && total >= 6) {
            log.info("复诊医疗记录已存在（{}条），跳过动态初始化", total);
            return;
        }
        List<Prisoner> prisoners = prisonerMapper.selectList(
                new LambdaQueryWrapper<Prisoner>().last("LIMIT 8")
        );
        if (prisoners.isEmpty()) {
            log.warn("无可用服刑人员数据，跳过复诊医疗记录初始化");
            return;
        }

        String[] diagnoses = {"高血压病2级", "2型糖尿病", "冠心病 稳定型心绞痛", "慢性胃炎伴糜烂",
                "慢性阻塞性肺疾病", "腰椎间盘突出症", "焦虑状态", "高脂血症"};
        String[] results = {"TREATING", "TREATING", "STABLE", "CURED", "TREATING", "STABLE", "TREATING", "STABLE"};
        String[] doctorNames = {"王医生", "李医生", "王医生", "张医生", "李医生", "王医生", "赵医生", "张医生"};
        int[] offsets = {0, 1, 3, 7, 14, 30, -5, -15};
        String[] statuses = {"PENDING", "PENDING", "PENDING", "PENDING", "PENDING", "PENDING", "MISSED", "MISSED"};
        String[] remarks = {
                "每日监测血压，规律服药", "控制饮食，每周复查血糖",
                "避免剧烈活动，随身携带硝酸甘油", "清淡饮食，抑酸护胃治疗",
                "戒烟，规律使用支气管扩张剂", "理疗康复，避免久坐久站",
                "心理疏导，规律作息", "低脂饮食，3个月后复查血脂"
        };

        int created = 0;
        for (int i = 0; i < Math.min(prisoners.size(), 8); i++) {
            Prisoner p = prisoners.get(i);
            MedicalRecord record = new MedicalRecord();
            record.setPrisonerId(p.getId());
            record.setRecordDate(today.minusDays(30 + i));
            record.setDiagnosis(diagnoses[i]);
            record.setResult(results[i]);
            record.setTreatment(remarks[i]);
            record.setDoctorName(doctorNames[i]);
            record.setFollowUpDate(today.plusDays(offsets[i]));
            record.setFollowUpStatus(statuses[i]);
            if (offsets[i] < 0) {
                record.setFollowUpRemark("逾期未复诊，需尽快联系");
            }
            int rows = medicalRecordMapper.insert(record);
            if (rows > 0) {
                created++;
                log.info("初始化复诊记录: {}({}) 诊断={} 复诊日期={} 状态={}",
                        p.getName(), p.getPrisonerNumber(), diagnoses[i], record.getFollowUpDate(), statuses[i]);
            }
        }

        if (prisoners.size() >= 2) {
            Prisoner p = prisoners.get(0);
            MedicalRecord missed1 = new MedicalRecord();
            missed1.setPrisonerId(p.getId());
            missed1.setRecordDate(today.minusDays(90));
            missed1.setDiagnosis("高血压病2级");
            missed1.setResult("TREATING");
            missed1.setTreatment("规律服药，每月复诊");
            missed1.setDoctorName("王医生");
            missed1.setFollowUpDate(today.minusDays(60));
            missed1.setFollowUpStatus("MISSED");
            missed1.setFollowUpRemark("上月未复诊");
            medicalRecordMapper.insert(missed1);
            log.info("初始化连续未复诊记录（第1次）: {} 复诊日期={}", p.getName(), missed1.getFollowUpDate());

            MedicalRecord done = new MedicalRecord();
            done.setPrisonerId(prisoners.get(1).getId());
            done.setRecordDate(today.minusDays(45));
            done.setDiagnosis("2型糖尿病");
            done.setResult("STABLE");
            done.setTreatment("控制饮食，规律运动");
            done.setDoctorName("李医生");
            done.setFollowUpDate(today.minusDays(15));
            done.setFollowUpStatus("COMPLETED");
            done.setActualFollowUpDate(today.minusDays(16));
            done.setFollowUpResult("血糖控制平稳，继续维持当前方案");
            done.setFollowUpRemark("患者依从性好");
            medicalRecordMapper.insert(done);
            log.info("初始化已完成复诊记录: {} 实际复诊日期={}", prisoners.get(1).getName(), done.getActualFollowUpDate());
        }

        log.info("复诊医疗记录初始化完成，共新增 {} 条", created + 2);
    }
}
