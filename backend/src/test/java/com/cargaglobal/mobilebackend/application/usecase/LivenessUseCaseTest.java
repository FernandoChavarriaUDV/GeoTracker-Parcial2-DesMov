package com.cargaglobal.mobilebackend.application.usecase;
import com.cargaglobal.mobilebackend.application.port.LivenessProvider;
import com.cargaglobal.mobilebackend.domain.port.LivenessSessionRepository;
import com.cargaglobal.mobilebackend.domain.model.LivenessSession;
import com.cargaglobal.mobilebackend.application.exception.LivenessUnavailableException;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class LivenessUseCaseTest {
    final LivenessProvider provider=mock(LivenessProvider.class);
    final LivenessSessionRepository sessions=mock(LivenessSessionRepository.class);
    final FaceComparisonUseCase comparison=mock(FaceComparisonUseCase.class);
    final LivenessUseCase service=new LivenessUseCase(provider,sessions,90,comparison);
    LivenessSession pending() { return new LivenessSession("id",1L,"aws-id","us-east-1",Instant.now().plusSeconds(120),"CREATED",null); }
    @Test void disabledFailsClosed() {
        assertThat(service.config().configured()).isFalse();
        assertThatThrownBy(()->service.create(1L,true)).isInstanceOf(LivenessUnavailableException.class);
        verify(provider,never()).create(any());
    }
    @Test void consentRequired() { assertThatThrownBy(()->service.create(1L,false)).isInstanceOf(IllegalArgumentException.class); }
    @Test void otherUsersCannotReadSessions() {
        when(sessions.findById("id")).thenReturn(Optional.of(pending()));
        assertThatThrownBy(()->service.result(2L,"id")).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        verify(provider,never()).result(any());
    }
    @Test void serverDecidesPassFromConfidence() {
        when(sessions.findById("id")).thenReturn(Optional.of(pending()));
        when(sessions.save(any())).thenAnswer(a->a.getArgument(0));
        when(provider.result("aws-id")).thenReturn(new LivenessProvider.Result("SUCCEEDED",98.0));
        assertThat(service.result(1L,"id").verified()).isTrue();
        when(provider.result("aws-id")).thenReturn(new LivenessProvider.Result("SUCCEEDED",60.0));
        assertThat(service.result(1L,"id").status()).isEqualTo("REJECTED");
        when(provider.result("aws-id")).thenReturn(new LivenessProvider.Result("SUCCEEDED",null));
        assertThat(service.result(1L,"id").verified()).isFalse();
    }
    @Test void expiredSessionDoesNotContactAWS() {
        var s=pending();
        when(sessions.findById("id")).thenReturn(Optional.of(new LivenessSession(s.id(),1L,s.providerSessionId(),s.region(),Instant.now().minusSeconds(1),"CREATED",null)));
        when(sessions.save(any())).thenAnswer(a->a.getArgument(0));
        assertThat(service.result(1L,"id").status()).isEqualTo("EXPIRED");
        verify(provider,never()).result(any());
    }
    @Test void cooldownPreventsRepeatedCalls() {
        when(provider.configured()).thenReturn(true);
        when(sessions.hasRecentSession(eq(1L),any())).thenReturn(true);
        assertThatThrownBy(()->service.create(1L,true)).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        verify(provider,never()).create(any());
    }
    @Test void rejectedLivenessNeverSearchesGallery() {
        when(sessions.findById("id")).thenReturn(Optional.of(pending()));
        when(sessions.save(any())).thenAnswer(a->a.getArgument(0));
        when(provider.result("aws-id")).thenReturn(new LivenessProvider.Result("SUCCEEDED",40.0,"selfie"));
        var result=service.result(1L,"id");
        assertThat(result.referenceBase64()).isNull();
        verifyNoInteractions(comparison);
    }
    @Test void completedSessionUsesPersistedSummaryWithoutCallingAwsAgain() {
        var p=pending();
        var summary=new com.cargaglobal.mobilebackend.domain.model.FaceComparison("COMPLETE",1,0,java.util.List.of());
        when(sessions.findById("id")).thenReturn(Optional.of(new LivenessSession(p.id(),1L,p.providerSessionId(),p.region(),p.expiresAt(),"PASSED",99.0,summary)));
        var result=service.result(1L,"id");
        assertThat(result.comparison()).isEqualTo(summary);
        assertThat(result.referenceBase64()).isNull();
        verifyNoInteractions(provider,comparison);
    }
}
