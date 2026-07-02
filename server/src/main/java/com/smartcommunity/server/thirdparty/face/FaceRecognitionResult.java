package com.smartcommunity.server.thirdparty.face;

public class FaceRecognitionResult {

    private boolean success;
    private String personId;
    private String personName;
    private Float confidence;
    private String errorMessage;

    public FaceRecognitionResult() {
    }

    public static FaceRecognitionResult matched(String personId, String personName, Float confidence) {
        FaceRecognitionResult result = new FaceRecognitionResult();
        result.setSuccess(true);
        result.setPersonId(personId);
        result.setPersonName(personName);
        result.setConfidence(confidence);
        return result;
    }

    public static FaceRecognitionResult unmatched() {
        FaceRecognitionResult result = new FaceRecognitionResult();
        result.setSuccess(false);
        result.setErrorMessage("未匹配到居民");
        return result;
    }

    public static FaceRecognitionResult error(String message) {
        FaceRecognitionResult result = new FaceRecognitionResult();
        result.setSuccess(false);
        result.setErrorMessage(message);
        return result;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getPersonId() { return personId; }
    public void setPersonId(String personId) { this.personId = personId; }
    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }
    public Float getConfidence() { return confidence; }
    public void setConfidence(Float confidence) { this.confidence = confidence; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
