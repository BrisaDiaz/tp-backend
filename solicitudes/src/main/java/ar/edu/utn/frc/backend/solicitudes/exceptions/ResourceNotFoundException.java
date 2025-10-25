package ar.edu.utn.frc.backend.solicitudes.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    // Constructor para buscar por ID
    public ResourceNotFoundException(String resourceName, Integer id) {
        super(String.format("%s con ID %d no encontrado.", resourceName, id));
    }
    // Constructor para buscar por descripción u otro campo
    public ResourceNotFoundException(String resourceName, String decripcion) {
        super(String.format("%s %s no encontrado.", resourceName, decripcion));
    }

    // Constructor para mensajes específicos
    public ResourceNotFoundException(String message) {
        super(message);
    }
}