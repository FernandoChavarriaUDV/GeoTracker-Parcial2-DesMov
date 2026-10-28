package com.cargaglobal.mobilebackend.application.usecase;

import com.cargaglobal.mobilebackend.application.dto.UpdateProfileCommand;
import com.cargaglobal.mobilebackend.domain.model.User;
import com.cargaglobal.mobilebackend.domain.port.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProfileUseCaseTest {
    @Test
    void getsAndUpdatesAuthenticatedProfile() {
        UserRepository users = mock(UserRepository.class);
        User current = new User(1L, "Ana", "López", "ana", "ana@example.com", "hash",
                "Femenino", LocalDate.of(2000, 1, 10), null, null);
        when(users.findById(1L)).thenReturn(Optional.of(current));
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(new GetProfileUseCase(users).execute(1L).username()).isEqualTo("ana");

        var updated = new UpdateProfileUseCase(users).execute(new UpdateProfileCommand(
                1L, "Ana", "López", "ana@example.com", "Femenino",
                LocalDate.of(2000, 1, 10), "5555-5555", "base64"
        ));
        assertThat(updated.phone()).isEqualTo("5555-5555");
        assertThat(updated.avatarBase64()).isEqualTo("base64");
    }
}
