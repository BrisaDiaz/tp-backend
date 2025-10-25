package ar.edu.utn.frc.backend.recursos.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.springframework.http.ResponseEntity.status;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.backend.recursos.dto.CamionDto;
import ar.edu.utn.frc.backend.recursos.services.CamionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/camiones")
public class CamionController {

    @Autowired
    private CamionService camionService;

    // Obtener todos los camiones disponibles con capacidad mínima de volumen y peso
    @GetMapping("/libres")
    public ResponseEntity<List<CamionDto>> obtenerCamionesDisponibles(
        @RequestParam(name = "volumen", required = false, defaultValue = "0") BigDecimal volumen,
        @RequestParam(name = "peso", required = false, defaultValue = "0") BigDecimal peso
    ) {
        List<CamionDto> camiones = camionService.buscarCamionesDisponibles(volumen, peso);
        return ResponseEntity.ok().body(camiones);
    }

    // Obtener todos los camiones
    @GetMapping
    public ResponseEntity<List<CamionDto>> obtenerTodosLosCamiones() {
        List<CamionDto> camiones = camionService.buscarTodosLosCamiones();
        return ResponseEntity.ok().body(camiones);
    }

    // Obtener camión por ID
    @GetMapping("/{camionId}")
    public ResponseEntity<CamionDto> obtenerCamionPorId(@RequestParam Integer camionId) {
        return camionService.buscarPorId(camionId)
                .map(camionDto -> ResponseEntity.ok().body(camionDto))
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener camión por dominio
    @GetMapping("/dominio")
    public ResponseEntity<CamionDto> obtenerCamionPorDominio(@RequestParam String dominio) {
        return camionService.buscarPorDominio(dominio)
                .map(camionDto -> ResponseEntity.ok().body(camionDto))
                .orElse(ResponseEntity.notFound().build());
    }

    // Alta de nuevo camión
    @PostMapping
    public ResponseEntity<CamionDto> agregarCamion(
        @Valid @RequestBody CamionDto camionDto) {
        CamionDto camionGuardado = camionService.guardarCamion(camionDto);
        return status(HttpStatus.CREATED).body(camionGuardado);
    }

    // Actualizar camión existente
    @PutMapping("/{camionId}")
    public ResponseEntity<CamionDto> actualizarCamion(@RequestParam Integer camionId, CamionDto camionDto) {
        return camionService.actualizarCamion(camionId, camionDto)
                .map(camionActualizado -> ResponseEntity.ok().body(camionActualizado))
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar camión
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCamion(@PathVariable Integer id) {
        if (camionService.eliminarCamion(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Marcar camión como ocupado
    @PutMapping("/{id}/ocupado")
    public ResponseEntity<CamionDto> setOcupado(@PathVariable Integer id) {
        return camionService.setCamionOcupado(id)
                .map(camionActualizado -> ResponseEntity.ok().body(camionActualizado))
                .orElse(ResponseEntity.notFound().build());
    }

    // Marcar camión como libre
    @PutMapping("/{id}/libre")
    public ResponseEntity<CamionDto> setLibre(@PathVariable Integer id) {
        return camionService.setCamionLibre(id)
                .map(camionActualizado -> ResponseEntity.ok().body(camionActualizado))
                .orElse(ResponseEntity.notFound().build());
    }

}
