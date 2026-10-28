package com.cargaglobal.mobilebackend.infrastructure.persistence;

import com.cargaglobal.mobilebackend.domain.model.Census;
import com.cargaglobal.mobilebackend.domain.model.CensusOrigin;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "censuses")
class CensusJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String name;
    @Column(name = "last_name", nullable = false, length = 120) private String lastName;
    @Column(name = "total_members", nullable = false) private int totalMembers;
    @Column(name = "television_count", nullable = false) private int televisionCount;
    @Column(nullable = false) private double latitude;
    @Column(nullable = false) private double longitude;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private CensusOrigin origin;
    @Column(name = "censist_id") private Long censistId;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected CensusJpaEntity() {}
    CensusJpaEntity(Census census) {
        id = census.id(); name = census.name(); lastName = census.lastName();
        totalMembers = census.totalMembers(); televisionCount = census.televisionCount();
        latitude = census.latitude(); longitude = census.longitude(); origin = census.origin();
        censistId = census.censistId(); createdAt = census.createdAt();
    }
    Census toDomain() {
        return new Census(id, name, lastName, totalMembers, televisionCount,
                latitude, longitude, origin, censistId, createdAt);
    }
}
