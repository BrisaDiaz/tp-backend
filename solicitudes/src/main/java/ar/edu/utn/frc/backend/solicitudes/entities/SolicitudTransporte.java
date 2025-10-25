package ar.edu.utn.frc.backend.solicitudes.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "solicitudes_transporte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudTransporte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDate fechaSolicitud;

    @Column(name = "costo_estimado", nullable = true)
    private BigDecimal costoEstimado;

    @Column(name = "tiempo_estimado", nullable = true)
    private int tiempoEstimado; // en segundos

    @Column(name = "costo_real", nullable = true)
    private BigDecimal costoReal;

    @Column(name = "tiempo_real", nullable = true)
    private int tiempoReal; // en segundos

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contenedor", nullable = false)
    private Contenedor contenedor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_deposito_origen", nullable = false)
    private Deposito depositoOrigen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_deposito_destino", nullable = false)
    private Deposito depositoDestino;

    public String getTiempoEstimadoLegible(int tiempo) {
        int segundos = tiempo;
        int dias = segundos / 86400;
        segundos %= 86400;
        int horas = segundos / 3600;
        segundos %= 3600;
        int minutos = segundos / 60;
        segundos %= 60;

        StringBuilder tiempoLegible = new StringBuilder();
        if (dias > 0) {
            tiempoLegible.append(dias).append(" días ");
        }
        if (horas > 0) {
            tiempoLegible.append(horas).append(" horas ");
        }
        if (minutos > 0) {
            tiempoLegible.append(minutos).append(" minutos ");
        }
        if (segundos > 0 || tiempoLegible.length() == 0) {
            tiempoLegible.append(segundos).append(" segundos");
        }

        return tiempoLegible.toString().trim();
    }

    public String getTiempoEstimadoLegible() {
        return getTiempoEstimadoLegible(this.tiempoEstimado);
    }

    public String getTiempoRealLegible() {
        return getTiempoEstimadoLegible(this.tiempoReal);
    }
}