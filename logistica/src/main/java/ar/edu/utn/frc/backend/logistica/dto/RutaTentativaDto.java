package ar.edu.utn.frc.backend.logistica.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RutaTentativaDto {

    @NotNull(message = "La cantidad de tramos es obligatoria.")
    @Min(value = 1, message = "Debe haber al menos un tramo.")
    Integer cantidadTramos;

    @NotNull(message = "La cantidad de depósitos es obligatoria.")
    @Min(value = 2, message = "Debe haber al menos dos depósitos (origen y destino).")
    Integer cantidadDepositos;

    @NotNull(message = "La lista de tramos es obligatoria.")
    @Size(min = 1, message = "La ruta debe contener al menos un tramo.")
    @Valid
    List<TramoTentativoDto> tramos;
}
