package com.smartcommunity.server.thirdparty.controller;

import com.smartcommunity.server.common.api.ApiResponse;
import com.smartcommunity.server.thirdparty.map.MapConfigResult;
import com.smartcommunity.server.thirdparty.map.MapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/map")
public class MapController {

    private final MapService mapService;

    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/config")
    public ApiResponse<MapConfigResult> config() {
        return ApiResponse.success(mapService.getConfig());
    }
}
