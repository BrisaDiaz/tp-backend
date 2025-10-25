package ar.edu.utn.frc.backend.solicitudes.repositories;

import ar.edu.utn.frc.backend.solicitudes.entities.Cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer>{

}
