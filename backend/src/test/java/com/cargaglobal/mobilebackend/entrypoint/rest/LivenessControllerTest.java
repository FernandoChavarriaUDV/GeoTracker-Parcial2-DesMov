package com.cargaglobal.mobilebackend.entrypoint.rest;

import com.cargaglobal.mobilebackend.application.usecase.LivenessUseCase;
import com.cargaglobal.mobilebackend.domain.model.FaceComparison;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LivenessControllerTest {
    final LivenessUseCase service=mock(LivenessUseCase.class);
    final LivenessController controller=new LivenessController(service);
    final UsernamePasswordAuthenticationToken auth=new UsernamePasswordAuthenticationToken("1",null);
    @Test void creationKeepsExistingCodeAndPayload() {
        var view=new LivenessUseCase.View("local-id","aws-id","us-east-1","expiry","CREATED",false,null,null);
        when(service.create(1L,true)).thenReturn(view);
        var response=controller.create(auth,new LivenessController.Consent(true));
        assertThat(response.httpCode()).isEqualTo(200);
        assertThat(response.message()).isEqualTo("Sesión de prueba facial creada");
        assertThat(response.timestamp()).isNotNull();
        assertThat(response.body()).isSameAs(view);
        verify(service).create(1L,true);
    }
    @Test void queriedResultDoesNotClaimARegisteredIdentityMatched() {
        var view=new LivenessUseCase.View("local-id","aws-id","us-east-1","expiry","PASSED",true,null,
            new FaceComparison("COMPLETE",2,0,List.of()));
        when(service.result(1L,"local-id")).thenReturn(view);
        var response=controller.result(auth,"local-id");
        assertThat(response.httpCode()).isEqualTo(200);
        assertThat(response.message()).isEqualTo("Resultado de prueba facial consultado");
        assertThat(response.body()).isSameAs(view);
        assertThat(response.body().comparison().matches()).isEmpty();
    }
}
