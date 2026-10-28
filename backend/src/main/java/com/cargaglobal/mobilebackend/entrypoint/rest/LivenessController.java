package com.cargaglobal.mobilebackend.entrypoint.rest;
import com.cargaglobal.mobilebackend.application.usecase.LivenessUseCase;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/liveness")
public class LivenessController {
    private final LivenessUseCase service;
    public LivenessController(LivenessUseCase service) { this.service=service; }
    @GetMapping("/config") public StandardResponse<LivenessUseCase.Config> config() { return StandardResponse.of(200,"Configuración de prueba facial consultada",service.config()); }
    @PostMapping("/sessions") public StandardResponse<LivenessUseCase.View> create(Authentication auth,@RequestBody Consent request) {
        return StandardResponse.of(200,"Sesión de prueba facial creada",service.create(Long.valueOf(auth.getName()),request.consent()));
    }
    @GetMapping("/sessions/{id}") public StandardResponse<LivenessUseCase.View> result(Authentication auth,@PathVariable String id) {
        return StandardResponse.of(200,"Resultado de prueba facial consultado",service.result(Long.valueOf(auth.getName()),id));
    }
    public record Consent(boolean consent) {}
}
