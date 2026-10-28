package com.cargaglobal.mobilebackend.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    @org.springframework.data.jpa.repository.Query("select u from UserJpaEntity u where u.avatarBase64 is not null and u.avatarBase64 <> '' order by u.id")
    java.util.List<UserJpaEntity> findFaceCandidates(org.springframework.data.domain.Pageable page);
}
