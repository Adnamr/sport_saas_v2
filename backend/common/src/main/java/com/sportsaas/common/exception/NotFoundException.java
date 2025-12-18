package com.sportsaas.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception pour les ressources non trouvées.
 */
public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public NotFoundException(String entityName, Object id) {
        super(String.format("%s avec l'id '%s' non trouvé", entityName, id),
              HttpStatus.NOT_FOUND, "NOT_FOUND");
    }
}
