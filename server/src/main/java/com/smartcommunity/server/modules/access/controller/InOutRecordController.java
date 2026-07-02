package com.smartcommunity.server.modules.access.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.server.common.api.ApiResponse;
import com.smartcommunity.server.modules.access.entity.InOutRecord;
import com.smartcommunity.server.modules.access.service.InOutRecordService;
import com.smartcommunity.server.security.RequirePermission;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records")
public class InOutRecordController {

    private final InOutRecordService recordService;

    public InOutRecordController(InOutRecordService recordService) {
        this.recordService = recordService;
    }

    @RequirePermission("access:record:view")
    @GetMapping
    public ApiResponse<Page<InOutRecord>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String personName,
            @RequestParam(required = false) String type) {
        return ApiResponse.success(recordService.page(current, size, personName, type));
    }
}
