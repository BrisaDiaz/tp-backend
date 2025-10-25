package ar.edu.utn.frc.backend.logistica.entities;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "contenedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"estadoActual"})
@ToString(exclude = {"estadoActual"})
public class Contenedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "volumen", nullable = false, precision = 10, scale = 2)
    private BigDecimal volumen;

    @Column(name = "peso", nullable = false, precision = 10, scale = 2)
    private BigDecimal peso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_actual_id", nullable = false)
    private Estado estadoActual;
}
