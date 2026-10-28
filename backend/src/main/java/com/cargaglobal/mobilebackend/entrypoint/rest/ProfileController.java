package com.cargaglobal.mobilebackend.entrypoint.rest;

import com.cargaglobal.mobilebackend.application.dto.UpdateProfileCommand;
import com.cargaglobal.mobilebackend.application.dto.UserResult;
import com.cargaglobal.mobilebackend.application.usecase.GetProfileUseCase;
import com.cargaglobal.mobilebackend.application.usecase.UpdateProfileUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {
    private final GetProfileUseCase getProfile;
    private final UpdateProfileUseCase updateProfile;

    public ProfileController(GetProfileUseCase getProfile, UpdateProfileUseCase updateProfile) {
        this.getProfile = getProfile;
        this.updateProfile = updateProfile;
    }

    @GetMapping
    StandardResponse<UserResult> get(Authentication authentication) {
        return StandardResponse.of(200, "Perfil consultado correctamente", getProfile.execute(userId(authentication)));
    }

    @PutMapping
    StandardResponse<UserResult> update(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        UserResult result = updateProfile.execute(new UpdateProfileCommand(
                userId(authentication), request.name(), request.lastName(), request.email(), request.gender(),
                request.birthDate(), request.phone(), request.avatarBase64()
        ));
        return StandardResponse.of(200, "Perfil actualizado", result);
    }

    private Long userId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 120) String lastName,
            @NotBlank @Email String email,
            @Size(max = 30) String gender,
            LocalDate birthDate,
            @Size(max = 30) String phone,
            @Size(max = 2000000) String avatarBase64
    ) {}
}
