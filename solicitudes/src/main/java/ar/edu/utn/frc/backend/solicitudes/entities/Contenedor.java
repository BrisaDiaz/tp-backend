package ar.edu.utn.frc.backend.solicitudes.entities;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contenedor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contenedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "volumen", nullable = false, precision = 10, scale = 2)
    private BigDecimal volumen;

    @Column(name = "peso", nullable = false, precision = 10, scale = 2)
    private BigDecimal peso;

    @ManyToOne
    @JoinColumn(name = "estado_actual_id", nullable = false)
    private Estado estadoActual;

    @OneToOne(mappedBy = "contenedor")
    private SolicitudTransporte solicitud;
}
