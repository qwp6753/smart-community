package com.smartcommunity.server.modules.access.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.server.modules.access.entity.InOutRecord;
import com.smartcommunity.server.modules.access.mapper.InOutRecordMapper;
import org.springframework.stereotype.Service;

@Service
public class InOutRecordService {

    private final InOutRecordMapper recordMapper;

    public InOutRecordService(InOutRecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    public Page<InOutRecord> page(Integer current, Integer size, String personName, String type) {
        LambdaQueryWrapper<InOutRecord> wrapper = new LambdaQueryWrapper<>();
        if (personName != null && !personName.isBlank()) {
            wrapper.like(InOutRecord::getPersonName, personName);
        }
        if (type != null && !type.isBlank()) {
            wrapper.eq(InOutRecord::getType, type);
        }
        wrapper.orderByDesc(InOutRecord::getCreateTime);
        return recordMapper.selectPage(new Page<>(current, size), wrapper);
    }

    public InOutRecord getById(Long id) {
        return recordMapper.selectById(id);
    }

    public boolean save(InOutRecord record) {
        return recordMapper.insert(record) > 0;
    }

    public boolean update(InOutRecord record) {
        return recordMapper.updateById(record) > 0;
    }

    public boolean delete(Long id) {
        return recordMapper.deleteById(id) > 0;
    }

    public long count() {
        return recordMapper.selectCount(null);
    }
}
