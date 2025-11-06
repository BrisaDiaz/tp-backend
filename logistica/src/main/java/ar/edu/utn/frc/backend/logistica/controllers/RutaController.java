package ar.edu.utn.frc.backend.logistica.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.backend.logistica.dto.RutaDto;
import ar.edu.utn.frc.backend.logistica.dto.RutaTentativaDto;
import ar.edu.utn.frc.backend.logistica.services.RutaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rutas")
@Tag(name = "1. Gestión de Rutas", description = "APIs para generar rutas tentativas y asignar rutas definitivas a solicitudes")
@SecurityRequirement(name = "bearerAuth") // Aplica seguridad a todos los endpoints del controller
public class RutaController {
    
    @Autowired
    private RutaService rutaService;

    @Operation(
        summary = "Obtener rutas tentativas",
        description = """
            Genera y retorna una lista de rutas alternativas (directa, con 1 intermedio, con 2 intermedios) 
            para una solicitud de transporte. Calcula automáticamente distancias, tiempos y costos.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Rutas tentativas generadas exitosamente",
            content = @Content(schema = @Schema(implementation = RutaTentativaDto[].class))
        ),
        @ApiResponse(responseCode = "400", description = "Solicitud no encontrada o datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/tentativas/{solicitudId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RutaTentativaDto>> obtenerTentativas(
            @Parameter(description = "ID de la solicitud de transporte", example = "1", required = true) 
            @PathVariable Integer solicitudId) {
        
        try {
            List<RutaTentativaDto> rutasTentativas = rutaService.obtenerTentativas(solicitudId);
            return ResponseEntity.ok(rutasTentativas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Asignar ruta a solicitud",
        description = """
            Asigna una ruta tentativa seleccionada a la solicitud, la persiste en base de datos, 
            crea los tramos correspondientes y actualiza el estado de la solicitud a 'Programada'.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201", 
            description = "Ruta asignada y persistida exitosamente",
            content = @Content(schema = @Schema(implementation = RutaDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Solicitud no encontrada o datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "409", description = "Conflicto de datos - La solicitud ya tiene una ruta asignada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/solicitud/{solicitudId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RutaDto> asignarRutaASolicitud(
            @Parameter(description = "ID de la solicitud de transporte", example = "1", required = true) 
            @PathVariable Integer solicitudId,
            
            @Valid @RequestBody RutaTentativaDto rutaDto) {

        Optional<RutaDto> rutaAsignada = rutaService.asignarRutaASolicitud(solicitudId, rutaDto);
        
        if (rutaAsignada.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(rutaAsignada.get());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(
        summary = "Obtener ruta asignada",
        description = """
            Retorna la ruta definitiva que ha sido asignada y persistida para una solicitud de transporte, 
            incluyendo todos sus tramos.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Ruta encontrada",
            content = @Content(schema = @Schema(implementation = RutaDto.class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Ruta no encontrada para la solicitud")
    })
    @GetMapping("/solicitud/{solicitudId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RutaDto> obtenerRutaSolicitud(
            @Parameter(description = "ID de la solicitud de transporte", example = "1", required = true) 
            @PathVariable Integer solicitudId) {
        
        Optional<RutaDto> rutaAsignada = rutaService.obtenerRutaAsignada(solicitudId);
        
        if (rutaAsignada.isPresent()) {
            return ResponseEntity.ok(rutaAsignada.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}