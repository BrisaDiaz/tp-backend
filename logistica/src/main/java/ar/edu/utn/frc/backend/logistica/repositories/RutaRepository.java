package ar.edu.utn.frc.backend.logistica.repositories;

import ar.edu.utn.frc.backend.logistica.entities.Ruta;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Integer>{
    Optional<Ruta> findBySolicitudId(Integer solicitudId);
}
