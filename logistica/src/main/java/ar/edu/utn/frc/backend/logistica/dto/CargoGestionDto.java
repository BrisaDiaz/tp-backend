package ar.edu.utn.frc.backend.logistica.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class CargoGestionDto {

    private Integer id;

    @NotNull(message = "El costo por tramo es obligatorio.")
    @DecimalMin(value = "0.00", message = "El costo por tramo no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "El costo por tramo debe tener máximo 8 enteros y 2 decimales.")
    private BigDecimal costoPorTramo;
}