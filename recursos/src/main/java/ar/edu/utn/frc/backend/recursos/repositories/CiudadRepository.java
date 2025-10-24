package ar.edu.utn.frc.backend.recursos.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frc.backend.recursos.entities.Ciudad;

@Repository
public interface CiudadRepository extends JpaRepository<Ciudad, Integer> {
    Optional<Ciudad> findByCodigoPostal(String codigoPostal);

    Optional<Ciudad> findByNombre(String nombre);
}