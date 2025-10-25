package ar.edu.utn.frc.backend.logistica.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando un recurso externo (otro microservicio)
 * Esto resulta en un HTTP 503 (SERVICE_UNAVAILABLE) o 500.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Opcionalmente se puede usar 503
public class RecursoNoDisponibleException extends RuntimeException {

    public RecursoNoDisponibleException(String message) {
        super(message);
    }

    public RecursoNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}