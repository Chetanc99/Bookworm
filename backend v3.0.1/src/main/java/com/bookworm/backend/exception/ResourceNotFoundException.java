package com.bookworm.backend.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String entity, String field, Object value) {
        super(entity + " not found with " + field + " = '" + value + "'");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
