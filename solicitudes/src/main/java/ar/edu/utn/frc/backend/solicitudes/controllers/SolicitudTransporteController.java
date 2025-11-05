package ar.edu.utn.frc.backend.solicitudes.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.springframework.http.ResponseEntity.status;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.backend.solicitudes.dto.ContenedorDto;
import ar.edu.utn.frc.backend.solicitudes.dto.HistoricoEstadoContenedorDto;
import ar.edu.utn.frc.backend.solicitudes.dto.SolicitudTransporteDto;
import ar.edu.utn.frc.backend.solicitudes.dto.SolicitudTransportePostDto;
import ar.edu.utn.frc.backend.solicitudes.dto.helpers.CostoYTiempoDto;
import ar.edu.utn.frc.backend.solicitudes.dto.helpers.InfoDepositoDto;
import ar.edu.utn.frc.backend.solicitudes.services.SolicitudTransporteService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudTransporteController {

    @Autowired
    private SolicitudTransporteService solicitudService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SolicitudTransporteDto>> buscarTodas() {
        return ResponseEntity.ok(solicitudService.buscarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @solicitudTransporteService.esDuenioDeSolicitud(#id, authentication.tokenAttributes['sub'])")
    public ResponseEntity<SolicitudTransporteDto> buscarPorId(@PathVariable Integer id) {
        return solicitudService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SolicitudTransporteDto> crearSolicitud(@Valid @RequestBody SolicitudTransportePostDto postDto) {
        return status(HttpStatus.CREATED).body(solicitudService.guardarSolicitud(postDto));
    }

    @GetMapping("/borrador")
    public ResponseEntity<List<SolicitudTransporteDto>> buscarBorradores() {
        return ResponseEntity.ok(solicitudService.buscarBorradores());
    }

    @PutMapping("/{id}/programada")
    public ResponseEntity<SolicitudTransporteDto> programarSolicitud(
            @PathVariable Integer id, @RequestBody CostoYTiempoDto dto) {
        return ResponseEntity.ok(
                solicitudService.actualizarEstadoAProgramada(id, dto.getCosto(), dto.getTiempo()));
    }

    @PutMapping("/{id}/en-transito")
    public ResponseEntity<SolicitudTransporteDto> marcarEnTransito(@PathVariable Integer id) {
        return ResponseEntity.ok(solicitudService.actualizarEstadoAEnTransito(id));
    }

    @PutMapping("/{id}/entregada")
    public ResponseEntity<SolicitudTransporteDto> marcarEntregada(
            @PathVariable Integer id, @RequestBody CostoYTiempoDto dto) {
        return ResponseEntity.ok(
                solicitudService.actualizarEstadoAEntregada(id, dto.getCosto(), dto.getTiempo()));
    }

    @PutMapping("/{id}/contenedor/en-viaje")
    public ResponseEntity<ContenedorDto> marcarContenedorEnViaje(
            @PathVariable Integer id, @Valid @RequestBody InfoDepositoDto dto) {
        return solicitudService.actualizarContenedorAEnViaje(id, dto.getNombre())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/contenedor/en-deposito")
    public ResponseEntity<ContenedorDto> marcarContenedorEnDeposito(
            @PathVariable Integer id, @Valid @RequestBody InfoDepositoDto dto) {
        return solicitudService.actualizarContenedorAEnDeposito(id, dto.getNombre())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/contenedor/seguimiento")
    @PreAuthorize("hasRole('ADMIN') or @solicitudTransporteService.esDuenioDeSolicitud(#id, authentication.tokenAttributes['sub'])")
    public ResponseEntity<List<HistoricoEstadoContenedorDto>> seguimientoContenedor(@PathVariable Integer id) {
        return ResponseEntity.ok(solicitudService.obtenerSeguimientoContenedor(id));
    }
}
