package ar.edu.utn.frc.backend.solicitudes.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frc.backend.solicitudes.entities.HistoricoEstadoContenedor;

@Repository
public interface HistoricoEstadoContenedorRepository extends JpaRepository<HistoricoEstadoContenedor, Integer> {
    List<HistoricoEstadoContenedor> findByContenedorIdOrderByFechaHoraDesdeAsc(Integer contenedorId);

    Optional<HistoricoEstadoContenedor> findByContenedorIdAndFechaHoraHastaIsNull(Integer contenedorId);
}
