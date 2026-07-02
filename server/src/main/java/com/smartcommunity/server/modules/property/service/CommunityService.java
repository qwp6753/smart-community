package com.smartcommunity.server.modules.property.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.server.modules.property.entity.Community;
import com.smartcommunity.server.modules.property.mapper.CommunityMapper;
import org.springframework.stereotype.Service;

@Service
public class CommunityService {

    private final CommunityMapper communityMapper;

    public CommunityService(CommunityMapper communityMapper) {
        this.communityMapper = communityMapper;
    }

    public Page<Community> page(Integer current, Integer size, String name) {
        LambdaQueryWrapper<Community> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            wrapper.like(Community::getName, name);
        }
        wrapper.orderByDesc(Community::getCreateTime);
        return communityMapper.selectPage(new Page<>(current, size), wrapper);
    }

    public Community getById(Long id) {
        return communityMapper.selectById(id);
    }

    public boolean save(Community community) {
        return communityMapper.insert(community) > 0;
    }

    public boolean update(Community community) {
        return communityMapper.updateById(community) > 0;
    }

    public boolean delete(Long id) {
        return communityMapper.deleteById(id) > 0;
    }

    public long count() {
        return communityMapper.selectCount(null);
    }
}
