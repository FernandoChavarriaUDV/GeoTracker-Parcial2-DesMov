package com.cargaglobal.mobilebackend.domain.model;
import java.time.Instant;
public record LivenessSession(String id, Long userId, String providerSessionId, String region,
        Instant expiresAt, String status, Double confidence, FaceComparison comparison) {
    public LivenessSession(String id, Long userId, String providerSessionId, String region,
            Instant expiresAt, String status, Double confidence) {
        this(id,userId,providerSessionId,region,expiresAt,status,confidence,null);
    }
}
