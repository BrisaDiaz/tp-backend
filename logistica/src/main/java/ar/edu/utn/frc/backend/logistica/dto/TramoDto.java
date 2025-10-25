package ar.edu.utn.frc.backend.logistica.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TramoDto {
    Integer id;
    Integer nroOrden;
    DepositoDto origen;
    DepositoDto destino;
    BigDecimal costoEstimado;
    BigDecimal costoReal;
    LocalDateTime fechaHoraInicio;
    LocalDateTime fechaHoraFin;
    Integer tiempoEstimadoSegundos;
    Integer tiempoRealSegundos;
    Float distanciaKilometros;
    String nombreEstado;
    String tipoTramo;
    CamionDto camion;
    RutaDto ruta;
}