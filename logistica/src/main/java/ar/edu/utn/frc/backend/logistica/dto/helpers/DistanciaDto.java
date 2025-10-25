package ar.edu.utn.frc.backend.logistica.dto.helpers;

import lombok.Data;

@Data
public class DistanciaDto {
    private String origen;
    private String destino;
    private double kilometros;
    private String duracionTexto;
}