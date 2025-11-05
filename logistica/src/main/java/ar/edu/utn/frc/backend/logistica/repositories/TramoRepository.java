package ar.edu.utn.frc.backend.logistica.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ar.edu.utn.frc.backend.logistica.entities.Tramo;

@Repository
public interface TramoRepository extends JpaRepository<Tramo, Integer> {

    @Query("SELECT t FROM Tramo t JOIN t.estado e WHERE e.nombre = :nombreEstado")
    List<Tramo> findByEstadoNombre(@Param("nombreEstado") String nombreEstado);

    @Query("SELECT t FROM Tramo t WHERE t.ruta.id = :idRuta ORDER BY t.nroOrden")
    List<Tramo> findByIdRuta(@Param("idRuta") Integer idRuta);

    @Query("SELECT t FROM Tramo t JOIN t.estado e WHERE t.camion.id = :idCamion AND e.nombre = 'Asignado'")
    List<Tramo> findByCamionAsignado(@Param("idCamion") Integer idCamion);
}
