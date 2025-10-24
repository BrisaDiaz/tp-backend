package ar.edu.utn.frc.backend.recursos.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DataConflictException extends RuntimeException {

    // Se utiliza para violaciones de unicidad o conflictos de lógica de negocio.
    public DataConflictException(String message) {
        super(message);
    }
}