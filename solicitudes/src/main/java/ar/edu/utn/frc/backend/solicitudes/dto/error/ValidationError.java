package ar.edu.utn.frc.backend.solicitudes.dto.error;

import lombok.Builder;


@Builder
public class ValidationError {
    private String field;
    private String message;
}