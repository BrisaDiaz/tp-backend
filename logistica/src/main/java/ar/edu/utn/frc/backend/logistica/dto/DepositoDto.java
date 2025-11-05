package ar.edu.utn.frc.backend.logistica.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class DepositoDto {

    private Integer id;

    @NotBlank(message = "El nombre del depósito es obligatorio.")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres.")
    private String nombre;

    @NotBlank(message = "La dirección del depósito es obligatoria.")
    @Size(max = 100, message = "La dirección no puede exceder los 100 caracteres.")
    private String direccion;

    @NotNull(message = "El precio por día (tarifa de estadía) es obligatorio.")
    @DecimalMin(value = "0.00", message = "El precio por día no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "El precio por día debe tener máximo 8 enteros y 2 decimales.")
    private BigDecimal precioPorDia;

    @NotNull(message = "La latitud es obligatoria.")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90.")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90.")
    private Float latitud;

    @NotNull(message = "La Longitud es obligatoria.")
    @DecimalMin(value = "-180.0", message = "La Longitud debe estar entre -180 y 180.")
    @DecimalMax(value = "180.0", message = "La Longitud debe estar entre -180 y 180.")
    private Float Longitud;

    private String ciudad;
}