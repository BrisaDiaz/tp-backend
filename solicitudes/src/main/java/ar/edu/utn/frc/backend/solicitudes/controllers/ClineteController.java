package ar.edu.utn.frc.backend.solicitudes.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import static org.springframework.http.ResponseEntity.status;
import ar.edu.utn.frc.backend.solicitudes.dto.ClienteDto;
import ar.edu.utn.frc.backend.solicitudes.services.ClienteService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class ClineteController {
    @Autowired
    ClienteService clienteService;

    // Dar de alta un cliente
    @PostMapping
    public ResponseEntity<ClienteDto> guardarCliente(
            @Valid @RequestBody ClienteDto clienteDto) {
        ClienteDto clienteGuardado = clienteService.guardarCliente(clienteDto);
        return status(HttpStatus.CREATED).body(clienteGuardado);
    }
    
    // Actualizar un cliente existente
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDto> actualizarCliente(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteDto clienteDto) {
        return clienteService.actualizarCliente(id, clienteDto)
                .map(clienteActualizado -> ResponseEntity.ok().body(clienteActualizado))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDto> buscarClientePorId(@PathVariable Integer id) {
        return clienteService.buscarPorId(id)
                .map(clienteDto -> ResponseEntity.ok().body(clienteDto))
                .orElse(ResponseEntity.notFound().build());
    }
}
