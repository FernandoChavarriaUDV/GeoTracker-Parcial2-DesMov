package com.cargaglobal.mobilebackend.application.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() { super("Credenciales incorrectas"); }
}

