package ar.edu.utn.frc.backend.recursos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class CiudadDto {

    private Integer id;

    @NotBlank(message = "El nombre de la ciudad es obligatorio.")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres.")
    private String nombre;

    @NotBlank(message = "El código postal es obligatorio.")
    @Size(max = 10, message = "El código postal no puede exceder los 10 caracteres.")
    private String codigoPostal;
}