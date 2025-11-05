package ar.edu.utn.frc.backend.logistica.repositories;

import ar.edu.utn.frc.backend.logistica.entities.Estado;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Integer>{
    public Optional<Estado> findByNombre(String nombre);
}
