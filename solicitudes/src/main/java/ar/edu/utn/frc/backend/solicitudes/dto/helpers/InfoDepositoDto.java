package ar.edu.utn.frc.backend.solicitudes.dto.helpers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InfoDepositoDto {
    @NotNull(message = "El nombre del depósito no puede ser nulo")
    @NotBlank(message = "El nombre del depósito no puede estar vacío")
    private String nombre;
}
