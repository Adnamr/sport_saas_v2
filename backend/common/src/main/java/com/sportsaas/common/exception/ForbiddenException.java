package com.sportsaas.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception pour les erreurs d'autorisation.
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    public ForbiddenException() {
        super("Accès refusé", HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}
