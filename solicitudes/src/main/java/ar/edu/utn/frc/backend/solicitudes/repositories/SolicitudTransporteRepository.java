package ar.edu.utn.frc.backend.solicitudes.repositories;

import ar.edu.utn.frc.backend.solicitudes.entities.SolicitudTransporte;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudTransporteRepository extends JpaRepository<SolicitudTransporte, Integer>{

}
