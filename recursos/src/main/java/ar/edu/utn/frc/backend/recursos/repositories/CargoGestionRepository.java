package ar.edu.utn.frc.backend.recursos.repositories;

import ar.edu.utn.frc.backend.recursos.entities.CargoGestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoGestionRepository extends JpaRepository<CargoGestion, Integer> {
    // Encuentra el registro de cargo de gestión que tiene fechaHoraHasta en NULL (actualmente vigente)
    List<CargoGestion> findByFechaHoraHastaIsNullOrderByFechaHoraDesdeDesc();
}