package ar.edu.utn.frc.backend.solicitudes.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.backend.solicitudes.dto.ContenedorDto;
import ar.edu.utn.frc.backend.solicitudes.services.ContenedorService;

@RestController
@RequestMapping("/api/contenedores")
public class ContenedorController {
    @Autowired
    private ContenedorService contenedorService;

    // Obtener todos los contenedores
    @GetMapping
    public ResponseEntity<List<ContenedorDto>> obtenerTodosLosContenedores() {
        List<ContenedorDto> contenedores = contenedorService.buscarTodos();
        return ResponseEntity.ok(contenedores);
    }

    // Obtener contenedor por ID
    @GetMapping("/{id}")
    public ResponseEntity<ContenedorDto> obtenerContenedorPorId(@PathVariable Integer id) {
        return contenedorService.buscarPorId(id)
                .map(contenedorDto -> ResponseEntity.ok().body(contenedorDto))
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener contenedores pendientes de entrega
    @GetMapping("/pendientes-entrega")
    public ResponseEntity<List<ContenedorDto>> obtenerContenedoresPendientesDeEntrega() {
        List<ContenedorDto> contenedores = contenedorService.buscarContenedoresPendientesDeEntrega();
        return ResponseEntity.ok(contenedores);
    }

    // Obtener contenedores en depósito
    @GetMapping("/en-deposito")
    public ResponseEntity<List<ContenedorDto>> obtenerContenedoresEnDeposito() {
        List<ContenedorDto> contenedores = contenedorService.buscarContenedoresEnDeposito();
        return ResponseEntity.ok(contenedores);
    }

    // Obtener contenedores en viaje
    @GetMapping("/en-viaje")
    public ResponseEntity<List<ContenedorDto>> obtenerContenedoresEnViaje() {
        List<ContenedorDto> contenedores = contenedorService.buscarContenedoresEnViaje();
        return ResponseEntity.ok(contenedores);
    }

    // Obtener contenedores por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ContenedorDto>> obtenerContenedoresPorEstado(@PathVariable String estado) {
        List<ContenedorDto> contenedores = contenedorService.buscarPorEstado(estado);
        return ResponseEntity.ok(contenedores);
    }
}
