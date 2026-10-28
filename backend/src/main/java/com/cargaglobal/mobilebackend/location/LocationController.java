package com.cargaglobal.mobilebackend.location;

import com.cargaglobal.mobilebackend.entrypoint.rest.StandardResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {
    private final LocationRepository repository;

    public LocationController(LocationRepository repository) { this.repository = repository; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StandardResponse<Location> save(Authentication authentication, @Valid @RequestBody LocationRequest request) {
        // El dueño proviene del token, nunca de un campo enviado por la aplicación.
        long userId = Long.parseLong(authentication.getName());
        if (!Double.isFinite(request.latitude()) || !Double.isFinite(request.longitude())) {
            throw new IllegalArgumentException("Coordenadas inválidas");
        }
        Location saved = repository.save(new Location(userId, request.name(), request.description(),
                request.latitude(), request.longitude()));
        return StandardResponse.of(201, "Ubicación guardada correctamente", saved);
    }

    @GetMapping
    public StandardResponse<List<Location>> list(Authentication authentication) {
        long userId = Long.parseLong(authentication.getName());
        return StandardResponse.of(200, "Ubicaciones consultadas correctamente",
                repository.findByUserIdOrderByIdDesc(userId));
    }

    public record LocationRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 1000) String description,
            @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {}
}
