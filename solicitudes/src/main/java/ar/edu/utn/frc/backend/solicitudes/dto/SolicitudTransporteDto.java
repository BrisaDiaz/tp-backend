package ar.edu.utn.frc.backend.solicitudes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SolicitudTransporteDto {
    private Integer id;
    private LocalDate fechaSolicitud;
    private BigDecimal costoEstimado;
    private BigDecimal tiempoEstimado;
    private BigDecimal costoReal;
    private BigDecimal tiempoReal;
    private String estado;
    private Integer clienteId;
    private Integer contenedorId;
    private DepositoDto depositoOrigen;
    private DepositoDto depositoDestino;
}