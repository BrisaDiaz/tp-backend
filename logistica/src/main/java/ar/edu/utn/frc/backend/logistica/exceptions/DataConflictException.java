package ar.edu.utn.frc.backend.logistica.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DataConflictException extends RuntimeException {
    public DataConflictException(String message) {
        super(message);
    }
    
    public DataConflictException(String resourceName, String identifier) {
        super(String.format("Conflicto con %s: %s ya existe", resourceName, identifier));
    }
    
    public DataConflictException(String resourceName, String identifier, String operation) {
        super(String.format("No se puede %s el %s '%s' debido a dependencias existentes", 
                          operation, resourceName, identifier));
    }
}