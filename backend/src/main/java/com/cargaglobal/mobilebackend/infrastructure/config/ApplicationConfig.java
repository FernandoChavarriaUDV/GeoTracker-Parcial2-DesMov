package com.cargaglobal.mobilebackend.infrastructure.config;

import com.cargaglobal.mobilebackend.application.port.PasswordService;
import com.cargaglobal.mobilebackend.application.port.TokenService;
import com.cargaglobal.mobilebackend.application.usecase.GetProfileUseCase;
import com.cargaglobal.mobilebackend.application.usecase.LoginUseCase;
import com.cargaglobal.mobilebackend.application.usecase.RegisterUserUseCase;
import com.cargaglobal.mobilebackend.application.usecase.UpdateProfileUseCase;
import com.cargaglobal.mobilebackend.domain.port.UserRepository;
import com.cargaglobal.mobilebackend.domain.port.CensusRepository;
import com.cargaglobal.mobilebackend.application.usecase.CensusUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class ApplicationConfig {
    @Bean CensusUseCase censusUseCase(CensusRepository censuses, UserRepository users) {
        return new CensusUseCase(censuses, users);
    }
    @Bean
    PasswordService passwordService() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return new PasswordService() {
            public String encode(String rawPassword) { return encoder.encode(rawPassword); }
            public boolean matches(String rawPassword, String encodedPassword) { return encoder.matches(rawPassword, encodedPassword); }
        };
    }

    @Bean LoginUseCase loginUseCase(UserRepository users, PasswordService passwords, TokenService tokens) {
        return new LoginUseCase(users, passwords, tokens);
    }

    @Bean RegisterUserUseCase registerUserUseCase(UserRepository users, PasswordService passwords) {
        return new RegisterUserUseCase(users, passwords);
    }

    @Bean GetProfileUseCase getProfileUseCase(UserRepository users) {
        return new GetProfileUseCase(users);
    }

    @Bean UpdateProfileUseCase updateProfileUseCase(UserRepository users) {
        return new UpdateProfileUseCase(users);
    }
}
