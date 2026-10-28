package com.cargaglobal.mobilebackend.entrypoint.rest;

import com.cargaglobal.mobilebackend.application.dto.RegisterCommand;
import com.cargaglobal.mobilebackend.application.dto.UserResult;
import com.cargaglobal.mobilebackend.application.usecase.RegisterUserUseCase;
import com.cargaglobal.mobilebackend.domain.model.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {
    private final RegisterUserUseCase register;
    public AdminUserController(RegisterUserUseCase register) { this.register = register; }
    @PostMapping
    public ResponseEntity<StandardResponse<UserResult>> create(Authentication auth, @Valid @RequestBody Request request) {
        var user = request.user();
        var result = register.executeAsAdmin(Long.valueOf(auth.getName()), new RegisterCommand(
            user.name(), user.lastName(), user.username(), user.email(), user.password(), user.gender(), user.birthDate(), user.phone()
        ), request.role());
        return ResponseEntity.status(201).body(StandardResponse.of(201, "Usuario creado", result));
    }
    public record Request(@NotNull @Valid AuthController.RegisterRequest user, @NotNull Role role) {}
}
