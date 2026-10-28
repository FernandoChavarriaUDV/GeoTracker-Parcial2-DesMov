package com.cargaglobal.mobilebackend.application.dto;

public record LoginResult(boolean login, String token, UserResult user) {}
