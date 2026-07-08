package com.smartcommunity.server.modules.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.server.modules.access.entity.InOutRecord;
import com.smartcommunity.server.modules.access.mapper.InOutRecordMapper;
import com.smartcommunity.server.modules.access.mapper.VisitorMapper;
import com.smartcommunity.server.modules.access.service.InOutRecordService;
import com.smartcommunity.server.modules.access.service.VisitorService;
import com.smartcommunity.server.modules.property.entity.Community;
import com.smartcommunity.server.modules.property.entity.Person;
import com.smartcommunity.server.modules.property.mapper.CommunityMapper;
import com.smartcommunity.server.modules.property.mapper.PersonMapper;
import com.smartcommunity.server.modules.property.service.CameraService;
import com.smartcommunity.server.modules.property.service.CommunityService;
import com.smartcommunity.server.modules.property.service.PersonService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardService {

    // 基础 Service 注入
    private final CommunityService communityService;
    private final PersonService personService;
    private final CameraService cameraService;
    private final InOutRecordService recordService;
    private final VisitorService visitorService;

    // 基础 Mapper 注入 (直接利用 BaseMapper 解决复杂统计查询)
    private final CommunityMapper communityMapper;
    private final PersonMapper personMapper;
    private final InOutRecordMapper recordMapper;
    private final VisitorMapper visitorMapper;

    public DashboardService(CommunityService communityService,
                            PersonService personService,
                            CameraService cameraService,
                            InOutRecordService recordService,
                            VisitorService visitorService,
                            CommunityMapper communityMapper,
                            PersonMapper personMapper,
                            InOutRecordMapper recordMapper,
                            VisitorMapper visitorMapper) {
        this.communityService = communityService;
        this.personService = personService;
        this.cameraService = cameraService;
        this.recordService = recordService;
        this.visitorService = visitorService;
        this.communityMapper = communityMapper;
        this.personMapper = personMapper;
        this.recordMapper = recordMapper;
        this.visitorMapper = visitorMapper;
    }

    public Map<String, Object> stats() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 基础数字卡片统计
        result.put("communityCount", communityService.count());
        result.put("personCount", personService.count());
        result.put("cameraCount", cameraService.count());
        result.put("recordCount", recordService.count());
        result.put("visitorCount", visitorService.count());

        // 2. 人员类型分布：常住居民 VS 登记访客
        List<Map<String, Object>> personTypeDistribution = new ArrayList<>();
        Map<String, Object> residentMap = new HashMap<>();
        residentMap.put("name", "常住居民");
        residentMap.put("value", personService.count());
        personTypeDistribution.add(residentMap);

        Map<String, Object> visitorMap = new HashMap<>();
        visitorMap.put("name", "登记访客");
        visitorMap.put("value", visitorService.count());
        personTypeDistribution.add(visitorMap);

        result.put("personTypeDistribution", personTypeDistribution);

        // 3. 各小区常住人口分布统计 (严格遵照 Community 实体类的 getCommunityId 和 getName)
        List<Community> communities = communityMapper.selectList(null);
        List<Map<String, Object>> communityPersonStats = new ArrayList<>();
        if (communities != null) {
            for (Community c : communities) {
                if (c.getCommunityId() != null) {
                    LambdaQueryWrapper<Person> qw = new LambdaQueryWrapper<>();
                    qw.eq(Person::getCommunityId, c.getCommunityId());
                    long count = personMapper.selectCount(qw);

                    Map<String, Object> statMap = new HashMap<>();
                    // 如果小区名字为null，给个默认名称避免前端 ECharts 报错
                    statMap.put("communityName", c.getName() != null ? c.getName() : "未知小区");
                    statMap.put("count", count);
                    communityPersonStats.add(statMap);
                }
            }
        }
        result.put("communityPersonStats", communityPersonStats);

        // 4. 近 7 天出入流量折线图趋势统计
        List<String> dates = new ArrayList<>();
        List<Long> inCounts = new ArrayList<>();
        List<Long> outCounts = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            String dateStr = today.minusDays(i).format(formatter);
            dates.add(dateStr);

            // 统计当天进入人次 (匹配 InOutRecord 中 time 字段的前缀)
            LambdaQueryWrapper<InOutRecord> inQw = new LambdaQueryWrapper<>();
            inQw.likeRight(InOutRecord::getTime, dateStr).eq(InOutRecord::getType, "in");
            inCounts.add(recordMapper.selectCount(inQw));

            // 统计当天离开人次
            LambdaQueryWrapper<InOutRecord> outQw = new LambdaQueryWrapper<>();
            outQw.likeRight(InOutRecord::getTime, dateStr).eq(InOutRecord::getType, "out");
            outCounts.add(recordMapper.selectCount(outQw));
        }

        Map<String, Object> trendMap = new HashMap<>();
        trendMap.put("dates", dates);
        trendMap.put("in", inCounts);
        trendMap.put("out", outCounts);
        result.put("accessTrend", trendMap);

        return result;
    }
}