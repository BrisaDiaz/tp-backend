package ar.edu.utn.frc.backend.solicitudes.repositories;

import ar.edu.utn.frc.backend.solicitudes.entities.SolicitudTransporte;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudTransporteRepository extends JpaRepository<SolicitudTransporte, Integer> {

    @Query("SELECT s FROM solicitudes_transporte s JOIN s.estado e WHERE e.nombre = :nombreEstado")
    List<SolicitudTransporte> findByEstadoNombre(@Param("nombreEstado") String nombreEstado);
}
