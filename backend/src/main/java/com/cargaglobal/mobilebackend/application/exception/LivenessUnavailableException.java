package com.cargaglobal.mobilebackend.application.exception;

public class LivenessUnavailableException extends RuntimeException {
    public LivenessUnavailableException() { super("Prueba de vida pendiente de configuración AWS o servicio no disponible"); }
}
