package ar.edu.utn.frc.backend.solicitudes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private LocalDateTime fechaSolicitud;
    private BigDecimal costoEstimado;
    private Long tiempoEstimado;
    private BigDecimal costoReal;
    private Long tiempoReal;
    private String estado;
    private Integer clienteId;
    private ContenedorDto contenedor;
    private DepositoDto depositoOrigen;
    private DepositoDto depositoDestino;
}