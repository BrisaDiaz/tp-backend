package ar.edu.utn.frc.backend.logistica.entities;

import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer cantidadTramos;

    @Column(nullable = false)
    private Integer cantidadDepositos;

    @Column(name = "id_solicitud", nullable = false)
    private Integer idSolicitud;

    // Lista de tramos que componen esta ruta
    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tramo> tramos;
}
