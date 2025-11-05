package ar.edu.utn.frc.backend.solicitudes.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoricoEstadoContenedorDto {
    private Integer id;

    @NotNull(message = "El ID del contenedor es obligatorio.")
    @Positive(message = "El ID del contenedor debe ser un número positivo.")
    private Integer idContenedor;

    @NotBlank(message = "El nombre del estado es obligatorio.")
    private String nombreEstado;

    @NotNull(message = "La fecha y hora de inicio del estado es obligatoria.")
    @PastOrPresent(message = "La fecha de inicio no puede ser futura.")
    private LocalDateTime fechaHoraDesde;

    @PastOrPresent(message = "La fecha de fin no puede ser futura.")
    private LocalDateTime fechaHoraHasta;

    @NotBlank(message = "La descripción del cambio de estado es obligatoria.")
    private String descripcion;
}