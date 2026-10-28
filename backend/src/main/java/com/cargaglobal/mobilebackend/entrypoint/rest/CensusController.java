package com.cargaglobal.mobilebackend.entrypoint.rest;

import com.cargaglobal.mobilebackend.application.usecase.CensusUseCase;
import com.cargaglobal.mobilebackend.domain.model.Census;
import com.cargaglobal.mobilebackend.domain.model.CensusOrigin;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/censuses")
public class CensusController {
    private final CensusUseCase useCase;
    public CensusController(CensusUseCase useCase) { this.useCase = useCase; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    StandardResponse<Census> capture(Authentication authentication, @Valid @RequestBody CaptureRequest request) {
        Census saved = useCase.capture(Long.parseLong(authentication.getName()), request.name(), request.lastName(),
                request.totalMembers(), request.televisionCount(), request.latitude(), request.longitude());
        return StandardResponse.of(201, "Censo guardado", saved);
    }

    @GetMapping
    StandardResponse<CensusUseCase.CensusPage> list(Authentication authentication,
            @RequestParam(defaultValue = "0") long afterId, @RequestParam(defaultValue = "500") int limit,
            @RequestParam(required = false) CensusOrigin origin) {
        return StandardResponse.of(200, "Censos consultados correctamente", useCase.list(Long.parseLong(authentication.getName()), afterId, limit, origin));
    }

    public record CaptureRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 120) String lastName,
            @NotNull @Min(1) @Max(1000) Integer totalMembers,
            @NotNull @Min(0) @Max(1000) Integer televisionCount,
            @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {}
}
