package com.smartcommunity.server.thirdparty.controller;

import com.smartcommunity.server.common.api.ApiResponse;
import com.smartcommunity.server.modules.access.service.InOutRecordService;
import com.smartcommunity.server.modules.property.entity.Person;
import com.smartcommunity.server.modules.property.service.PersonService;
import com.smartcommunity.server.thirdparty.face.FaceRecognitionResult;
import com.smartcommunity.server.thirdparty.face.FaceRecognitionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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

    @PostMapping("/identify")
    public ApiResponse<FaceRecognitionResult> identify(@RequestBody Map<String, String> body) {
        String imageBase64 = body.get("image");
        FaceRecognitionResult result = faceService.identify(imageBase64);

        if (result.isSuccess()) {
            var record = new com.smartcommunity.server.modules.access.entity.InOutRecord();
            record.setPersonName(result.getPersonName());
            record.setCommunityId(1L);
            record.setType("in");
            record.setTime(LocalDateTime.now().toString());
            record.setLocation("移动端拍照识别");
            record.setVerified(1);
            try {
                record.setPersonId(Long.valueOf(result.getPersonId()));
            } catch (NumberFormatException ignored) {
            }
            recordService.save(record);
        }

        return ApiResponse.success(result);
    }
}
