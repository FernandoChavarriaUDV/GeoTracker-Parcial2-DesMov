package com.cargaglobal.mobilebackend.domain.model;

import java.time.Instant;

public record Census(Long id, String name, String lastName, int totalMembers, int televisionCount,
                     double latitude, double longitude, CensusOrigin origin, Long censistId, Instant createdAt) {}
