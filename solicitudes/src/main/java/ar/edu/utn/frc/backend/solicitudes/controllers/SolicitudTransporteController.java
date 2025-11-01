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
    private SolicitudTransporteService solicitudTransporteService;

    // Obtener una solicitud de transporte por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @solicitudTransporteService.esDuenioDeSolicitud(#id, authentication.principal.claim('cliente_id'))")
    public ResponseEntity<SolicitudTransporteDto> buscarSolicitudPorId(@PathVariable Integer id) {
        return solicitudTransporteService.buscarPorId(id)
                .map(solicitudDto -> ResponseEntity.ok().body(solicitudDto))
                .orElse(ResponseEntity.notFound().build());
    }

    // Dar de alta una solicitud de transporte
    @PostMapping
    public ResponseEntity<SolicitudTransporteDto> crearSolicitud(
            @Valid @RequestBody SolicitudTransportePostDto postDto) {
        SolicitudTransporteDto solicitudCreada = solicitudTransporteService.guardarSolicitud(postDto);
        return status(HttpStatus.CREATED).body(solicitudCreada);
    }

    // Obtener solicitudes de transporte en estado "Borrador"
    @GetMapping("/borrador")
    public ResponseEntity<List<SolicitudTransporteDto>> buscarSolicitudesBorrador() {
        List<SolicitudTransporteDto> solicitudesBorrador = solicitudTransporteService
                .buscarBorradores();
        return ResponseEntity.ok(solicitudesBorrador);
    }

    // Actualizar una solicitud de transporte a estado "Programada"
    @PutMapping("/{id}/programada")
    public ResponseEntity<SolicitudTransporteDto> actualizarSolicitudAProgramada(
            @PathVariable Integer id,
            @RequestBody CostoYTiempoDto costoYTiempoDto) {
        SolicitudTransporteDto solicitudActualizada = solicitudTransporteService.actualizarEstadoAProgramada(
                id,
                costoYTiempoDto.getCosto(),
                costoYTiempoDto.getTiempo());
        return ResponseEntity.ok(solicitudActualizada);
    }

    // Actualizar una solicitud de transporte a estado "En Tránsito"
    @PutMapping("/{id}/en-transito")
    public ResponseEntity<SolicitudTransporteDto> actualizarSolicitudAEnTransito(
            @PathVariable Integer id) {
        SolicitudTransporteDto solicitudActualizada = solicitudTransporteService.actualizarEstadoAEnTransito(id);
        return ResponseEntity.ok(solicitudActualizada);
    }

    // Actualizar una solicitud de transporte a estado "Entregada"
    @PutMapping("/{id}/entregada")
    public ResponseEntity<SolicitudTransporteDto> actualizarSolicitudAEntregada(
            @PathVariable Integer id,
            @RequestBody CostoYTiempoDto costoYTiempoDto) {
        SolicitudTransporteDto solicitudActualizada = solicitudTransporteService.actualizarEstadoAEntregada(
                id,
                costoYTiempoDto.getCosto(),
                costoYTiempoDto.getTiempo());
        return ResponseEntity.ok(solicitudActualizada);
    }

    // Actualizar estado del contenedor de una solicitud a "En Viaje"
    @PutMapping("/{id}/contenedor/en-viaje")
    public ResponseEntity<ContenedorDto> marcarContenedorEnViaje(
            @PathVariable Integer id, @Valid @RequestBody InfoDepositoDto infoDepositoDto) {
        return solicitudTransporteService.actualizarContenedorAEnDeposito(id, infoDepositoDto.getNombre())
                .map(contenedorDto -> ResponseEntity.ok().body(contenedorDto))
                .orElse(ResponseEntity.notFound().build());
    }

    // Actualizar estado del contenedor de una solicitud a "En Depósito"
    @PutMapping("/{id}/contenedor/en-deposito")
    public ResponseEntity<ContenedorDto> marcarContenedorEnDeposito(
            @PathVariable Integer id, @Valid @RequestBody InfoDepositoDto infoDepositoDto) {

        return solicitudTransporteService.actualizarContenedorAEnViaje(id, infoDepositoDto.getNombre())
                .map(contenedorDto -> ResponseEntity.ok().body(contenedorDto))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Obtener seguimiento historico del contenedor asociado a una solicitud
    @GetMapping("/{id}/contenedor/seguimiento")
    @PreAuthorize("hasRole('ADMIN') or @solicitudTransporteService.esDuenioDeSolicitud(#id, authentication.principal.claim('cliente_id'))")
    public ResponseEntity<List<HistoricoEstadoContenedorDto>> obtenerSeguimientoContenedorDeSolicitud(@PathVariable Integer id) {
        List<HistoricoEstadoContenedorDto> seguimiento = solicitudTransporteService.obtenerSeguimientoContenedor(id);
        return ResponseEntity.ok(seguimiento);
    }
}
