package com.cargaglobal.mobilebackend.infrastructure.config;

import com.cargaglobal.mobilebackend.application.port.PasswordService;
import com.cargaglobal.mobilebackend.domain.model.Role;
import com.cargaglobal.mobilebackend.domain.model.User;
import com.cargaglobal.mobilebackend.domain.port.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;

/** One-time, server-owner-only provisioning. No public bootstrap endpoint. */
@Component
public class AdminBootstrap implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordService passwords;
    private final boolean enabled;
    private final String email, password;
    public AdminBootstrap(UserRepository users, PasswordService passwords,
            @Value("${BOOTSTRAP_ADMIN_ENABLED:false}") boolean enabled,
            @Value("${BOOTSTRAP_ADMIN_EMAIL:}") String email,
            @Value("${BOOTSTRAP_ADMIN_PASSWORD:}") String password) {
        this.users=users; this.passwords=passwords; this.enabled=enabled; this.email=email.trim().toLowerCase(java.util.Locale.ROOT); this.password=password;
    }
    public void run(ApplicationArguments args) {
        if (!enabled) return;
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || password.length()<12 || password.getBytes(StandardCharsets.UTF_8).length>72)
            throw new IllegalStateException("Configuración de cuenta inicial inválida: correo válido y contraseña de 12 a 72 bytes requeridos");
        // Existing accounts are NEVER promoted and passwords are NEVER reset by this process.
        if (users.existsByUsername(email) || users.existsByEmail(email)) return;
        users.save(new User(null,"Administrador","Censos",email,email,passwords.encode(password),
            null,(LocalDate)null,null,null,Role.ADMINISTRADOR));
    }
}
