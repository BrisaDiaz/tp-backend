package ar.edu.utn.frc.backend.solicitudes.controllers;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(ContenedorController.class);

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
        logger.info("GET /api/contenedores: Solicitud para obtener todos los contenedores.");
        List<ContenedorDto> contenedores = contenedorService.buscarTodos();
        logger.info("Se encontraron {} contenedores.", contenedores.size());
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
        logger.info("GET /api/contenedores/{}: Solicitud para obtener contenedor por ID.", id);
        
        Optional<ContenedorDto> contenedorDto = contenedorService.buscarPorId(id);

        if (contenedorDto.isPresent()) {
            logger.debug("Contenedor con ID {} encontrado.", id);
            return ResponseEntity.ok().body(contenedorDto.get());
        } else {
            logger.warn("Contenedor con ID {} no encontrado.", id);
            return ResponseEntity.notFound().build();
        }
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
        logger.info("GET /api/contenedores/pendientes-entrega: Solicitud para obtener contenedores pendientes de entrega.");
        List<ContenedorDto> contenedores = contenedorService.buscarContenedoresPendientesDeEntrega();
        logger.info("Se encontraron {} contenedores pendientes de entrega.", contenedores.size());
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
        logger.info("GET /api/contenedores/en-deposito: Solicitud para obtener contenedores en depósito.");
        List<ContenedorDto> contenedores = contenedorService.buscarContenedoresEnDeposito();
        logger.info("Se encontraron {} contenedores en depósito.", contenedores.size());
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
        logger.info("GET /api/contenedores/en-viaje: Solicitud para obtener contenedores en viaje.");
        List<ContenedorDto> contenedores = contenedorService.buscarContenedoresEnViaje();
        logger.info("Se encontraron {} contenedores en viaje.", contenedores.size());
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
        logger.info("GET /api/contenedores/estado/{}: Solicitud para obtener contenedores por estado.", estado);
        List<ContenedorDto> contenedores = contenedorService.buscarPorEstado(estado);
        logger.info("Se encontraron {} contenedores con el estado {}.", contenedores.size(), estado);
        return ResponseEntity.ok(contenedores);
    }
}