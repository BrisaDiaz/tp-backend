package ar.edu.utn.frc.backend.logistica.controllers;

import ar.edu.utn.frc.backend.logistica.dto.RutaTentativaDto;
import ar.edu.utn.frc.backend.logistica.dto.RutaDto;
import ar.edu.utn.frc.backend.logistica.services.RutaService;

import java.util.List;
import java.util.Optional;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {
    @Autowired
    private RutaService rutaService;

    /**
     * GET /rutas/tentativas/{solicitudId}
     * Obtiene una lista de rutas alternativas (tentativas) para una solicitud.
     */
    @GetMapping("/tentativas/{solicitudId}")
    public ResponseEntity<List<RutaTentativaDto>> obtenerTentativas(@PathVariable Integer solicitudId) {
        try {
            List<RutaTentativaDto> rutasTentativas = rutaService.obtenerTentativas(solicitudId);
            return ResponseEntity.ok(rutasTentativas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /rutas/solicitud/{solicitudId}
     * Asigna una ruta tentativa seleccionada a la solicitud y la persiste.
     */
    @PostMapping("/solicitud/{solicitudId}")
    public ResponseEntity<RutaDto> asignarRutaASolicitud(
            @PathVariable Integer solicitudId,
            @Valid @RequestBody RutaTentativaDto rutaDto) {

        Optional<RutaDto> rutaAsignada = rutaService.asignarRutaASolicitud(solicitudId, rutaDto);
        
        if (rutaAsignada.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(rutaAsignada.get());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * GET /rutas/solicitud/{solicitudId}
     * Obtiene la ruta definitiva (asignada y persistida) de una solicitud.
     */
    @GetMapping("/solicitud/{solicitudId}")
    public ResponseEntity<RutaDto> obtenerRutaSolicitud(@PathVariable Integer solicitudId) {
        Optional<RutaDto> rutaAsignada = rutaService.obtenerRutaAsignada(solicitudId);
        
        if (rutaAsignada.isPresent()) {
            return ResponseEntity.ok(rutaAsignada.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}