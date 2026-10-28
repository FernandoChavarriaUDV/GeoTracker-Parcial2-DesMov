package com.cargaglobal.mobilebackend.application.usecase;

import com.cargaglobal.mobilebackend.application.dto.LoginCommand;
import com.cargaglobal.mobilebackend.application.exception.InvalidCredentialsException;
import com.cargaglobal.mobilebackend.application.port.PasswordService;
import com.cargaglobal.mobilebackend.application.port.TokenService;
import com.cargaglobal.mobilebackend.domain.model.User;
import com.cargaglobal.mobilebackend.domain.port.UserRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginUseCaseTest {
    private final UserRepository users = mock(UserRepository.class);
    private final PasswordService passwords = mock(PasswordService.class);
    private final TokenService tokens = mock(TokenService.class);
    private final LoginUseCase useCase = new LoginUseCase(users, passwords, tokens);

    @Test
    void logsInWithValidCredentials() {
        User user = user();
        when(users.findByUsername("ana")).thenReturn(Optional.of(user));
        when(passwords.matches("password123", "hash")).thenReturn(true);
        when(tokens.create(user)).thenReturn("jwt-token");
        var result = useCase.execute(new LoginCommand("ANA", "password123"));
        assertThat(result.login()).isTrue();
        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.user().id()).isEqualTo(1L);
    }

    @Test
    void rejectsInvalidPassword() {
        when(users.findByUsername("ana")).thenReturn(Optional.of(user()));
        when(passwords.matches(anyString(), anyString())).thenReturn(false);
        assertThatThrownBy(() -> useCase.execute(new LoginCommand("ana", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    private User user() {
        return new User(1L, "Ana", "López", "ana", "ana@example.com", "hash",
                "Femenino", null, null, null);
    }
}
