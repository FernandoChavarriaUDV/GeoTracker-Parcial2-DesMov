package com.cargaglobal.mobilebackend.infrastructure.persistence;

import com.cargaglobal.mobilebackend.domain.model.Census;
import com.cargaglobal.mobilebackend.domain.model.CensusOrigin;
import com.cargaglobal.mobilebackend.domain.port.CensusRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
class CensusRepositoryAdapter implements CensusRepository {
    private final SpringDataCensusRepository repository;
    CensusRepositoryAdapter(SpringDataCensusRepository repository) { this.repository = repository; }
    public Census save(Census census) { return repository.save(new CensusJpaEntity(census)).toDomain(); }
    public List<Census> findAfter(long afterId, int limit, CensusOrigin origin) {
        var pageable = PageRequest.of(0, limit);
        var entities = origin == null ? repository.findByIdGreaterThanOrderByIdAsc(afterId, pageable)
                : repository.findByOriginAndIdGreaterThanOrderByIdAsc(origin, afterId, pageable);
        return entities.stream().map(CensusJpaEntity::toDomain).toList();
    }
}
