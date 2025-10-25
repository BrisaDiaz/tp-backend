package ar.edu.utn.frc.backend.logistica.dto.error;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error; // Código de error HTTP (ej: Not Found, Conflict)
    private String message; // Mensaje general del error
    private String path; // Ruta del endpoint que falló
    private List<ValidationError> details; // Lista de errores de validación
}