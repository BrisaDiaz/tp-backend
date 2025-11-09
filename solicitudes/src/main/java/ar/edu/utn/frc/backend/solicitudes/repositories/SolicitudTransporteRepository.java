package ar.edu.utn.frc.backend.solicitudes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frc.backend.solicitudes.entities.SolicitudTransporte;

@Repository
public interface SolicitudTransporteRepository extends JpaRepository<SolicitudTransporte, Integer> {

    @Query("SELECT s FROM SolicitudTransporte s JOIN s.estado e WHERE e.nombre = :nombreEstado")
    List<SolicitudTransporte> findByEstadoNombre(@Param("nombreEstado") String nombreEstado);

    List<SolicitudTransporte> findByClienteId(Integer idCliente);
}
