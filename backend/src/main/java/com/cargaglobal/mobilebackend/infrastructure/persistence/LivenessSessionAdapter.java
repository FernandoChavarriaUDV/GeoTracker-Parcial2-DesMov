package com.cargaglobal.mobilebackend.infrastructure.persistence;
import com.cargaglobal.mobilebackend.domain.model.LivenessSession;
import com.cargaglobal.mobilebackend.domain.port.LivenessSessionRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Optional;

interface SpringLivenessSessions extends JpaRepository<LivenessSessionEntity,String> {
    boolean existsByUserIdAndCreatedAtAfter(Long userId, Instant since);
}
@Repository
class LivenessSessionAdapter implements LivenessSessionRepository {
    private final SpringLivenessSessions store;
    LivenessSessionAdapter(SpringLivenessSessions store) { this.store=store; }
    public Optional<LivenessSession> findById(String id) { return store.findById(id).map(this::domain); }
    public boolean hasRecentSession(Long userId, Instant since) { return store.existsByUserIdAndCreatedAtAfter(userId,since); }
    public LivenessSession save(LivenessSession s) {
        var e = store.findById(s.id()).orElseGet(LivenessSessionEntity::new);
        e.id=s.id(); e.userId=s.userId(); e.providerSessionId=s.providerSessionId(); e.region=s.region();
        if (e.createdAt==null) e.createdAt=Instant.now();
        e.expiresAt=s.expiresAt(); e.status=s.status(); e.confidence=s.confidence();
        e.comparison=s.comparison();
        return domain(store.save(e));
    }
    private LivenessSession domain(LivenessSessionEntity e) {
        return new LivenessSession(e.id,e.userId,e.providerSessionId,e.region,e.expiresAt,e.status,e.confidence,e.comparison);
    }
}
