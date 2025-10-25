package ar.edu.utn.frc.backend.logistica.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RutaDto {
    Integer id;
    Integer cantidadTramos;
    Integer cantidadDepositos;
    Integer idSolicitud;
    List<TramoDto> tramos;
}