package ar.edu.utn.frc.backend.recursos.repositories;

import ar.edu.utn.frc.backend.recursos.entities.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositoRepository extends JpaRepository<Deposito, Integer> {
    @Query("SELECT COUNT(d) > 0 FROM Deposito d WHERE d.ciudad.id = :ciudadId")
    boolean existsByCiudadId(@Param("ciudadId") Integer ciudadId);
}