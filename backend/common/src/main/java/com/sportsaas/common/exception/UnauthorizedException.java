package com.sportsaas.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception pour les erreurs d'authentification.
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    public UnauthorizedException() {
        super("Authentification requise", HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}
