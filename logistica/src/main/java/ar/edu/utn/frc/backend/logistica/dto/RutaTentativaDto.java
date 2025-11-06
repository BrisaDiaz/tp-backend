package ar.edu.utn.frc.backend.logistica.dto;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO para representar una ruta tentativa con sus tramos")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RutaTentativaDto {

    @Schema(description = "Cantidad total de tramos en la ruta", example = "3", required = true)
    @NotNull(message = "La cantidad de tramos es obligatoria.")
    @Min(value = 1, message = "Debe haber al menos un tramo.")
    Integer cantidadTramos;

    @Schema(description = "Cantidad total de depósitos involucrados en la ruta", example = "4", required = true)
    @NotNull(message = "La cantidad de depósitos es obligatoria.")
    @Min(value = 2, message = "Debe haber al menos dos depósitos (origen y destino).")
    Integer cantidadDepositos;

    @Schema(description = "Lista de tramos que componen la ruta", required = true)
    @NotNull(message = "La lista de tramos es obligatoria.")
    @Size(min = 1, message = "La ruta debe contener al menos un tramo.")
    @Valid
    List<TramoTentativoDto> tramos;
}