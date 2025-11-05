package ar.edu.utn.frc.backend.logistica.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "solicitud_transporte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"estado", "cliente", "contenedor", "depositoOrigen", "depositoDestino"})
@ToString(exclude = {"estado", "cliente", "contenedor", "depositoOrigen", "depositoDestino"})
public class SolicitudTransporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "costo_estimado", nullable = true)
    private BigDecimal costoEstimado;

    @Column(name = "tiempo_estimado", nullable = true)
    private Long tiempoEstimado; // en segundos

    @Column(name = "costo_real", nullable = true)
    private BigDecimal costoReal;

    @Column(name = "tiempo_real", nullable = true)
    private Long tiempoReal; // en segundos

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contenedor", nullable = false)
    private Contenedor contenedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_deposito_origen", nullable = false)
    private Deposito depositoOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_deposito_destino", nullable = false)
    private Deposito depositoDestino;

    /**
     * Devuelve un tiempo en formato legible (días, horas, minutos, segundos).
     * @param tiempoSegundos duración en segundos
     * @return representación legible del tiempo
     */
    private String formatearTiempoLegible(Long tiempoSegundos) {
        Long segundos = tiempoSegundos;

        Long dias = segundos / 86400;
        segundos %= 86400;

        Long horas = segundos / 3600;
        segundos %= 3600;

        Long minutos = segundos / 60;
        segundos %= 60;

        StringBuilder tiempoLegible = new StringBuilder();
        if (dias > 0) tiempoLegible.append(dias).append(" días ");
        if (horas > 0) tiempoLegible.append(horas).append(" horas ");
        if (minutos > 0) tiempoLegible.append(minutos).append(" minutos ");
        if (segundos > 0 || tiempoLegible.length() == 0)
            tiempoLegible.append(segundos).append(" segundos");

        return tiempoLegible.toString().trim();
    }

    /**
     * Devuelve el tiempo estimado en formato legible.
     */
    public String getTiempoEstimadoLegible() {
        return formatearTiempoLegible(this.tiempoEstimado);
    }

    /**
     * Devuelve el tiempo real en formato legible.
     */
    public String getTiempoRealLegible() {
        return formatearTiempoLegible(this.tiempoReal);
    }
}
