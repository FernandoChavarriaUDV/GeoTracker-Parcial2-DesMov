package com.cargaglobal.mobilebackend.application.dto;

import com.cargaglobal.mobilebackend.domain.model.User;
import com.cargaglobal.mobilebackend.domain.model.Role;
import java.time.LocalDate;

public record UserResult(
        Long id,
        String name,
        String lastName,
        String username,
        String email,
        String gender,
        LocalDate birthDate,
        String phone,
        String avatarBase64,
        Role role
) {
    public UserResult(Long id, String name, String lastName, String username, String email,
                      String gender, LocalDate birthDate, String phone, String avatarBase64) {
        this(id, name, lastName, username, email, gender, birthDate, phone, avatarBase64, Role.CENSISTA);
    }
    public static UserResult from(User user) {
        return new UserResult(
                user.id(), user.name(), user.lastName(), user.username(), user.email(),
                user.gender(), user.birthDate(), user.phone(), user.avatarBase64(), user.role()
        );
    }
}
