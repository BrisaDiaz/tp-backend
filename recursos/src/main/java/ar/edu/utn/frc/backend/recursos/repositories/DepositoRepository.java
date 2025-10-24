package ar.edu.utn.frc.backend.recursos.repositories;

import ar.edu.utn.frc.backend.recursos.entities.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositoRepository extends JpaRepository<Deposito, Integer> {
}