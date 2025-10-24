package ar.edu.utn.frc.backend.recursos.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "precio_combustible")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrecioCombustible {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "precio_por_litro", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPorLitro;

    @Column(name = "fecha_hora_desde", nullable = false)
    private LocalDateTime fechaHoraDesde;

    @Column(name = "fecha_hora_hasta")
    private LocalDateTime fechaHoraHasta;
}