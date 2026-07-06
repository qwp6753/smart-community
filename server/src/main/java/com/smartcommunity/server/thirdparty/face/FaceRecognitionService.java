package com.smartcommunity.server.thirdparty.face;

public interface FaceRecognitionService {
    FaceRecognitionResult identify(String imageBase64);
    FaceRecognitionResult compare(String imageBase64, String targetImageUrl);

    /** 向人员库注册人脸（默认空实现，腾讯云服务覆写） */
    default void registerFace(String personId, String personName, String imageBase64) {
    }

    /** 从人员库删除人脸（默认空实现，腾讯云服务覆写） */
    default void removeFace(String personId) {
    }
}
