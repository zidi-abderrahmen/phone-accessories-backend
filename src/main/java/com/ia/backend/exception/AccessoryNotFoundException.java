package com.ia.backend.exception;

public class AccessoryNotFoundException extends RuntimeException {
    public AccessoryNotFoundException(String message) {
        super(message);
    }
}
