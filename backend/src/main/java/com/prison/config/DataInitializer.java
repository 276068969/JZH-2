package com.prison.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prison.entity.Prisoner;
import com.prison.entity.User;
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
}
