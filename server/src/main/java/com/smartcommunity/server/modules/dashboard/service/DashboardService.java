package com.smartcommunity.server.modules.dashboard.service;

import com.smartcommunity.server.modules.access.service.InOutRecordService;
import com.smartcommunity.server.modules.access.service.VisitorService;
import com.smartcommunity.server.modules.property.service.CameraService;
import com.smartcommunity.server.modules.property.service.CommunityService;
import com.smartcommunity.server.modules.property.service.PersonService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final CommunityService communityService;
    private final PersonService personService;
    private final CameraService cameraService;
    private final InOutRecordService recordService;
    private final VisitorService visitorService;

    public DashboardService(CommunityService communityService,
                            PersonService personService,
                            CameraService cameraService,
                            InOutRecordService recordService,
                            VisitorService visitorService) {
        this.communityService = communityService;
        this.personService = personService;
        this.cameraService = cameraService;
        this.recordService = recordService;
        this.visitorService = visitorService;
    }

    public Map<String, Object> stats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("communityCount", communityService.count());
        result.put("personCount", personService.count());
        result.put("cameraCount", cameraService.count());
        result.put("recordCount", recordService.count());
        result.put("visitorCount", visitorService.count());
        return result;
    }
}
