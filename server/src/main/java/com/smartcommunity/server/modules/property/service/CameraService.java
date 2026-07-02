package com.smartcommunity.server.modules.property.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.server.modules.property.entity.Camera;
import com.smartcommunity.server.modules.property.mapper.CameraMapper;
import org.springframework.stereotype.Service;

@Service
public class CameraService {

    private final CameraMapper cameraMapper;

    public CameraService(CameraMapper cameraMapper) {
        this.cameraMapper = cameraMapper;
    }

    public Page<Camera> page(Integer current, Integer size, String name, Long communityId) {
        LambdaQueryWrapper<Camera> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            wrapper.like(Camera::getName, name);
        }
        if (communityId != null) {
            wrapper.eq(Camera::getCommunityId, communityId);
        }
        wrapper.orderByDesc(Camera::getCreateTime);
        return cameraMapper.selectPage(new Page<>(current, size), wrapper);
    }

    public Camera getById(Long id) {
        return cameraMapper.selectById(id);
    }

    public boolean save(Camera camera) {
        return cameraMapper.insert(camera) > 0;
    }

    public boolean update(Camera camera) {
        return cameraMapper.updateById(camera) > 0;
    }

    public boolean delete(Long id) {
        return cameraMapper.deleteById(id) > 0;
    }

    public long count() {
        return cameraMapper.selectCount(null);
    }
}
