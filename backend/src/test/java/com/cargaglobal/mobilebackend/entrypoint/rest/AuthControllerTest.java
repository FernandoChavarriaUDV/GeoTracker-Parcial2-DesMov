package com.cargaglobal.mobilebackend.entrypoint.rest;

import com.cargaglobal.mobilebackend.application.dto.LoginResult;
import com.cargaglobal.mobilebackend.application.dto.UserResult;
import com.cargaglobal.mobilebackend.application.usecase.LoginUseCase;
import com.cargaglobal.mobilebackend.application.usecase.RegisterUserUseCase;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    @Test
    void returnsExpectedLoginAndRegistrationResponses() {
        LoginUseCase login = mock(LoginUseCase.class);
        RegisterUserUseCase register = mock(RegisterUserUseCase.class);
        UserResult user = new UserResult(1L, "Ana", "López", "ana", "ana@example.com",
                "Femenino", LocalDate.of(2000, 1, 10), null, null);
        when(login.execute(any())).thenReturn(new LoginResult(true, "jwt", user));
        when(register.execute(any())).thenReturn(user);
        AuthController controller = new AuthController(login, register);

        var loginResponse = controller.login(new AuthController.LoginRequest("ana", "123456"));
        var registerResponse = controller.register(new AuthController.RegisterRequest(
                "Ana", "López", "ana", "ana@example.com", "123456", "Femenino",
                LocalDate.of(2000, 1, 10), "+502 5555-1234"
        ));

        assertThat(loginResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(loginResponse.getBody().message()).isEqualTo("Inicio de sesión exitoso");
        assertThat(loginResponse.getBody().httpCode()).isEqualTo(200);
        assertThat(loginResponse.getBody().timestamp()).isNotNull();
        assertThat(loginResponse.getBody().body().token()).isEqualTo("jwt");
        assertThat(registerResponse.getStatusCode().value()).isEqualTo(201);
        assertThat(registerResponse.getBody().message()).isEqualTo("Usuario registrado correctamente");
        verify(register).execute(argThat(command -> "+502 5555-1234".equals(command.phone())));
    }
}
