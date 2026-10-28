package com.cargaglobal.mobilebackend.application.port;

public interface LivenessProvider {
    boolean configured();
    String region();
    String create(String requestId);
    Result result(String sessionId);
    Double compare(String referenceBase64, String candidateBase64);
    record Result(String status, Double confidence, String referenceBase64) {
        public Result(String status, Double confidence) { this(status,confidence,null); }
    }
}
