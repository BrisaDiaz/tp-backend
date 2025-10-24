package ar.edu.utn.frc.backend.solicitudes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClienteDto {

    private Integer id;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres.")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(max = 100, message = "El apellido no puede exceder los 100 caracteres.")
    private String apellido;

    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El formato del email no es válido.")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres.")
    private String email;

    @Size(max = 50, message = "El teléfono no puede exceder los 50 caracteres.")
    private String telefono;

    @NotBlank(message = "El DNI es obligatorio.")
    @Pattern(regexp = "^[0-9]{7,15}$", message = "El DNI debe contener solo números (7 a 15 dígitos).")
    private String dni;
}