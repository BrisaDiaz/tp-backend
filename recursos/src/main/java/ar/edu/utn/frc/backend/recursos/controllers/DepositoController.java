package ar.edu.utn.frc.backend.recursos.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.backend.recursos.dto.DepositoDto;
import ar.edu.utn.frc.backend.recursos.services.DepositoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/depositos")
public class DepositoController {
    
    @Autowired
    private DepositoService depositoService;

    // Obtener todos los depósitos
    @GetMapping
    public ResponseEntity<List<DepositoDto>> obtenerTodosLosDepositos() {
        List<DepositoDto> depositos = depositoService.buscarTodosLosDepositos();
        return ResponseEntity.ok().body(depositos);
    }

    // Obtener depósito por ID
    @GetMapping("/{id}")
    public ResponseEntity<DepositoDto> obtenerDepositoPorId(@PathVariable Integer id) {
        return depositoService.buscarPorId(id)
                .map(depositoDto -> ResponseEntity.ok().body(depositoDto))
                .orElse(ResponseEntity.notFound().build());
    }

    // Alta de nuevo depósito
    @PostMapping
    public ResponseEntity<DepositoDto> agregarDeposito(@Valid @RequestBody DepositoDto depositoDto) {
        DepositoDto depositoGuardado = depositoService.guardarDeposito(depositoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(depositoGuardado);
    }

    // Actualizar depósito existente
    @PutMapping("/{id}")
    public ResponseEntity<DepositoDto> actualizarDeposito(@PathVariable Integer id, @Valid @RequestBody DepositoDto depositoDto) {
        return depositoService.actualizarDeposito(id, depositoDto)
                .map(depositoActualizado -> ResponseEntity.ok().body(depositoActualizado))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Eliminar depósito
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDeposito(@PathVariable Integer id) {
        depositoService.eliminarDeposito(id);
        return ResponseEntity.noContent().build();
    }
}