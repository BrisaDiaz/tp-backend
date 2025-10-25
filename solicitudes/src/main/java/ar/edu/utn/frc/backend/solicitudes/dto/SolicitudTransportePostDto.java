package ar.edu.utn.frc.backend.solicitudes.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SolicitudTransportePostDto {

    @NotNull(message = "El ID del cliente es obligatorio.")
    private Integer idCliente;

    @NotNull(message = "El ID del depósito de origen es obligatorio.")
    private Integer idDepositoOrigen;

    @NotNull(message = "El ID del depósito de destino es obligatorio.")
    private Integer idDepositoDestino;

    @NotNull(message = "El volumen es obligatorio.")
    @DecimalMin(value = "0.01", message = "El volumen debe ser mayor que cero.")
    @Digits(integer = 8, fraction = 2, message = "El volumen debe tener máximo 8 enteros y 2 decimales.")
    private BigDecimal volumenContenedor;

    @NotNull(message = "El peso es obligatorio.")
    @DecimalMin(value = "0.01", message = "El peso debe ser mayor que cero.")
    @Digits(integer = 8, fraction = 2, message = "El peso debe tener máximo 8 enteros y 2 decimales.")
    private BigDecimal pesoContenedor;
}