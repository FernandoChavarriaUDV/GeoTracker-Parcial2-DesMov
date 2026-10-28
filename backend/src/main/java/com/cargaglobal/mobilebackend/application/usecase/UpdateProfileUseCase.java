package com.cargaglobal.mobilebackend.application.usecase;

import com.cargaglobal.mobilebackend.application.dto.UpdateProfileCommand;
import com.cargaglobal.mobilebackend.application.dto.UserResult;
import com.cargaglobal.mobilebackend.application.exception.UserNotFoundException;
import com.cargaglobal.mobilebackend.domain.model.User;
import com.cargaglobal.mobilebackend.domain.port.UserRepository;

public class UpdateProfileUseCase {
    private final UserRepository users;

    public UpdateProfileUseCase(UserRepository users) { this.users = users; }

    public UserResult execute(UpdateProfileCommand command) {
        User current = users.findById(command.userId()).orElseThrow(UserNotFoundException::new);
        User updated = new User(
                current.id(), command.name().trim(), command.lastName().trim(), current.username(),
                command.email().trim().toLowerCase(), current.passwordHash(), command.gender(),
                command.birthDate(), command.phone(), command.avatarBase64(), current.role()
        );
        return UserResult.from(users.save(updated));
    }
}
