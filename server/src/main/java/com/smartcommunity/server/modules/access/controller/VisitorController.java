package com.smartcommunity.server.modules.access.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.server.common.api.ApiResponse;
import com.smartcommunity.server.modules.access.entity.Visitor;
import com.smartcommunity.server.modules.access.service.VisitorService;
import com.smartcommunity.server.security.RequirePermission;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visitors")
public class VisitorController {

    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @RequirePermission("access:visitor:view")
    @GetMapping
    public ApiResponse<Page<Visitor>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long communityId) {
        return ApiResponse.success(visitorService.page(current, size, name, communityId));
    }

    @RequirePermission("access:visitor:view")
    @GetMapping("/{id}")
    public ApiResponse<Visitor> detail(@PathVariable Long id) {
        return ApiResponse.success(visitorService.getById(id));
    }

    @RequirePermission("access:visitor:edit")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody Visitor visitor) {
        visitorService.save(visitor);
        return ApiResponse.success(null);
    }

    @RequirePermission("access:visitor:edit")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Visitor visitor) {
        visitor.setVisitorId(id);
        visitorService.update(visitor);
        return ApiResponse.success(null);
    }

    @RequirePermission("access:visitor:edit")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        visitorService.delete(id);
        return ApiResponse.success(null);
    }
}
