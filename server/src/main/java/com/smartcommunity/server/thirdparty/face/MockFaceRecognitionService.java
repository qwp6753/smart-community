package com.smartcommunity.server.thirdparty.face;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnExpression("'${app.face.tencent.secret-id:}'.isBlank()")
public class MockFaceRecognitionService implements FaceRecognitionService {

    @Override
    public FaceRecognitionResult identify(String imageBase64) {
        if (imageBase64 == null || imageBase64.isBlank()) {
            return FaceRecognitionResult.error("图片数据为空");
        }
        return FaceRecognitionResult.matched("1", "模拟居民", 0.95f);
    }

    @Override
    public FaceRecognitionResult compare(String imageBase64, String targetImageUrl) {
        if (imageBase64 == null || imageBase64.isBlank()) {
            return FaceRecognitionResult.error("图片数据为空");
        }
        return FaceRecognitionResult.matched(null, "模拟比对", 0.92f);
    }
}
