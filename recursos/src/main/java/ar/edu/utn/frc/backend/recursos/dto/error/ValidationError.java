package ar.edu.utn.frc.backend.recursos.dto.error;

import lombok.Builder;


@Builder
public class ValidationError {
    private String field;
    private String message;
}