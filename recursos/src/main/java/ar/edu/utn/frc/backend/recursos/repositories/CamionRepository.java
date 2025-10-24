package ar.edu.utn.frc.backend.recursos.repositories;

import ar.edu.utn.frc.backend.recursos.entities.Camion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CamionRepository extends JpaRepository<Camion, Integer> {

    Optional<Camion> findByDominio(String dominio);

    // Encuentra camiones disponibles que cumplan con los requisitos mínimos de volumen y peso
    List<Camion> findByDisponibilidadTrueAndCapacidadVolumenGreaterThanEqualAndCapacidadPesoGreaterThanEqual(
            BigDecimal requiredVolumen, 
            BigDecimal requiredPeso
    );
}