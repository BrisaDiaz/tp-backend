package ar.edu.utn.frc.backend.logistica.dto;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TramoTentativoDto {
    
    @NotNull(message = "El número de orden es obligatorio.")
    @Min(value = 1, message = "El número de orden debe ser positivo.")
    Integer nroOrden;

    @NotNull(message = "El depósito de origen es obligatorio.")
    @Valid
    DepositoDto origen;

    @NotNull(message = "El depósito de destino es obligatorio.")
    @Valid
    DepositoDto destino;

    @NotNull(message = "El costo estimado es obligatorio.")
    @DecimalMin(value = "0.00", message = "El costo estimado no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "El costo estimado debe tener máximo 8 enteros y 2 decimales.")
    BigDecimal costoEstimado;

    @NotNull(message = "El tiempo estimado es obligatorio.")
    @Min(value = 1, message = "El tiempo estimado en segundos debe ser positivo.")
    Long tiempoEstimadoSegundos;

    @NotNull(message = "La distancia en kilómetros es obligatoria.")
    @DecimalMin(value = "0.1", message = "La distancia debe ser un valor positivo.")
    Float distanciaKilometros;
}
