package com.cargaglobal.mobilebackend.application.dto;

import java.time.LocalDate;

public record UpdateProfileCommand(
        Long userId,
        String name,
        String lastName,
        String email,
        String gender,
        LocalDate birthDate,
        String phone,
        String avatarBase64
) {}
