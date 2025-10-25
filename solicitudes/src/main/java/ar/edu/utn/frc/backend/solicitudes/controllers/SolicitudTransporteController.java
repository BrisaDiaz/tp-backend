package ar.edu.utn.frc.backend.solicitudes.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.status;
import ar.edu.utn.frc.backend.solicitudes.dto.SolicitudTransporteDto;
import ar.edu.utn.frc.backend.solicitudes.dto.SolicitudTransportePostDto;
import ar.edu.utn.frc.backend.solicitudes.dto.helpers.CostoYPrecioDto;
import ar.edu.utn.frc.backend.solicitudes.services.SolicitudTransporteService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudTransporteController {
    @Autowired
    private SolicitudTransporteService solicitudTransporteService;

    // Obtener una solicitud de transporte por ID
    @GetMapping("/{id}")
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

    // Actualizar una solicitud de transporte a estado "Programada"
    @PutMapping("/{id}/programada")
    public ResponseEntity<SolicitudTransporteDto> actualizarSolicitudAProgramada(
            @PathVariable Integer id,
            @RequestBody CostoYPrecioDto costoYPrecioDto) {
        SolicitudTransporteDto solicitudActualizada = solicitudTransporteService.actualizarEstadoAProgramada(
                id,
                costoYPrecioDto.getCosto(),
                costoYPrecioDto.getTiempo());
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
            @RequestBody CostoYPrecioDto costoYPrecioDto) {
        SolicitudTransporteDto solicitudActualizada = solicitudTransporteService.actualizarEstadoAEntregada(
                id,
                costoYPrecioDto.getCosto(),
                costoYPrecioDto.getTiempo());
        return ResponseEntity.ok(solicitudActualizada);
    }

}
