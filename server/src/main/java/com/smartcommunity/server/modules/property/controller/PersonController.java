package com.smartcommunity.server.modules.property.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.server.common.api.ApiResponse;
import com.smartcommunity.server.modules.property.entity.Person;
import com.smartcommunity.server.modules.property.service.PersonService;
import com.smartcommunity.server.security.RequirePermission;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/persons")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @RequirePermission("property:person:view")
    @GetMapping
    public ApiResponse<Page<Person>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Long communityId) {
        return ApiResponse.success(personService.page(current, size, userName, communityId));
    }

    @RequirePermission("property:person:view")
    @GetMapping("/{id}")
    public ApiResponse<Person> detail(@PathVariable Long id) {
        return ApiResponse.success(personService.getById(id));
    }

    @RequirePermission("property:person:edit")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody Person person) {
        personService.save(person);
        return ApiResponse.success(null);
    }

    @RequirePermission("property:person:edit")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Person person) {
        person.setPersonId(id);
        personService.update(person);
        return ApiResponse.success(null);
    }

    @RequirePermission("property:person:edit")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        personService.delete(id);
        return ApiResponse.success(null);
    }

    /** 上传人脸照片 */
    @RequirePermission("property:person:edit")
    @PostMapping("/{id}/face")
    public ApiResponse<String> uploadFace(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        String image = body.get("image");
        if (image == null || image.isBlank()) {
            return ApiResponse.fail(400, "图片数据不能为空");
        }
        String faceUrl = personService.saveFace(id, image);
        return ApiResponse.success("人脸照片上传成功", faceUrl);
    }

    /** 获取人脸照片（base64） */
    @RequirePermission("property:person:view")
    @GetMapping("/{id}/face")
    public ApiResponse<String> getFace(@PathVariable Long id) {
        String base64 = personService.getFaceBase64(id);
        if (base64 == null) {
            return ApiResponse.fail(404, "未找到人脸照片");
        }
        return ApiResponse.success(base64);
    }
}
