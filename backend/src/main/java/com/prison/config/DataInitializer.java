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
        initAdminUser();
        initReleaseWarningPrisoners();
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
        LambdaQueryWrapper<Prisoner> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.isNotNull(Prisoner::getReleaseDate)
                .ge(Prisoner::getReleaseDate, today)
                .le(Prisoner::getReleaseDate, today.plusDays(90));
        Long count = prisonerMapper.selectCount(checkWrapper);

        if (count != null && count > 0) {
            log.info("临释人员数据已存在，共 {} 人，无需初始化", count);
            return;
        }

        log.info("未检测到临释人员数据，开始初始化...");

        insertIfAbsent(buildPrisoner("P20240020", "吴某", "男", "440106199301013456",
                "盗窃罪", today.plusDays(15), "LOW", "INCARCERATED", "广东省中山市", "表现良好"));

        insertIfAbsent(buildPrisoner("P20240021", "杨某", "男", "440111199708085678",
                "盗窃罪", today.plusDays(5), "LOW", "TRANSFERRED", "广东省湛江市", "已办理转监手续"));

        insertIfAbsent(buildPrisoner("P20240022", "刘某", "男", "440112199002021111",
                "诈骗罪", today.plusDays(25), "MEDIUM", "INCARCERATED", "广东省韶关市", ""));

        insertIfAbsent(buildPrisoner("P20240023", "王某", "男", "440108199502024567",
                "故意伤害罪", today.plusDays(40), "HIGH", "INCARCERATED", "广东省江门市", "需重点关注"));

        insertIfAbsent(buildPrisoner("P20240024", "罗某", "男", "440113199205053333",
                "盗窃罪", today.plusDays(35), "LOW", "INCARCERATED", "广东省河源市", ""));

        insertIfAbsent(buildPrisoner("P20240025", "黄某", "女", "440114198906067777",
                "贪污罪", today.plusDays(48), "LOW", "INCARCERATED", "广东省清远市", ""));

        insertIfAbsent(buildPrisoner("P20240026", "宋某", "女", "440115199104049999",
                "诈骗罪", today.plusDays(20), "LOW", "INCARCERATED", "广东省云浮市", ""));

        insertIfAbsent(buildPrisoner("P20240027", "郑某", "男", "440107198906067890",
                "诈骗罪", today.plusDays(61), "MEDIUM", "INCARCERATED", "广东省惠州市", ""));

        insertIfAbsent(buildPrisoner("P20240028", "许某", "男", "440116198803032222",
                "抢劫罪", today.plusDays(71), "HIGH", "INCARCERATED", "广东省梅州市", "有慢性病"));

        insertIfAbsent(buildPrisoner("P20240029", "冯某", "女", "440109198810108765",
                "贪污罪", today.plusDays(83), "LOW", "MEDICAL_PAROLE", "广东省肇庆市", "保外就医中"));

        insertIfAbsent(buildPrisoner("P20240030", "何某", "男", "440117198707078888",
                "贩毒罪", today.plusDays(81), "EXTREME", "INCARCERATED", "广东省揭阳市", "高度戒备区"));

        insertIfAbsent(buildPrisoner("P20240031", "唐某", "男", "440118198508085555",
                "抢劫罪", today.plusDays(76), "MEDIUM", "TRANSFERRED", "广东省潮州市", "转至低戒备区"));

        insertIfAbsent(buildPrisoner("P20240032", "梁某", "男", "440119198309096666",
                "故意伤害罪", today.plusDays(87), "MEDIUM", "INCARCERATED", "广东省阳江市", ""));

        log.info("临释人员数据初始化完成");
    }

    private Prisoner buildPrisoner(String number, String name, String gender, String idCard,
                                   String crimeType, LocalDate releaseDate, String dangerLevel,
                                   String status, String nativePlace, String remark) {
        Prisoner p = new Prisoner();
        p.setPrisonerNumber(number);
        p.setName(name);
        p.setGender(gender);
        p.setIdCard(idCard);
        p.setCrimeType(crimeType);
        p.setReleaseDate(releaseDate);
        p.setEntryDate(releaseDate.minusMonths(12));
        p.setDangerLevel(dangerLevel);
        p.setStatus(status);
        p.setNativePlace(nativePlace);
        p.setHealthStatus("良好");
        p.setEducationLevel("高中");
        p.setRemark(remark);
        return p;
    }

    private void insertIfAbsent(Prisoner prisoner) {
        LambdaQueryWrapper<Prisoner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prisoner::getPrisonerNumber, prisoner.getPrisonerNumber());
        if (prisonerMapper.selectCount(wrapper) == 0) {
            prisonerMapper.insert(prisoner);
            log.info("插入临释人员: {} ({})", prisoner.getName(), prisoner.getReleaseDate());
        }
    }
}