package ar.edu.utn.frc.backend.recursos.repositories;

import ar.edu.utn.frc.backend.recursos.entities.PrecioCombustible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrecioCombustibleRepository extends JpaRepository<PrecioCombustible, Integer> {
    // Encuentra el registro de precio de combustible que tiene fechaHoraHasta en NULL (actualmente vigente)
    List<PrecioCombustible> findByFechaHoraHastaIsNullOrderByFechaHoraDesdeDesc();
}