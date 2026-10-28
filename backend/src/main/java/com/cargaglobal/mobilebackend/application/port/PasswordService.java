package com.cargaglobal.mobilebackend.application.port;

public interface PasswordService {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}

