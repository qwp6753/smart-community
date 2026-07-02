package com.smartcommunity.server.modules.access.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.server.modules.access.entity.Visitor;
import com.smartcommunity.server.modules.access.mapper.VisitorMapper;
import org.springframework.stereotype.Service;

@Service
public class VisitorService {

    private final VisitorMapper visitorMapper;

    public VisitorService(VisitorMapper visitorMapper) {
        this.visitorMapper = visitorMapper;
    }

    public Page<Visitor> page(Integer current, Integer size, String name, Long communityId) {
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            wrapper.like(Visitor::getName, name);
        }
        if (communityId != null) {
            wrapper.eq(Visitor::getCommunityId, communityId);
        }
        wrapper.orderByDesc(Visitor::getCreateTime);
        return visitorMapper.selectPage(new Page<>(current, size), wrapper);
    }

    public Visitor getById(Long id) {
        return visitorMapper.selectById(id);
    }

    public boolean save(Visitor visitor) {
        return visitorMapper.insert(visitor) > 0;
    }

    public boolean update(Visitor visitor) {
        return visitorMapper.updateById(visitor) > 0;
    }

    public boolean delete(Long id) {
        return visitorMapper.deleteById(id) > 0;
    }

    public long count() {
        return visitorMapper.selectCount(null);
    }
}
