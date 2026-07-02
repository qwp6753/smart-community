package com.smartcommunity.server.thirdparty.map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BaiduMapService implements MapService {

    @Value("${app.map.baidu-ak:}")
    private String baiduAk;

    @Override
    public MapConfigResult getConfig() {
        MapConfigResult result = new MapConfigResult();
        result.setAk(baiduAk);
        result.setCenterLng("116.480");
        result.setCenterLat("39.996");
        result.setMarkers(List.of(
                Map.of("name", "智慧社区一号", "lng", 116.480, "lat", 39.996),
                Map.of("name", "智慧社区二号", "lng", 121.590, "lat", 31.208)
        ));
        return result;
    }
}
