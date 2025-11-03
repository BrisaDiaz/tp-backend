package ar.edu.utn.frc.backend.recursos.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "camion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Camion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "dominio", nullable = false, length = 10, unique = true)
    private String dominio;

    @Column(name = "capacidad_volumen", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadVolumen;

    @Column(name = "capacidad_peso", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadPeso;

    @Column(name = "costo_por_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoPorKm;

    @Column(name = "consumo_combustible_promedio", nullable = false, precision = 10, scale = 2)
    private BigDecimal consumoCombustiblePromedio;

    @Column(name = "nombre_transportista", nullable = false, length = 100)
    private String nombreTransportista;

    @Column(name = "telefono_transportista", nullable = false, length = 20)
    private String telefonoTransportista;

    // Se inicializa en "True" al crear un camión
    @Column(name = "disponibilidad", nullable = false)
    private Boolean disponibilidad = true;

    public void liberar() {
        this.disponibilidad = true;
    }

    public void ocupar() {
        this.disponibilidad = false;
    }
}
