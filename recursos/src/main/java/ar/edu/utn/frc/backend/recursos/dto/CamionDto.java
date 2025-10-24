package ar.edu.utn.frc.backend.recursos.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class CamionDto {
    private Integer id;

    @NotBlank(message = "El dominio no puede ser nulo ni vacío.")
    @Size(max = 10, message = "El dominio no puede exceder los 10 caracteres.")
    private String dominio;

    @NotNull(message = "La capacidad en volumen es obligatoria.")
    @DecimalMin(value = "0.01", message = "La capacidad en volumen debe ser un valor positivo.")
    @Digits(integer = 8, fraction = 2, message = "La capacidad en volumen debe tener máximo 8 enteros y 2 decimales.")
    private BigDecimal capacidadVolumen; 

    @NotNull(message = "La capacidad en peso es obligatoria.")
    @DecimalMin(value = "0.01", message = "La capacidad en peso debe ser un valor positivo.")
    @Digits(integer = 8, fraction = 2, message = "La capacidad en peso debe tener máximo 8 enteros y 2 decimales.")
    private BigDecimal capacidadPeso; 

    @NotNull(message = "El costo por kilómetro es obligatorio.")
    @DecimalMin(value = "0.00", message = "El costo por kilómetro no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "El costo por kilómetro debe tener máximo 8 enteros y 2 decimales.")
    private BigDecimal costoPorKm; 

    @NotNull(message = "El consumo de combustible promedio es obligatorio.")
    @DecimalMin(value = "0.01", message = "El consumo promedio debe ser un valor positivo.")
    @Digits(integer = 8, fraction = 2, message = "El consumo de combustible promedio debe tener máximo 8 enteros y 2 decimales.")
    private BigDecimal consumoCombustiblePromedio; 

    @NotBlank(message = "El nombre del transportista no puede ser nulo ni vacío.")
    @Size(max = 100, message = "El nombre del transportista no puede exceder los 100 caracteres.")
    private String nombreTransportista;

    @NotBlank(message = "El teléfono del transportista es obligatorio.")
    @Size(max = 20, message = "El teléfono del transportista no puede exceder los 20 caracteres.")
    private String telefonoTransportista;
}