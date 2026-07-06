package com.smartcommunity.server.thirdparty.controller;

import com.smartcommunity.server.common.api.ApiResponse;
import com.smartcommunity.server.modules.access.entity.InOutRecord;
import com.smartcommunity.server.modules.access.service.InOutRecordService;
import com.smartcommunity.server.modules.property.entity.Person;
import com.smartcommunity.server.modules.property.service.PersonService;
import com.smartcommunity.server.thirdparty.face.FaceRecognitionResult;
import com.smartcommunity.server.thirdparty.face.FaceRecognitionService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/face")
public class FaceRecognitionController {

    private final FaceRecognitionService faceService;
    private final PersonService personService;
    private final InOutRecordService recordService;

    public FaceRecognitionController(FaceRecognitionService faceService,
                                     PersonService personService,
                                     InOutRecordService recordService) {
        this.faceService = faceService;
        this.personService = personService;
        this.recordService = recordService;
    }

    /**
     * 人脸识别（1:N）——移动端拍照识别 / 摄像头抓拍识别
     * 识别成功后自动创建出入记录
     */
    @PostMapping("/identify")
    public ApiResponse<Map<String, Object>> identify(@RequestBody Map<String, String> body) {
        String imageBase64 = body.get("image");
        String type = body.getOrDefault("type", "in"); // in=进入, out=离开
        String location = body.getOrDefault("location", "门禁识别");
        Long communityId = body.containsKey("communityId")
                ? Long.valueOf(body.get("communityId")) : 1L;

        FaceRecognitionResult result = faceService.identify(imageBase64);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("success", result.isSuccess());
        data.put("personName", result.getPersonName());
        data.put("confidence", result.getConfidence());
        data.put("errorMessage", result.getErrorMessage());

        if (result.isSuccess()) {
            Long personId = null;
            try {
                personId = Long.valueOf(result.getPersonId());
            } catch (NumberFormatException ignored) {
            }

            // 查询人员信息获取小区ID
            if (personId != null) {
                Person person = personService.getById(personId);
                if (person != null) {
                    communityId = person.getCommunityId();
                }
            }

            // 创建出入记录
            InOutRecord record = new InOutRecord();
            record.setPersonId(personId);
            record.setPersonName(result.getPersonName());
            record.setCommunityId(communityId);
            record.setType(type);
            record.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            record.setLocation(location);
            record.setVerified(1);
            recordService.save(record);

            data.put("recordId", record.getRecordId());
        }

        return ApiResponse.success(data);
    }

    /**
     * 人脸比对（1:1）——比较两张人脸的相似度
     */
    @PostMapping("/compare")
    public ApiResponse<FaceRecognitionResult> compare(@RequestBody Map<String, String> body) {
        String imageA = body.get("imageA");
        String imageB = body.get("imageB");
        return ApiResponse.success(faceService.compare(imageA, imageB));
    }

    /**
     * 摄像头抓拍识别并记录——专用接口
     * 接收摄像头抓拍图片，识别人脸并创建出入记录
     */
    @PostMapping("/camera-snap")
    public ApiResponse<Map<String, Object>> cameraSnap(@RequestBody Map<String, String> body) {
        String image = body.get("image");
        String cameraName = body.getOrDefault("cameraName", "摄像头");
        String location = body.getOrDefault("location", cameraName);
        String type = body.getOrDefault("type", "in");
        Long communityId = body.containsKey("communityId")
                ? Long.valueOf(body.get("communityId")) : 1L;

        FaceRecognitionResult result = faceService.identify(image);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("success", result.isSuccess());
        data.put("personName", result.getPersonName());
        data.put("confidence", result.getConfidence());
        data.put("errorMessage", result.getErrorMessage());

        // 无论识别成功与否都记录（未匹配的也记录为未验证）
        InOutRecord record = new InOutRecord();
        record.setPersonName(result.getPersonName() != null ? result.getPersonName() : "陌生人");

        if (result.isSuccess()) {
            try {
                record.setPersonId(Long.valueOf(result.getPersonId()));
            } catch (NumberFormatException ignored) {
            }
            record.setVerified(1);
        } else {
            record.setVerified(0);
        }

        if (result.isSuccess() && record.getPersonId() != null) {
            Person person = personService.getById(record.getPersonId());
            if (person != null) {
                communityId = person.getCommunityId();
            }
        }

        record.setCommunityId(communityId);
        record.setType(type);
        record.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        record.setLocation(location);
        recordService.save(record);

        data.put("recordId", record.getRecordId());
        return ApiResponse.success(data);
    }
}
