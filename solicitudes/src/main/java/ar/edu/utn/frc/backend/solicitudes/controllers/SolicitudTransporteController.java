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
@RequestMapping("/api/solicitudes")
@Tag(name = "3. Gestión de Solicitudes de Transporte", description = "APIs para gestionar solicitudes de transporte - Creación, seguimiento y actualización de estados")
@SecurityRequirement(name = "bearerAuth")
public class SolicitudTransporteController {

    @Autowired
    private SolicitudTransporteService solicitudService;

    @Operation(
        summary = "Obtener todas las solicitudes",
        description = """
            Retorna la lista completa de solicitudes de transporte del sistema.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de solicitudes obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = SolicitudTransporteDto[].class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SolicitudTransporteDto>> buscarTodas() {
        return ResponseEntity.ok(solicitudService.buscarTodos());
    }

    @Operation(
        summary = "Obtener solicitud por ID",
        description = """
            Retorna los detalles de una solicitud específica por su ID.
            
            **Roles permitidos:** ADMIN o el cliente propietario de la solicitud
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Solicitud encontrada",
            content = @Content(schema = @Schema(implementation = SolicitudTransporteDto.class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - No es propietario de la solicitud o no tiene rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @solicitudTransporteService.esDuenioDeSolicitud(#id, authentication.principal.claims['sub'])")
    public ResponseEntity<SolicitudTransporteDto> buscarPorId(
            @Parameter(description = "ID de la solicitud", example = "1", required = true) 
            @PathVariable Integer id) {
        return solicitudService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Crear nueva solicitud",
        description = """
            Crea una nueva solicitud de transporte en estado "Borrador".
            
            **Acceso:** Público (interno)
            """,
        security = {}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201", 
            description = "Solicitud creada exitosamente",
            content = @Content(schema = @Schema(implementation = SolicitudTransporteDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Cliente, depósito de origen o destino no encontrados"),
        @ApiResponse(responseCode = "409", description = "Conflicto - El cliente ya tiene una solicitud activa con los mismos datos")
    })
    @PostMapping
    public ResponseEntity<SolicitudTransporteDto> crearSolicitud(
            @Valid @RequestBody SolicitudTransportePostDto postDto) {
        return status(HttpStatus.CREATED).body(solicitudService.guardarSolicitud(postDto));
    }

    @Operation(
        summary = "Obtener solicitudes en borrador",
        description = """
            Retorna todas las solicitudes que están en estado "Borrador".
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de solicitudes en borrador obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = SolicitudTransporteDto[].class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/borrador")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SolicitudTransporteDto>> buscarBorradores() {
        return ResponseEntity.ok(solicitudService.buscarBorradores());
    }

    @Operation(
        summary = "Programar solicitud",
        description = """
            Actualiza el estado de la solicitud a "Programada" con costo y tiempo estimado.
            Utilizado por el servicio de Logística cuando asigna una ruta.
            
            **Acceso:** Público (interno)
            """,
        security = {}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Solicitud programada exitosamente",
            content = @Content(schema = @Schema(implementation = SolicitudTransporteDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
        @ApiResponse(responseCode = "409", description = "La solicitud no está en estado adecuado para programar")
    })
    @PutMapping("/{id}/programada")
    public ResponseEntity<SolicitudTransporteDto> programarSolicitud(
            @Parameter(description = "ID de la solicitud", example = "1", required = true) 
            @PathVariable Integer id, 
            
            @RequestBody CostoYTiempoDto dto) {
        return ResponseEntity.ok(
                solicitudService.actualizarEstadoAProgramada(id, dto.getCosto(), dto.getTiempo()));
    }

    @Operation(
        summary = "Marcar solicitud en tránsito",
        description = """
            Actualiza el estado de la solicitud a "En Tránsito".
            Utilizado por el servicio de Logística cuando inicia el primer tramo.
            
            **Acceso:** Público (interno)
            """,
        security = {}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Solicitud marcada como en tránsito exitosamente",
            content = @Content(schema = @Schema(implementation = SolicitudTransporteDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
        @ApiResponse(responseCode = "409", description = "La solicitud no está en estado adecuado para marcar como en tránsito")
    })
    @PutMapping("/{id}/en-transito")
    public ResponseEntity<SolicitudTransporteDto> marcarEnTransito(
            @Parameter(description = "ID de la solicitud", example = "1", required = true) 
            @PathVariable Integer id) {
        return ResponseEntity.ok(solicitudService.actualizarEstadoAEnTransito(id));
    }

    @Operation(
        summary = "Marcar solicitud como entregada",
        description = """
            Actualiza el estado de la solicitud a "Entregada" con costo y tiempo real.
            Utilizado por el servicio de Logística cuando finaliza el último tramo.
            
            **Acceso:** Público (interno)
            """,
        security = {}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Solicitud marcada como entregada exitosamente",
            content = @Content(schema = @Schema(implementation = SolicitudTransporteDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
        @ApiResponse(responseCode = "409", description = "La solicitud no está en estado adecuado para marcar como entregada")
    })
    @PutMapping("/{id}/entregada")
    public ResponseEntity<SolicitudTransporteDto> marcarEntregada(
            @Parameter(description = "ID de la solicitud", example = "1", required = true) 
            @PathVariable Integer id, 
            
            @RequestBody CostoYTiempoDto dto) {
        return ResponseEntity.ok(
                solicitudService.actualizarEstadoAEntregada(id, dto.getCosto(), dto.getTiempo()));
    }

    @Operation(
        summary = "Marcar contenedor en viaje",
        description = """
            Actualiza el estado del contenedor a "En Viaje" desde un depósito específico.
            Utilizado por el servicio de Logística durante el seguimiento de tramos.
            
            **Acceso:** Público (interno)
            """,
        security = {}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Contenedor marcado como en viaje exitosamente",
            content = @Content(schema = @Schema(implementation = ContenedorDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
        @ApiResponse(responseCode = "409", description = "El contenedor no está en estado adecuado para marcar como en viaje")
    })
    @PutMapping("/{id}/contenedor/en-viaje")
    public ResponseEntity<ContenedorDto> marcarContenedorEnViaje(
            @Parameter(description = "ID de la solicitud", example = "1", required = true) 
            @PathVariable Integer id, 
            
            @Valid @RequestBody InfoDepositoDto dto) {
        return solicitudService.actualizarContenedorAEnViaje(id, dto.getNombre())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Marcar contenedor en depósito",
        description = """
            Actualiza el estado del contenedor a "En Depósito" en un depósito específico.
            Utilizado por el servicio de Logística durante el seguimiento de tramos.
            
            **Acceso:** Público (interno)
            """,
        security = {}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Contenedor marcado como en depósito exitosamente",
            content = @Content(schema = @Schema(implementation = ContenedorDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
        @ApiResponse(responseCode = "409", description = "El contenedor no está en estado adecuado para marcar como en depósito")
    })
    @PutMapping("/{id}/contenedor/en-deposito")
    public ResponseEntity<ContenedorDto> marcarContenedorEnDeposito(
            @Parameter(description = "ID de la solicitud", example = "1", required = true) 
            @PathVariable Integer id, 
            
            @Valid @RequestBody InfoDepositoDto dto) {
        return solicitudService.actualizarContenedorAEnDeposito(id, dto.getNombre())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Obtener seguimiento del contenedor",
        description = """
            Retorna el histórico completo de estados del contenedor asociado a la solicitud.
            
            **Roles permitidos:** ADMIN o el cliente propietario de la solicitud
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Seguimiento del contenedor obtenido exitosamente",
            content = @Content(schema = @Schema(implementation = HistoricoEstadoContenedorDto[].class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - No es propietario de la solicitud o no tiene rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    @GetMapping("/{id}/contenedor/seguimiento")
    @PreAuthorize("hasRole('ADMIN') or @solicitudTransporteService.esDuenioDeSolicitud(#id, authentication.principal.claims['sub'])")
    public ResponseEntity<List<HistoricoEstadoContenedorDto>> seguimientoContenedor(
            @Parameter(description = "ID de la solicitud", example = "1", required = true) 
            @PathVariable Integer id) {
        return ResponseEntity.ok(solicitudService.obtenerSeguimientoContenedor(id));
    }
}