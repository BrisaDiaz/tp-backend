package ar.edu.utn.frc.backend.recursos.dto.error;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationError {
    private String field;
    private String message;
    private Object rejectedValue;
}