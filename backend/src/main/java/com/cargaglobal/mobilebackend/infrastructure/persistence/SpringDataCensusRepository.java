package com.cargaglobal.mobilebackend.infrastructure.persistence;

import com.cargaglobal.mobilebackend.domain.model.CensusOrigin;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataCensusRepository extends JpaRepository<CensusJpaEntity, Long> {
    List<CensusJpaEntity> findByIdGreaterThanOrderByIdAsc(long afterId, Pageable pageable);
    List<CensusJpaEntity> findByOriginAndIdGreaterThanOrderByIdAsc(CensusOrigin origin, long afterId, Pageable pageable);
}
