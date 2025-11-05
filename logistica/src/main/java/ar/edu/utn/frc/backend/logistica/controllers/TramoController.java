package ar.edu.utn.frc.backend.logistica.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.backend.logistica.dto.TramoDto;
import ar.edu.utn.frc.backend.logistica.services.TramoService;
import java.util.List;

@RestController
@RequestMapping("/api/tramos")
public class TramoController {

    // DTO local para la asignación (Input Body: { "camionId": 123 })
    record AsignarCamionRequest(Integer camionId) {}

    @Autowired
    private TramoService tramoService;

    /**
     * PUT /api/tramos/{tramoId}/asignado
     * Asigna un camión a un tramo y marca el camión como ocupado.
     * Roles: Administrador/Operador.
     */
    @PutMapping("/{tramoId}/asignado")
    public ResponseEntity<TramoDto> asignarCamionATramo(@PathVariable Integer tramoId, @RequestBody AsignarCamionRequest request) {
        return tramoService.asignarCamion(tramoId, request.camionId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/tramos/asignado/{camionId}
     * Retorna todos los tramos pendientes en estado “Asignados” para un camión.
     * Roles: Transportista.
     */
    @GetMapping("/asignado/{camionId}")
    public ResponseEntity<List<TramoDto>> obtenerTramosAsignadosACamion(@PathVariable Integer camionId) {
        // Se llama al método que utiliza la consulta filtrada del repositorio
        List<TramoDto> tramos = tramoService.buscarTramosAsignadosCamion(camionId);
        if (tramos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tramos);
    }

    /**
     * PUT /api/tramos/{tramoId}/iniciado
     * Actualiza el estado del tramo a “Iniciado” y notifica a ServicioSolicitud.
     * Roles: Transportista.
     */
    @PutMapping("/{tramoId}/iniciado")
    @PreAuthorize("hasRole('TRANSPORTISTA') and @tramoService.esPropietarioDelTramo(#tramoId, authentication.principal.claims['sub'])")
    public ResponseEntity<TramoDto> iniciarTramo(@PathVariable Integer tramoId) {
        return tramoService.iniciarTramo(tramoId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/tramos/{tramoId}/finalizado
     * Actualiza el estado del tramo a “Finalizado”, libera el camión y calcula/notifica a ServicioSolicitud.
     * Roles: Transportista.
     */
    @PutMapping("/{tramoId}/finalizado")
    @PreAuthorize("hasRole('TRANSPORTISTA') and @tramoService.esPropietarioDelTramo(#tramoId, authentication.principal.claims['sub'])")
    public ResponseEntity<TramoDto> finalizarTramo(@PathVariable Integer tramoId) {
        return tramoService.finalizarTramo(tramoId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}