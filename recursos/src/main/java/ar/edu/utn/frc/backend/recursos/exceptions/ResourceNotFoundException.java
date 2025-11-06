package ar.edu.utn.frc.backend.recursos.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(String.format("%s no encontrado con id: %s", resourceName, identifier));
    }
    
    public ResourceNotFoundException(String resourceName, String field, Object value) {
        super(String.format("%s no encontrado con %s: %s", resourceName, field, value));
    }
}