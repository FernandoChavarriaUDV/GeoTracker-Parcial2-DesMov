package com.cargaglobal.mobilebackend.application.usecase;

import com.cargaglobal.mobilebackend.application.dto.RegisterCommand;
import com.cargaglobal.mobilebackend.application.dto.UserResult;
import com.cargaglobal.mobilebackend.application.exception.EmailAlreadyExistsException;
import com.cargaglobal.mobilebackend.application.exception.UsernameAlreadyExistsException;
import com.cargaglobal.mobilebackend.application.port.PasswordService;
import com.cargaglobal.mobilebackend.domain.model.User;
import com.cargaglobal.mobilebackend.domain.port.UserRepository;

public class RegisterUserUseCase {
    private final UserRepository users;
    private final PasswordService passwords;

    public RegisterUserUseCase(UserRepository users, PasswordService passwords) {
        this.users = users;
        this.passwords = passwords;
    }

    public UserResult execute(RegisterCommand command) {
        return create(command, com.cargaglobal.mobilebackend.domain.model.Role.CENSISTA);
    }

    public UserResult executeAsAdmin(Long actorId, RegisterCommand command, com.cargaglobal.mobilebackend.domain.model.Role role) {
        var actor = users.findById(actorId).orElseThrow(com.cargaglobal.mobilebackend.application.exception.UserNotFoundException::new);
        if (actor.role() != com.cargaglobal.mobilebackend.domain.model.Role.ADMINISTRADOR)
            throw new org.springframework.security.access.AccessDeniedException("Solo un administrador puede crear usuarios con rol");
        if (role == null) throw new IllegalArgumentException("Rol requerido");
        return create(command, role);
    }

    private UserResult create(RegisterCommand command, com.cargaglobal.mobilebackend.domain.model.Role role) {
        if (command.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Contraseña demasiado larga");
        String email = command.email().trim().toLowerCase();
        String username = command.username().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new EmailAlreadyExistsException();
        if (users.existsByUsername(username)) throw new UsernameAlreadyExistsException();
        User saved = users.save(new User(
                null,
                command.name().trim(),
                command.lastName().trim(),
                username,
                email,
                passwords.encode(command.password()),
                command.gender().trim(),
                command.birthDate(),
                command.phone() == null || command.phone().isBlank() ? null : command.phone().trim(),
                null, role
        ));
        return UserResult.from(saved);
    }
}
