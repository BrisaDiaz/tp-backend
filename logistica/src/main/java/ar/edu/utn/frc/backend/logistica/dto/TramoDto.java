package ar.edu.utn.frc.backend.logistica.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO para representar un tramo de transporte")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TramoDto {
    
    @Schema(description = "ID único del tramo", example = "1")
    private Integer id;
    
    @Schema(description = "Número de orden en la ruta", example = "1")
    private Integer nroOrden;
    
    @Schema(description = "Depósito de origen")
    private DepositoDto origen;
    
    @Schema(description = "Depósito de destino")
    private DepositoDto destino;
    
    @Schema(description = "Costo estimado del tramo", example = "1500.50")
    private BigDecimal costoEstimado;
    
    @Schema(description = "Costo real del tramo (calculado al finalizar)", example = "1450.75")
    private BigDecimal costoReal;
    
    @Schema(description = "Fecha y hora de inicio real del tramo", example = "2024-01-15T10:30:00")
    private LocalDateTime fechaHoraInicio;
    
    @Schema(description = "Fecha y hora de fin real del tramo", example = "2024-01-15T14:45:00")
    private LocalDateTime fechaHoraFin;
    
    @Schema(description = "Estado actual del tramo", example = "INICIADO")
    private String nombreEstado;
    
    @Schema(description = "Tipo de tramo", example = "ORIGEN_DEPOSITO")
    private String tipoTramo;
    
    @Schema(description = "Camión asignado al tramo")
    private CamionDto camion;
}