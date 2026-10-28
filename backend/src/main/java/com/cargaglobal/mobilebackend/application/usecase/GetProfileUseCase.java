package com.cargaglobal.mobilebackend.application.usecase;

import com.cargaglobal.mobilebackend.application.dto.UserResult;
import com.cargaglobal.mobilebackend.application.exception.UserNotFoundException;
import com.cargaglobal.mobilebackend.domain.port.UserRepository;

public class GetProfileUseCase {
    private final UserRepository users;

    public GetProfileUseCase(UserRepository users) { this.users = users; }

    public UserResult execute(Long userId) {
        return users.findById(userId).map(UserResult::from)
                .orElseThrow(UserNotFoundException::new);
    }
}
