package ar.edu.utn.frc.backend.logistica.dto.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DistanciaDto {
    private String origen;
    private String destino;
    private double kilometros;
    private String duracionTexto;
    private Long duracionSegundos;
}