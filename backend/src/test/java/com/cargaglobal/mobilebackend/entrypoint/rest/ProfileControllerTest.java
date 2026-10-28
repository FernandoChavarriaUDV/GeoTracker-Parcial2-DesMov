package com.cargaglobal.mobilebackend.entrypoint.rest;

import com.cargaglobal.mobilebackend.application.dto.UserResult;
import com.cargaglobal.mobilebackend.application.usecase.GetProfileUseCase;
import com.cargaglobal.mobilebackend.application.usecase.UpdateProfileUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProfileControllerTest {
    @Test
    void getsAndUpdatesUserFromAuthenticatedIdentity() {
        GetProfileUseCase getProfile = mock(GetProfileUseCase.class);
        UpdateProfileUseCase updateProfile = mock(UpdateProfileUseCase.class);
        Authentication authentication = mock(Authentication.class);
        UserResult user = new UserResult(4L, "Ana", "López", "ana", "ana@example.com",
                "Femenino", LocalDate.of(2000, 1, 10), "5555-5555", null);
        when(authentication.getName()).thenReturn("4");
        when(getProfile.execute(4L)).thenReturn(user);
        when(updateProfile.execute(any())).thenReturn(user);
        ProfileController controller = new ProfileController(getProfile, updateProfile);

        assertThat(controller.get(authentication).body().id()).isEqualTo(4L);
        assertThat(controller.update(authentication, new ProfileController.UpdateProfileRequest(
                "Ana", "López", "ana@example.com", "Femenino",
                LocalDate.of(2000, 1, 10), "5555-5555", null
        )).message()).isEqualTo("Perfil actualizado");
    }
}
