package com.cargaglobal.mobilebackend.entrypoint.rest;

import com.cargaglobal.mobilebackend.application.dto.*;
import com.cargaglobal.mobilebackend.application.usecase.LoginUseCase;
import com.cargaglobal.mobilebackend.application.usecase.RegisterUserUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final LoginUseCase login;
    private final RegisterUserUseCase register;

    public AuthController(LoginUseCase login, RegisterUserUseCase register) {
        this.login = login;
        this.register = register;
    }

    @PostMapping("/login")
    public ResponseEntity<StandardResponse<LoginResult>> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = login.execute(new LoginCommand(request.username(), request.password()));
        return ResponseEntity.ok(StandardResponse.of(200, "Inicio de sesión exitoso", result));
    }

    @PostMapping("/register")
    public ResponseEntity<StandardResponse<UserResult>> register(@Valid @RequestBody RegisterRequest request) {
        UserResult result = register.execute(new RegisterCommand(
                request.name(), request.lastName(), request.username(), request.email(), request.password(),
                request.gender(), request.birthDate(), request.phone()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(StandardResponse.of(201, "Usuario registrado correctamente", result));
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record RegisterRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 120) String lastName,
            @NotBlank @Size(max = 180) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 72) String password,
            @NotBlank @Size(max = 30) @Pattern(regexp = "Masculino|Femenino") String gender,
            @NotNull @jakarta.validation.constraints.PastOrPresent LocalDate birthDate,
            @Size(max = 30) @Pattern(regexp = "^$|^\\+?(?=(?:\\D*\\d){7,15}\\D*$)[0-9 ()-]+$") String phone
    ) {}
}
