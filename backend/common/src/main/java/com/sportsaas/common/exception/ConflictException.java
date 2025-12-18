package com.sportsaas.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception pour les conflits (ex: doublon).
 */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT");
    }
}
