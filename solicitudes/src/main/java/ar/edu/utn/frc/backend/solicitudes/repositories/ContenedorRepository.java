package ar.edu.utn.frc.backend.solicitudes.repositories;

import ar.edu.utn.frc.backend.solicitudes.entities.Contenedor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, Integer>{
  @Query("SELECT c FROM Contenedor c JOIN c.estadoActual e WHERE e.nombre = :nombreEstado")
    List<Contenedor> findByEstadoNombre(@Param("nombreEstado") String nombreEstado);

    @Query("SELECT c FROM Contenedor c JOIN c.solicitud s JOIN s.cliente cli WHERE cli.id = :idCliente")
    List<Contenedor> findBySolicitudClienteId(@Param("idCliente") Integer idCliente);
}
