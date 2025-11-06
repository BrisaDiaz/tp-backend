package ar.edu.utn.frc.backend.logistica.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO para representar un tramo tentativo dentro de una ruta")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TramoTentativoDto {
    
    @Schema(description = "Número de orden del tramo en la ruta", example = "1", required = true)
    @NotNull(message = "El número de orden es obligatorio.")
    @Min(value = 1, message = "El número de orden debe ser positivo.")
    Integer nroOrden;

    @Schema(description = "Depósito de origen del tramo", required = true)
    @NotNull(message = "El depósito de origen es obligatorio.")
    @Valid
    DepositoDto origen;

    @Schema(description = "Depósito de destino del tramo", required = true)
    @NotNull(message = "El depósito de destino es obligatorio.")
    @Valid
    DepositoDto destino;

    @Schema(description = "Costo estimado del tramo en pesos", example = "1500.50", required = true)
    @NotNull(message = "El costo estimado es obligatorio.")
    @DecimalMin(value = "0.00", message = "El costo estimado no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "El costo estimado debe tener máximo 8 enteros y 2 decimales.")
    BigDecimal costoEstimado;

    @Schema(description = "Tiempo estimado del tramo en segundos", example = "3600", required = true)
    @NotNull(message = "El tiempo estimado es obligatorio.")
    @Min(value = 1, message = "El tiempo estimado en segundos debe ser positivo.")
    Long tiempoEstimadoSegundos;

    @Schema(description = "Distancia del tramo en kilómetros", example = "120.5", required = true)
    @NotNull(message = "La distancia en kilómetros es obligatoria.")
    @DecimalMin(value = "0.1", message = "La distancia debe ser un valor positivo.")
    Float distanciaKilometros;
}