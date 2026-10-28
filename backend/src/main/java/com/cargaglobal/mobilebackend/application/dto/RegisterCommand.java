package com.cargaglobal.mobilebackend.application.dto;

import java.time.LocalDate;

public record RegisterCommand(
        String name,
        String lastName,
        String username,
        String email,
        String password,
        String gender,
        LocalDate birthDate,
        String phone
) {
    public RegisterCommand(String name, String lastName, String username, String email,
                           String password, String gender, LocalDate birthDate) {
        this(name, lastName, username, email, password, gender, birthDate, null);
    }
}
