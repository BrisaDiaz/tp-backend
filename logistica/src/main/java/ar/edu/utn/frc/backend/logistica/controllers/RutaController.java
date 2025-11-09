package ar.edu.utn.frc.backend.logistica.controllers;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import ar.edu.utn.frc.backend.logistica.dto.TramoDto; // Importación de TramoDto
import ar.edu.utn.frc.backend.logistica.services.RutaService;
import ar.edu.utn.frc.backend.logistica.services.TramoService; // Importación de TramoService
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
    
    private static final Logger logger = LoggerFactory.getLogger(RutaController.class);

    @Autowired
    private RutaService rutaService;
    
    @Autowired
    private TramoService tramoService;

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
        
        logger.info("GET /api/rutas/tentativas/{} - Solicitud de rutas tentativas.", solicitudId);
        
        try {
            List<RutaTentativaDto> rutasTentativas = rutaService.obtenerTentativas(solicitudId);
            logger.info("Rutas tentativas generadas para Solicitud ID {}: {} encontradas.", solicitudId, rutasTentativas.size());
            return ResponseEntity.ok(rutasTentativas);
        } catch (Exception e) {
            logger.error("Error interno al obtener rutas tentativas para Solicitud ID {}: {}", solicitudId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
        summary = "Obtener tramos por ID de ruta",
        description = """
            Retorna la lista de tramos (segmentos) que componen la ruta definitiva asignada.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de tramos de la ruta obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = TramoDto[].class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Ruta o tramos no encontrados")
    })
    @GetMapping("/{id}/tramos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TramoDto>> obtenerTramosPorRuta(
            @Parameter(description = "ID de la ruta", example = "10", required = true) 
            @PathVariable Integer id) {
        
        logger.info("GET /api/rutas/{}/tramos - Solicitud para obtener tramos de la ruta ID {}.", id, id);
        
        List<TramoDto> tramos = tramoService.buscarPorRuta(id);

        if (tramos.isEmpty()) {
            logger.warn("No se encontraron tramos para la ruta ID {}.", id);
            return ResponseEntity.notFound().build();
        }

        logger.info("Se encontraron {} tramos para la ruta ID {}.", tramos.size(), id);
        return ResponseEntity.ok(tramos);
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

        logger.info("POST /api/rutas/solicitud/{} - Intentando asignar ruta a solicitud.", solicitudId);
        logger.debug("Ruta tentativa recibida para Solicitud ID {}: {}", solicitudId, rutaDto);

        Optional<RutaDto> rutaAsignada = rutaService.asignarRutaASolicitud(solicitudId, rutaDto);
        
        if (rutaAsignada.isPresent()) {
            logger.info("Ruta asignada exitosamente a Solicitud ID {}. Ruta ID: {}", solicitudId, rutaAsignada.get().getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(rutaAsignada.get());
        } else {
            logger.warn("Fallo al asignar ruta a Solicitud ID {}. Posiblemente solicitud no encontrada o conflicto.", solicitudId);
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
        
        logger.info("GET /api/rutas/solicitud/{} - Solicitud de ruta asignada.", solicitudId);

        Optional<RutaDto> rutaAsignada = rutaService.obtenerRutaAsignada(solicitudId);
        
        if (rutaAsignada.isPresent()) {
            logger.info("Ruta asignada encontrada para Solicitud ID {}. Ruta ID: {}", solicitudId, rutaAsignada.get().getId());
            return ResponseEntity.ok(rutaAsignada.get());
        } else {
            logger.warn("Ruta asignada no encontrada para Solicitud ID {}.", solicitudId);
            return ResponseEntity.notFound().build();
        }
    }
}