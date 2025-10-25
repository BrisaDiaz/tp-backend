package ar.edu.utn.frc.backend.logistica.dto;

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
    private int tiempoEstimado;
    private BigDecimal costoReal;
    private int tiempoReal;
    private String estado;
    private Integer clienteId;
    private ContenedorDto contenedor;
    private DepositoDto depositoOrigen;
    private DepositoDto depositoDestino;
}