package ar.edu.utn.frc.backend.logistica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EstadoDto {

    private Integer id;

    @NotBlank(message = "El nombre del estado es obligatorio.")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres.")
    private String nombre;

    @NotBlank(message = "La descripción del estado es obligatoria.")
    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres.")
    private String descripcion;
}