package ar.edu.utn.frc.backend.logistica.controllers;

import java.util.List;

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
@RequestMapping("/api/tramos")
@Tag(name = "2. Gestión de Tramos", description = "APIs para gestionar tramos de transporte - Asignación, inicio y finalización de tramos")
@SecurityRequirement(name = "bearerAuth")
public class TramoController {

    @Schema(description = "Request para asignar camión a tramo")
    public record AsignarCamionRequest(
        @Parameter(description = "ID del camión a asignar", example = "1", required = true) 
        Integer camionId
    ) {}

    @Autowired
    private TramoService tramoService;

    @Operation(
        summary = "Asignar camión a tramo",
        description = """
            Asigna un camión específico a un tramo y marca el camión como ocupado.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Camión asignado exitosamente",
            content = @Content(schema = @Schema(implementation = TramoDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Tramo o camión no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto - El camión ya está ocupado o el tramo ya tiene asignación")
    })
    @PutMapping("/{tramoId}/asignado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TramoDto> asignarCamionATramo(
            @Parameter(description = "ID del tramo", example = "1", required = true) 
            @PathVariable Integer tramoId, 
            
            @Valid @RequestBody AsignarCamionRequest request) {
        
        return tramoService.asignarCamion(tramoId, request.camionId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Obtener tramos asignados a camión",
        description = """
            Retorna todos los tramos en estado 'Asignados' para un camión específico.
            
            **Roles permitidos:** TRANSPORTISTA (solo para sus propios camiones)
            **Validación adicional:** El usuario debe ser propietario del camión
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de tramos asignados",
            content = @Content(schema = @Schema(implementation = TramoDto[].class))
        ),
        @ApiResponse(responseCode = "204", description = "No hay tramos asignados"),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - No es propietario del camión o no tiene rol TRANSPORTISTA"),
        @ApiResponse(responseCode = "404", description = "Camión no encontrado")
    })
    @GetMapping("/asignado/{camionId}")
    @PreAuthorize("hasRole('TRANSPORTISTA') and @tramoService.esPropietarioDelCamion(#camionId, authentication.principal.claims['sub'])")
    public ResponseEntity<List<TramoDto>> obtenerTramosAsignadosACamion(
            @Parameter(description = "ID del camión", example = "1", required = true) 
            @PathVariable Integer camionId) {
        
        List<TramoDto> tramos = tramoService.buscarTramosAsignadosCamion(camionId);
        if (tramos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tramos);
    }

    @Operation(
        summary = "Iniciar tramo",
        description = """
            Actualiza el estado del tramo a 'Iniciado', registra la fecha/hora de inicio y 
            notifica al servicio de solicitudes.
            
            **Roles permitidos:** TRANSPORTISTA (solo para sus propios tramos)
            **Validación adicional:** El usuario debe ser propietario del tramo
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Tramo iniciado exitosamente",
            content = @Content(schema = @Schema(implementation = TramoDto.class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - No es propietario del tramo o no tiene rol TRANSPORTISTA"),
        @ApiResponse(responseCode = "404", description = "Tramo no encontrado"),
        @ApiResponse(responseCode = "409", description = "El tramo no está en estado adecuado para iniciar")
    })
    @PutMapping("/{tramoId}/iniciado")
    @PreAuthorize("hasRole('TRANSPORTISTA') and @tramoService.esPropietarioDelTramo(#tramoId, authentication.principal.claims['sub'])")
    public ResponseEntity<TramoDto> iniciarTramo(
            @Parameter(description = "ID del tramo a iniciar", example = "1", required = true) 
            @PathVariable Integer tramoId) {
        
        return tramoService.iniciarTramo(tramoId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Finalizar tramo",
        description = """
            Actualiza el estado del tramo a 'Finalizado', registra la fecha/hora de fin, 
            libera el camión, calcula costos reales y notifica al servicio de solicitudes.
            
            **Roles permitidos:** TRANSPORTISTA (solo para sus propios tramos)
            **Validación adicional:** El usuario debe ser propietario del tramo
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Tramo finalizado exitosamente",
            content = @Content(schema = @Schema(implementation = TramoDto.class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - No es propietario del tramo o no tiene rol TRANSPORTISTA"),
        @ApiResponse(responseCode = "404", description = "Tramo no encontrado"),
        @ApiResponse(responseCode = "409", description = "El tramo no está en estado adecuado para finalizar")
    })
    @PutMapping("/{tramoId}/finalizado")
    @PreAuthorize("hasRole('TRANSPORTISTA') and @tramoService.esPropietarioDelTramo(#tramoId, authentication.principal.claims['sub'])")
    public ResponseEntity<TramoDto> finalizarTramo(
            @Parameter(description = "ID del tramo a finalizar", example = "1", required = true) 
            @PathVariable Integer tramoId) {
        
        return tramoService.finalizarTramo(tramoId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}