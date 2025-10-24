package ar.edu.utn.frc.backend.solicitudes.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContenedorDto {

    private Integer id;

    @NotNull(message = "El volumen es obligatorio.")
    @DecimalMin(value = "0.01", message = "El volumen debe ser mayor que cero.")
    @Digits(integer = 8, fraction = 2, message = "El volumen debe tener máximo 8 enteros y 2 decimales.")
    private BigDecimal volumen;

    @NotNull(message = "El peso es obligatorio.")
    @DecimalMin(value = "0.01", message = "El peso debe ser mayor que cero.")
    @Digits(integer = 8, fraction = 2, message = "El peso debe tener máximo 8 enteros y 2 decimales.")
    private BigDecimal peso;
    
    @NotBlank(message = "El estado actual del contenedor es obligatorio.")
    @Size(max = 50, message = "El estado no puede exceder los 50 caracteres.")
    private String estadoActual;
}