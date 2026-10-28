package com.cargaglobal.mobilebackend.infrastructure.config;
import com.cargaglobal.mobilebackend.application.port.PasswordService;
import com.cargaglobal.mobilebackend.domain.port.UserRepository;
import com.cargaglobal.mobilebackend.domain.model.User;
import com.cargaglobal.mobilebackend.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class AdminBootstrapTest {
    final UserRepository users=mock(UserRepository.class);
    final PasswordService passwords=mock(PasswordService.class);
    @Test void disabledDoesNotCreateAccount() {
        new AdminBootstrap(users,passwords,false,"","").run(null);
        verifyNoInteractions(users,passwords);
    }
    @Test void createsHashedAdminOnlyWhenAbsent() {
        when(passwords.encode("temporary-password-123")).thenReturn("hash");
        new AdminBootstrap(users,passwords,true,"admin@example.com","temporary-password-123").run(null);
        var captor=ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        assertThat(captor.getValue().role()).isEqualTo(Role.ADMINISTRADOR);
        assertThat(captor.getValue().passwordHash()).isEqualTo("hash");
    }
    @Test void neverResetsOrPromotesExistingUsers() {
        when(users.existsByUsername("admin@example.com")).thenReturn(true);
        new AdminBootstrap(users,passwords,true,"admin@example.com","temporary-password-123").run(null);
        verify(users,never()).save(any()); verifyNoInteractions(passwords);
    }
    @Test void refusesWeakBootstrapPassword() {
        assertThatThrownBy(()->new AdminBootstrap(users,passwords,true,"admin@example.com","123456").run(null))
            .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(users,passwords);
    }
}
