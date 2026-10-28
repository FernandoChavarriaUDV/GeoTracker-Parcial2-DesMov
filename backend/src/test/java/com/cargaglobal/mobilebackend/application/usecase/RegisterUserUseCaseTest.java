package com.cargaglobal.mobilebackend.application.usecase;

import com.cargaglobal.mobilebackend.application.dto.RegisterCommand;
import com.cargaglobal.mobilebackend.application.port.PasswordService;
import com.cargaglobal.mobilebackend.domain.model.User;
import com.cargaglobal.mobilebackend.domain.port.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterUserUseCaseTest {
    @Test
    void registersCompleteUserAndHashesPassword() {
        UserRepository users = mock(UserRepository.class);
        PasswordService passwords = mock(PasswordService.class);
        when(passwords.encode("123456")).thenReturn("hash");
        when(users.save(any())).thenAnswer(invocation -> {
            User value = invocation.getArgument(0);
            return new User(10L, value.name(), value.lastName(), value.username(), value.email(),
                    value.passwordHash(), value.gender(), value.birthDate(), value.phone(), value.avatarBase64());
        });

        var result = new RegisterUserUseCase(users, passwords).execute(new RegisterCommand(
                "Ana", "López", "ANA", "ANA@example.com", "123456", "Femenino",
                LocalDate.of(2000, 1, 10), " +502 5555-1234 "
        ));

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.username()).isEqualTo("ana");
        assertThat(result.lastName()).isEqualTo("López");
        assertThat(result.phone()).isEqualTo("+502 5555-1234");
        verify(passwords).encode("123456");
    }
}
