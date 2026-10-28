package com.cargaglobal.mobilebackend.domain.model;

import java.time.LocalDate;

public record User(
        Long id,
        String name,
        String lastName,
        String username,
        String email,
        String passwordHash,
        String gender,
        LocalDate birthDate,
        String phone,
        String avatarBase64,
        Role role
) {
    public User(Long id, String name, String lastName, String username, String email,
                String passwordHash, String gender, LocalDate birthDate, String phone, String avatarBase64) {
        this(id, name, lastName, username, email, passwordHash, gender, birthDate, phone, avatarBase64, Role.CENSISTA);
    }
}
