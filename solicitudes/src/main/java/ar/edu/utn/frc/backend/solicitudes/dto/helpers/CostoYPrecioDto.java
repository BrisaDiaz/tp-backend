package ar.edu.utn.frc.backend.solicitudes.dto.helpers;

import java.math.BigDecimal;

import org.hibernate.annotations.Check;

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
public class CostoYPrecioDto {
    @NotNull(message = "El costo no puede ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "El costo debe ser mayor que cero")
    private BigDecimal costo;
    @NotNull(message = "El tiempo no puede ser nulo")
    @Check(constraints = "tiempo >= 0")
    private int tiempo;
}
