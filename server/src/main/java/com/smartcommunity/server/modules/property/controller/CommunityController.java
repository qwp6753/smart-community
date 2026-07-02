package com.smartcommunity.server.modules.property.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.server.common.api.ApiResponse;
import com.smartcommunity.server.modules.property.entity.Community;
import com.smartcommunity.server.modules.property.service.CommunityService;
import com.smartcommunity.server.security.RequirePermission;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @RequirePermission("property:community:view")
    @GetMapping
    public ApiResponse<Page<Community>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {
        return ApiResponse.success(communityService.page(current, size, name));
    }

    @RequirePermission("property:community:view")
    @GetMapping("/{id}")
    public ApiResponse<Community> detail(@PathVariable Long id) {
        return ApiResponse.success(communityService.getById(id));
    }

    @RequirePermission("property:community:edit")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody Community community) {
        communityService.save(community);
        return ApiResponse.success(null);
    }

    @RequirePermission("property:community:edit")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Community community) {
        community.setCommunityId(id);
        communityService.update(community);
        return ApiResponse.success(null);
    }

    @RequirePermission("property:community:edit")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        communityService.delete(id);
        return ApiResponse.success(null);
    }
}
