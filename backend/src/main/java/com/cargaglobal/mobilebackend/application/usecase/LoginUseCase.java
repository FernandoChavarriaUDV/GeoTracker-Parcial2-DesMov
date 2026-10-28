package com.cargaglobal.mobilebackend.application.usecase;

import com.cargaglobal.mobilebackend.application.dto.LoginCommand;
import com.cargaglobal.mobilebackend.application.dto.LoginResult;
import com.cargaglobal.mobilebackend.application.dto.UserResult;
import com.cargaglobal.mobilebackend.application.exception.InvalidCredentialsException;
import com.cargaglobal.mobilebackend.application.port.PasswordService;
import com.cargaglobal.mobilebackend.application.port.TokenService;
import com.cargaglobal.mobilebackend.domain.model.User;
import com.cargaglobal.mobilebackend.domain.port.UserRepository;

public class LoginUseCase {
    private final UserRepository users;
    private final PasswordService passwords;
    private final TokenService tokens;

    public LoginUseCase(UserRepository users, PasswordService passwords, TokenService tokens) {
        this.users = users;
        this.passwords = passwords;
        this.tokens = tokens;
    }

    public LoginResult execute(LoginCommand command) {
        User user = users.findByUsername(command.username().trim().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwords.matches(command.password(), user.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        return new LoginResult(true, tokens.create(user), UserResult.from(user));
    }
}
