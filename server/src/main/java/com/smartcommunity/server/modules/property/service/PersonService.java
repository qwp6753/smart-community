package com.smartcommunity.server.modules.property.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.server.modules.property.entity.Person;
import com.smartcommunity.server.modules.property.mapper.PersonMapper;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

    private final PersonMapper personMapper;

    public PersonService(PersonMapper personMapper) {
        this.personMapper = personMapper;
    }

    public Page<Person> page(Integer current, Integer size, String userName, Long communityId) {
        LambdaQueryWrapper<Person> wrapper = new LambdaQueryWrapper<>();
        if (userName != null && !userName.isBlank()) {
            wrapper.like(Person::getUserName, userName);
        }
        if (communityId != null) {
            wrapper.eq(Person::getCommunityId, communityId);
        }
        wrapper.orderByDesc(Person::getCreateTime);
        return personMapper.selectPage(new Page<>(current, size), wrapper);
    }

    public Person getById(Long id) {
        return personMapper.selectById(id);
    }

    public boolean save(Person person) {
        return personMapper.insert(person) > 0;
    }

    public boolean update(Person person) {
        return personMapper.updateById(person) > 0;
    }

    public boolean delete(Long id) {
        return personMapper.deleteById(id) > 0;
    }

    public long count() {
        return personMapper.selectCount(null);
    }
}
