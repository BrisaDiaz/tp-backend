package ar.edu.utn.frc.backend.recursos.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.springframework.http.ResponseEntity.status;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/camiones")
@Tag(name = "1. Gestión de Camiones", description = "APIs para gestionar la flota de camiones - Altas, bajas, modificaciones y control de estado")
@SecurityRequirement(name = "bearerAuth")
public class CamionController {
    private static final Logger logger = LoggerFactory.getLogger(CamionController.class);

    @Autowired
    private CamionService camionService;

    @Operation(
        summary = "Obtener camiones disponibles",
        description = """
            Retorna todos los camiones disponibles que cumplen con la capacidad mínima requerida.
            
            **Roles permitidos:** ADMIN
            """,
        security = {}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de camiones disponibles obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = CamionDto[].class))
        ),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/libres")
    public ResponseEntity<List<CamionDto>> obtenerCamionesDisponibles(
        @Parameter(description = "Volumen mínimo requerido en m³", example = "50.0") 
        @RequestParam(name = "volumen", required = false, defaultValue = "0") BigDecimal volumen,
        
        @Parameter(description = "Peso mínimo requerido en kg", example = "10000.0") 
        @RequestParam(name = "peso", required = false, defaultValue = "0") BigDecimal peso
    ) {
        logger.info("INICIO - GET /api/camiones/libres. Filtros: Volumen={}, Peso={}", volumen, peso);
        List<CamionDto> camiones = camionService.buscarCamionesDisponibles(volumen, peso);
        logger.info("FIN - GET /api/camiones/libres. {} camiones disponibles encontrados.", camiones.size());
        return ResponseEntity.ok().body(camiones);
    }

    @Operation(
        summary = "Obtener todos los camiones",
        description = """
            Retorna la lista completa de camiones del sistema.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de camiones obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = CamionDto[].class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CamionDto>> obtenerTodosLosCamiones() {
        logger.info("INICIO - GET /api/camiones. Obteniendo todos los camiones.");
        List<CamionDto> camiones = camionService.buscarTodosLosCamiones();
        logger.info("FIN - GET /api/camiones. Total de {} camiones encontrados.", camiones.size());
        return ResponseEntity.ok().body(camiones);
    }

    @Operation(
        summary = "Obtener camión por ID",
        description = """
            Retorna los detalles de un camión específico por su ID.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Camión encontrado",
            content = @Content(schema = @Schema(implementation = CamionDto.class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Camión no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CamionDto> obtenerCamionPorId(
            @Parameter(description = "ID del camión", example = "1", required = true) 
            @PathVariable Integer id) {

        logger.info("INICIO - GET /api/camiones/{}. Buscando camión por ID.", id);
        Optional<CamionDto> result = camionService.buscarPorId(id);
        if (result.isPresent()) {
            logger.info("FIN - GET /api/camiones/{}. Camión encontrado.", id);
        } else {
            logger.warn("FIN - GET /api/camiones/{}. Camión no encontrado (404).", id);
        }
        return result
             .map(camionDto -> ResponseEntity.ok().body(camionDto))
             .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Obtener camión por dominio",
        description = """
            Busca un camión por su número de dominio (patente).
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Camión encontrado",
            content = @Content(schema = @Schema(implementation = CamionDto.class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Camión no encontrado")
    })
    @GetMapping("/dominio")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CamionDto> obtenerCamionPorDominio(
            @Parameter(description = "Dominio del camión (patente)", example = "ABC123", required = true) 
            @RequestParam String dominio) {
        
        logger.info("INICIO - GET /api/camiones/dominio?dominio={}. Buscando camión por dominio.", dominio);
        Optional<CamionDto> result = camionService.buscarPorDominio(dominio);
        if (result.isPresent()) {
            logger.info("FIN - GET /api/camiones/dominio. Camión con dominio {} encontrado.", dominio);
        } else {
            logger.warn("FIN - GET /api/camiones/dominio. Camión con dominio {} no encontrado (404).", dominio);
        }
        return result
                .map(camionDto -> ResponseEntity.ok().body(camionDto))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Crear nuevo camión",
        description = """
            Da de alta un nuevo camión en el sistema.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201", 
            description = "Camión creado exitosamente",
            content = @Content(schema = @Schema(implementation = CamionDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "409", description = "Conflicto - El dominio ya existe")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CamionDto> agregarCamion(
            @Valid @RequestBody CamionDto camionDto) {
        logger.info("INICIO - POST /api/camiones. Creando nuevo camión con Dominio: {}", camionDto.getDominio());
        CamionDto camionGuardado = camionService.guardarCamion(camionDto);
        logger.info("FIN - POST /api/camiones. Camión ID {} creado exitosamente.", camionGuardado.getId());
        return status(HttpStatus.CREATED).body(camionGuardado);
    }

    @Operation(
        summary = "Actualizar camión",
        description = """
            Actualiza los datos de un camión existente.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Camión actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = CamionDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Camión no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto - El dominio ya existe")
    })
    @PutMapping("/{camionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CamionDto> actualizarCamion(
            @Parameter(description = "ID del camión a actualizar", example = "1", required = true) 
            @PathVariable Integer camionId,
            @Valid @RequestBody CamionDto camionDto) {

        logger.info("INICIO - PUT /api/camiones/{}. Actualizando camión.", camionId);
        Optional<CamionDto> result = camionService.actualizarCamion(camionId, camionDto);
        if (result.isPresent()) {
            logger.info("FIN - PUT /api/camiones/{}. Camión actualizado con éxito. Dominio: {}", camionId, result.get().getDominio());
        } else {
            logger.warn("FIN - PUT /api/camiones/{}. Camión no encontrado para actualizar (404).", camionId);
        }
        return result
                .map(camionActualizado -> ResponseEntity.ok().body(camionActualizado))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Eliminar camión",
        description = """
            Elimina un camión del sistema.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Camión eliminado exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Camión no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto - El camión no puede ser eliminado (tiene tramos asignados)")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarCamion(
            @Parameter(description = "ID del camión a eliminar", example = "1", required = true) 
            @PathVariable Integer id) {
        logger.warn("INICIO - DELETE /api/camiones/{}. Intentando eliminar camión.", id);
        if (camionService.eliminarCamion(id)) {
            logger.info("FIN - DELETE /api/camiones/{}. Camión eliminado con éxito (204).", id);
            return ResponseEntity.noContent().build();
    }
        logger.warn("FIN - DELETE /api/camiones/{}. Camión no encontrado para eliminar (404).", id);
        return ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "Marcar camión como ocupado",
        description = """
            Actualiza el estado del camión a 'ocupado'.
            Utilizado por el servicio de Logística cuando asigna un camión a un tramo.
            
            **Acceso:** Público (interno)
            """,
        security = {}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Camión marcado como ocupado exitosamente",
            content = @Content(schema = @Schema(implementation = CamionDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Camión no encontrado"),
        @ApiResponse(responseCode = "409", description = "El camión ya está ocupado")
    })
    @PutMapping("/{id}/ocupado")
    public ResponseEntity<CamionDto> setOcupado(
            @Parameter(description = "ID del camión", example = "1", required = true) 
            @PathVariable Integer id) {
        logger.info("INICIO - PUT /api/camiones/{}/ocupado. Marcando camión como OCUPADO.", id);
        Optional<CamionDto> result = camionService.setCamionOcupado(id);
        if (result.isPresent()) {
            logger.info("FIN - PUT /api/camiones/{}/ocupado. Camión marcado como OCUPADO con éxito.", id);
        } else {
            logger.warn("FIN - PUT /api/camiones/{}/ocupado. Camión no encontrado (404).", id);
        }
        return result
        .map(camionActualizado -> ResponseEntity.ok().body(camionActualizado))
        .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Marcar camión como libre",
        description = """
            Actualiza el estado del camión a 'libre'.
            Utilizado por el servicio de Logística cuando finaliza un tramo.
            
            **Acceso:** Público (interno)
            """,
        security = {}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Camión marcado como libre exitosamente",
            content = @Content(schema = @Schema(implementation = CamionDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Camión no encontrado"),
        @ApiResponse(responseCode = "409", description = "El camión ya está libre")
    })
    @PutMapping("/{id}/libre")
    public ResponseEntity<CamionDto> setLibre(
            @Parameter(description = "ID del camión", example = "1", required = true) 
            @PathVariable Integer id) {
        logger.info("INICIO - PUT /api/camiones/{}/libre. Marcando camión como LIBRE.", id);
        Optional<CamionDto> result = camionService.setCamionLibre(id);
        if (result.isPresent()) {
            logger.info("FIN - PUT /api/camiones/{}/libre. Camión marcado como LIBRE con éxito.", id);
        } else {
            logger.warn("FIN - PUT /api/camiones/{}/libre. Camión no encontrado (404).", id);
        }
        return result
        .map(camionActualizado -> ResponseEntity.ok().body(camionActualizado))
        .orElse(ResponseEntity.notFound().build());
    }
}