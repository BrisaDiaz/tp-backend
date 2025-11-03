package ar.edu.utn.frc.backend.recursos.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.backend.recursos.dto.CiudadDto;
import ar.edu.utn.frc.backend.recursos.services.CiudadService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ciudades")
public class CiudadController {

    @Autowired
    private CiudadService ciudadService;

    // GET /api/ciudades: Listar todas las ciudades
    @GetMapping
    public ResponseEntity<List<CiudadDto>> obtenerTodasLasCiudades() {
        // Asumiendo que CiudadService tiene un método para buscar todas (se agrega abajo)
        List<CiudadDto> ciudades = ciudadService.buscarTodos();
        return ResponseEntity.ok(ciudades);
    }

    // GET /api/ciudades/buscar?codigoPostal=X5000: Obtener ciudad por código postal
    @GetMapping("/buscar")
    public ResponseEntity<CiudadDto> obtenerCiudadPorCodigoPostal(@RequestParam String codigoPostal) {
        Optional<CiudadDto> ciudadOpt = ciudadService.buscarPorCodigoPostal(codigoPostal);

        return ciudadOpt
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/ciudades/{id}: Obtener ciudad por ID
    @GetMapping("/{id}")
    public ResponseEntity<CiudadDto> obtenerCiudadPorId(@PathVariable Integer id) {
        Optional<CiudadDto> ciudadOpt = ciudadService.buscarPorId(id);

        return ciudadOpt
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/ciudades: Dar de alta una nueva ciudad
    @PostMapping
    public ResponseEntity<CiudadDto> crearCiudad(@Valid @RequestBody CiudadDto ciudadDto) {
        CiudadDto nuevaCiudad = ciudadService.guardarCiudad(ciudadDto);
        // Devuelve 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCiudad);
    }

    // PUT /api/ciudades/{id}: Actualizar una ciudad existente
    @PutMapping("/{id}")
    public ResponseEntity<CiudadDto> actualizarCiudad(@PathVariable Integer id, @Valid @RequestBody CiudadDto ciudadDto) {
        Optional<CiudadDto> ciudadActualizada = ciudadService.actualizarCiudad(id, ciudadDto);

        // Si se actualizó (existía), devuelve 200 OK. Si no, devuelve 404 Not Found.
        return ciudadActualizada
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
