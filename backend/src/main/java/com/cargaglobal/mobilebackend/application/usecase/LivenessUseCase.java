package com.cargaglobal.mobilebackend.application.usecase;
import com.cargaglobal.mobilebackend.application.port.LivenessProvider;
import com.cargaglobal.mobilebackend.application.exception.LivenessUnavailableException;
import com.cargaglobal.mobilebackend.domain.model.LivenessSession;
import com.cargaglobal.mobilebackend.domain.port.LivenessSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.util.UUID;

@Service
public class LivenessUseCase {
    private final LivenessProvider provider;
    private final LivenessSessionRepository sessions;
    private final double threshold;
    private final FaceComparisonUseCase comparison;
    public LivenessUseCase(LivenessProvider provider,LivenessSessionRepository sessions,
            @Value("${liveness.confidence-threshold:90}") double threshold, FaceComparisonUseCase comparison) {
        if (!Double.isFinite(threshold) || threshold<0 || threshold>100) throw new IllegalArgumentException("Umbral inválido");
        this.provider=provider; this.sessions=sessions; this.threshold=threshold;
        this.comparison=comparison;
    }
    public Config config() { return new Config(provider.configured(),provider.region()); }
    public synchronized View create(Long userId,boolean consent) {
        if (!consent) throw new IllegalArgumentException("Consentimiento requerido");
        if (!provider.configured()) throw new LivenessUnavailableException();
        var now=Instant.now();
        if (sessions.hasRecentSession(userId,now.minusSeconds(30)))
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Espera antes de iniciar otra prueba");
        String id=UUID.randomUUID().toString();
        var session=new LivenessSession(id,userId,provider.create(id),provider.region(),now.plusSeconds(180),"CREATED",null);
        return view(sessions.save(session));
    }
    public synchronized View result(Long userId,String id) {
        var session=sessions.findById(id).filter(s->s.userId().equals(userId)).orElseThrow(
            ()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Sesión no encontrada"));
        if (java.util.Set.of("PASSED","REJECTED","FAILED","EXPIRED").contains(session.status())) return view(session);
        String status; Double confidence=null; String reference=null;
        com.cargaglobal.mobilebackend.domain.model.FaceComparison matches=null;
        if (session.expiresAt().isBefore(Instant.now())) status="EXPIRED";
        else {
            var result=provider.result(session.providerSessionId());
            confidence=result.confidence();
            status=switch(result.status()) {
                case "SUCCEEDED" -> confidence!=null && Double.isFinite(confidence) && confidence>=threshold && confidence<=100 ? "PASSED":"REJECTED";
                case "FAILED" -> "FAILED";
                case "EXPIRED" -> "EXPIRED";
                case "CREATED","IN_PROGRESS" -> "PROCESSING";
                default -> throw new LivenessUnavailableException();
            };
            if (status.equals("PASSED")) {
                reference=result.referenceBase64();
                matches=comparison.execute(reference);
            }
        }
        var saved=sessions.save(new LivenessSession(session.id(),userId,session.providerSessionId(),session.region(),session.expiresAt(),status,confidence,matches));
        return view(saved,reference);
    }
    private View view(LivenessSession s) { return view(s,null); }
    private View view(LivenessSession s,String reference) { return new View(s.id(),s.providerSessionId(),s.region(),s.expiresAt().toString(),s.status(),s.status().equals("PASSED"),reference,s.comparison()); }
    public record Config(boolean configured,String region) {}
    public record View(String id,String sessionId,String region,String expiresAt,String status,boolean verified,
            String referenceBase64, com.cargaglobal.mobilebackend.domain.model.FaceComparison comparison) {}
}
