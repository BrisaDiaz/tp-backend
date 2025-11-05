package ar.edu.utn.frc.backend.logistica.dto.helpers;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CoordenadasDto {
    @NotNull(message = "La latitud es obligatoria.")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90.")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90.")
    private Float latitud;

    @NotNull(message = "La Longitud es obligatoria.")
    @DecimalMin(value = "-180.0", message = "La Longitud debe estar entre -180 y 180.")
    @DecimalMax(value = "180.0", message = "La Longitud debe estar entre -180 y 180.")
    private Float Longitud;
}
