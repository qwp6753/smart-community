package com.smartcommunity.server.thirdparty.face;

public interface FaceRecognitionService {
    FaceRecognitionResult identify(String imageBase64);
    FaceRecognitionResult compare(String imageBase64, String targetImageUrl);
}
