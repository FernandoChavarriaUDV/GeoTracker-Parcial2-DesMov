package com.cargaglobal.mobilebackend.application.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException() { super("El nombre de usuario ya está registrado"); }
}
