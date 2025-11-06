package ar.edu.utn.frc.backend.solicitudes.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.backend.solicitudes.dto.ContenedorDto;
import ar.edu.utn.frc.backend.solicitudes.services.ContenedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/contenedores")
@Tag(name = "2. Gestión de Contenedores", description = "APIs para consultar contenedores y sus estados")
@SecurityRequirement(name = "bearerAuth")
public class ContenedorController {
    
    @Autowired
    private ContenedorService contenedorService;

    @Operation(
        summary = "Obtener todos los contenedores",
        description = """
            Retorna la lista completa de contenedores del sistema.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de contenedores obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = ContenedorDto[].class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContenedorDto>> obtenerTodosLosContenedores() {
        List<ContenedorDto> contenedores = contenedorService.buscarTodos();
        return ResponseEntity.ok(contenedores);
    }

    @Operation(
        summary = "Obtener contenedor por ID",
        description = """
            Retorna los detalles de un contenedor específico por su ID.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Contenedor encontrado",
            content = @Content(schema = @Schema(implementation = ContenedorDto.class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Contenedor no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContenedorDto> obtenerContenedorPorId(
            @Parameter(description = "ID del contenedor", example = "1", required = true) 
            @PathVariable Integer id) {
        return contenedorService.buscarPorId(id)
                .map(contenedorDto -> ResponseEntity.ok().body(contenedorDto))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Obtener contenedores pendientes de entrega",
        description = """
            Retorna todos los contenedores que están pendientes de entrega.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de contenedores pendientes obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = ContenedorDto[].class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/pendientes-entrega")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContenedorDto>> obtenerContenedoresPendientesDeEntrega() {
        List<ContenedorDto> contenedores = contenedorService.buscarContenedoresPendientesDeEntrega();
        return ResponseEntity.ok(contenedores);
    }

    @Operation(
        summary = "Obtener contenedores en depósito",
        description = """
            Retorna todos los contenedores que están actualmente en depósito.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de contenedores en depósito obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = ContenedorDto[].class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/en-deposito")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContenedorDto>> obtenerContenedoresEnDeposito() {
        List<ContenedorDto> contenedores = contenedorService.buscarContenedoresEnDeposito();
        return ResponseEntity.ok(contenedores);
    }

    @Operation(
        summary = "Obtener contenedores en viaje",
        description = """
            Retorna todos los contenedores que están actualmente en viaje.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de contenedores en viaje obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = ContenedorDto[].class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/en-viaje")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContenedorDto>> obtenerContenedoresEnViaje() {
        List<ContenedorDto> contenedores = contenedorService.buscarContenedoresEnViaje();
        return ResponseEntity.ok(contenedores);
    }

    @Operation(
        summary = "Obtener contenedores por estado",
        description = """
            Retorna todos los contenedores que tienen un estado específico.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de contenedores por estado obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = ContenedorDto[].class))
        ),
        @ApiResponse(responseCode = "400", description = "Estado no válido"),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "No se encontraron contenedores con ese estado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContenedorDto>> obtenerContenedoresPorEstado(
            @Parameter(description = "Estado del contenedor", example = "En Viaje", required = true) 
            @PathVariable String estado) {
        List<ContenedorDto> contenedores = contenedorService.buscarPorEstado(estado);
        return ResponseEntity.ok(contenedores);
    }
}