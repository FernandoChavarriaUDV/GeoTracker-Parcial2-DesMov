package com.cargaglobal.mobilebackend.infrastructure.persistence;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="liveness_sessions")
public class LivenessSessionEntity {
    @Id @Column(length=36) String id;
    @Column(nullable=false) Long userId;
    @Column(nullable=false,length=128) String providerSessionId;
    @Column(nullable=false,length=40) String region;
    @Column(nullable=false) Instant createdAt;
    @Column(nullable=false) Instant expiresAt;
    @Column(nullable=false,length=30) String status;
    Double confidence;
    @Convert(converter=FaceComparisonConverter.class)
    @Column(name="comparison_json",columnDefinition="TEXT")
    com.cargaglobal.mobilebackend.domain.model.FaceComparison comparison;
    protected LivenessSessionEntity() {}
}
