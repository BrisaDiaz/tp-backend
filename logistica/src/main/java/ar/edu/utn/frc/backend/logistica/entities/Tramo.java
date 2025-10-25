package ar.edu.utn.frc.backend.logistica.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer nroOrden;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costoEstimado;

    @Column(precision = 10, scale = 2)
    private BigDecimal costoReal;

    @Column
    private LocalDateTime fechaHoraInicio; // Fecha y hora real de inicio del tramo

    @Column
    private LocalDateTime fechaHoraFin; // Fecha y hora real de fin del tramo

    @Column(nullable = false)
    private Integer tiempoEstimado; // Duración estimada en segundos

    @Column
    private Integer tiempoReal; // Duración real en segundos

    @Column(nullable = false)
    private Float distanciaKm;

    @ManyToOne
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_origen", nullable = false)
    private Depostio origen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_destino", nullable = false)
    private Depostio destino;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tramo", nullable = false)
    private TipoTramo tipoTramo;

    @ManyToOne
    @JoinColumn(name = "camion_id", nullable = true)
    private Camion camion;

    // Relación con la Ruta (Muchos Tramos a Una Ruta)
    @ManyToOne
    @JoinColumn(name = "id_ruta", nullable = false)
    private Ruta ruta;
}

