package ar.edu.utn.frc.backend.solicitudes.dto;

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

    @NotNull(message = "El contenedor es obligatorio.")
    private ContenedorDto contenedor;
}