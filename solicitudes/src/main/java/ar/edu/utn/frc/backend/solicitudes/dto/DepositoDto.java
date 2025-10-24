package ar.edu.utn.frc.backend.solicitudes.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class DepositoDto {
    private Integer id;
    private String nombre;
    private String direccion;
    private BigDecimal precioPorDia;
    private Float latitud;
    private Float longitud;
    private String ciudad;
}