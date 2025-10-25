package ar.edu.utn.frc.backend.logistica.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"estado", "origen", "destino", "camion", "ruta"})
@ToString(exclude = {"estado", "origen", "destino", "camion", "ruta"})
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer nroOrden;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costoEstimado;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal costoReal;

    @Column(nullable = true)
    private LocalDateTime fechaHoraInicio; // Fecha y hora real de inicio del tramo

    @Column(nullable = true)
    private LocalDateTime fechaHoraFin; // Fecha y hora real de fin del tramo

    @Column(nullable = false)
    private Integer tiempoEstimado; // Duración estimada en segundos

    @Column(nullable = true)
    private Integer tiempoReal; // Duración real en segundos

    @Column(nullable = false)
    private Float distanciaKm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_origen", nullable = false)
    private Deposito origen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_destino", nullable = false)
    private Deposito destino;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tramo", nullable = false)
    private TipoTramo tipoTramo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camion_id", nullable = true)
    private Camion camion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ruta", nullable = false)
    private Ruta ruta;
}

