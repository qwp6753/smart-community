package com.smartcommunity.server.modules.property.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.server.common.api.ApiResponse;
import com.smartcommunity.server.modules.property.entity.Camera;
import com.smartcommunity.server.modules.property.service.CameraService;
import com.smartcommunity.server.security.RequirePermission;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cameras")
public class CameraController {

    private final CameraService cameraService;

    public CameraController(CameraService cameraService) {
        this.cameraService = cameraService;
    }

    @RequirePermission("property:camera:view")
    @GetMapping
    public ApiResponse<Page<Camera>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long communityId) {
        return ApiResponse.success(cameraService.page(current, size, name, communityId));
    }

    @RequirePermission("property:camera:view")
    @GetMapping("/{id}")
    public ApiResponse<Camera> detail(@PathVariable Long id) {
        return ApiResponse.success(cameraService.getById(id));
    }

    @RequirePermission("property:camera:edit")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody Camera camera) {
        cameraService.save(camera);
        return ApiResponse.success(null);
    }

    @RequirePermission("property:camera:edit")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Camera camera) {
        camera.setCameraId(id);
        cameraService.update(camera);
        return ApiResponse.success(null);
    }

    @RequirePermission("property:camera:edit")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        cameraService.delete(id);
        return ApiResponse.success(null);
    }
}
