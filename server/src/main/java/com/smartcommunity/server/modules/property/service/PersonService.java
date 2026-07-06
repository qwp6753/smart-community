package com.smartcommunity.server.modules.property.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.server.modules.property.entity.Person;
import com.smartcommunity.server.modules.property.mapper.PersonMapper;
import com.smartcommunity.server.thirdparty.face.FaceRecognitionService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
public class PersonService {

    private final PersonMapper personMapper;
    private final FaceRecognitionService faceRecognitionService;

    /** 人脸照片存储目录 */
    private static final Path FACE_DIR = Paths.get("data", "faces");

    public PersonService(PersonMapper personMapper,
                         FaceRecognitionService faceRecognitionService) {
        this.personMapper = personMapper;
        this.faceRecognitionService = faceRecognitionService;
        // 确保存储目录存在
        try {
            Files.createDirectories(FACE_DIR);
        } catch (IOException ignored) {
        }
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
        Person person = getById(id);
        if (person != null && person.getFaceUrl() != null) {
            // 删除本地人脸照片
            try {
                Files.deleteIfExists(Paths.get(person.getFaceUrl()));
            } catch (IOException ignored) {
            }
            // 从腾讯云人员库删除
            faceRecognitionService.removeFace(String.valueOf(id));
        }
        return personMapper.deleteById(id) > 0;
    }

    public long count() {
        return personMapper.selectCount(null);
    }

    /**
     * 保存人脸照片
     * @param personId 人员ID
     * @param imageBase64 人脸照片 base64
     */
    public String saveFace(Long personId, String imageBase64) {
        Person person = getById(personId);
        if (person == null) {
            throw new RuntimeException("人员不存在");
        }

        // 删除旧照片
        if (person.getFaceUrl() != null) {
            try {
                Files.deleteIfExists(Paths.get(person.getFaceUrl()));
            } catch (IOException ignored) {
            }
        }

        // 保存新照片到磁盘
        String fileName = personId + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
        Path filePath = FACE_DIR.resolve(fileName);
        try {
            String base64Data = imageBase64.contains(",")
                    ? imageBase64.substring(imageBase64.indexOf(",") + 1)
                    : imageBase64;
            Files.write(filePath, Base64.getDecoder().decode(base64Data));
        } catch (IOException e) {
            throw new RuntimeException("保存人脸照片失败", e);
        }

        // 更新数据库
        String faceUrl = filePath.toString();
        person.setFaceUrl(faceUrl);
        personMapper.updateById(person);

        // 注册到人脸识别服务的人员库
        try {
            faceRecognitionService.registerFace(String.valueOf(personId), person.getUserName(), imageBase64);
        } catch (Exception e) {
            // 注册失败不阻塞主流程，打印日志即可
            e.printStackTrace();
        }

        return faceUrl;
    }

    /**
     * 读取人脸照片为 base64
     */
    public String getFaceBase64(Long personId) {
        Person person = getById(personId);
        if (person == null || person.getFaceUrl() == null) {
            return null;
        }
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(person.getFaceUrl()));
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            return null;
        }
    }
}
