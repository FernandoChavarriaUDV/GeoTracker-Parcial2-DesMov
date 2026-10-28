package com.cargaglobal.mobilebackend.domain.port;
import com.cargaglobal.mobilebackend.domain.model.LivenessSession;
import java.time.Instant;
import java.util.Optional;
public interface LivenessSessionRepository {
    Optional<LivenessSession> findById(String id);
    boolean hasRecentSession(Long userId, Instant since);
    LivenessSession save(LivenessSession session);
}
