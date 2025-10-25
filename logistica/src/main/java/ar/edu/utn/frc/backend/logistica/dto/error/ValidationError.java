package ar.edu.utn.frc.backend.logistica.dto.error;

import lombok.Builder;


@Builder
public class ValidationError {
    private String field;
    private String message;
}