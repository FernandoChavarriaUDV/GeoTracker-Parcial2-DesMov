package com.cargaglobal.mobilebackend.application.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() { super("Usuario no encontrado"); }
}
