package com.education.stelar.kernel.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(String.format("%s con id '%s' no encontrado", resource, id));
    }
}
