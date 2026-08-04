package com.ia.backend.exception;

public class AlreadyVerifiedException extends RuntimeException {
    public AlreadyVerifiedException(String message) {
        super(message);
    }
}
