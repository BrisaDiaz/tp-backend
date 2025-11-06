package ar.edu.utn.frc.backend.logistica.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO para representar una ruta asignada y persistida")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RutaDto {
    
    @Schema(description = "ID único de la ruta", example = "1")
    Integer id;
    
    @Schema(description = "Cantidad total de tramos en la ruta", example = "3")
    Integer cantidadTramos;
    
    @Schema(description = "Cantidad total de depósitos involucrados", example = "4")
    Integer cantidadDepositos;
    
    @Schema(description = "ID de la solicitud asociada a esta ruta", example = "1")
    Integer idSolicitud;
    
    @Schema(description = "Lista de tramos que componen la ruta")
    List<TramoDto> tramos;
}