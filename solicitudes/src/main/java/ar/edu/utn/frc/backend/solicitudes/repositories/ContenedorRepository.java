package ar.edu.utn.frc.backend.solicitudes.repositories;

import ar.edu.utn.frc.backend.solicitudes.entities.Contenedor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, Integer>{
    
    // CORRECCIÓN: Se cambió 'contenedor' (nombre de tabla) por 'Contenedor' (nombre de la Entidad Java)
    @Query("SELECT c FROM Contenedor c JOIN c.estadoActual e WHERE e.nombre = :nombreEstado")
    List<Contenedor> findByEstadoNombre(@Param("nombreEstado") String nombreEstado);

    @Query(value = "SELECT c.* FROM contenedor c " +
                   "JOIN solicitudes_transporte s ON c.id = s.id_contenedor " +
                   "WHERE s.id_cliente = :idCliente",
             nativeQuery = true)
    List<Contenedor> findBySolicitudClienteId(@Param("idCliente") Integer idCliente);
}
