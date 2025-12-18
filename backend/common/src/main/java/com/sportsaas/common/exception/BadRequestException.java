package com.sportsaas.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception pour les requetes invalides (ex: validation metier).
 */
public class BadRequestException extends BusinessException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }
}
