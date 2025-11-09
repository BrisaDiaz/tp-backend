package ar.edu.utn.frc.backend.solicitudes.controllers;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import ar.edu.utn.frc.backend.solicitudes.dto.ClienteDto;
import ar.edu.utn.frc.backend.solicitudes.dto.SolicitudTransporteDto;
import ar.edu.utn.frc.backend.solicitudes.services.ClienteService;
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
@RequestMapping("/api/clientes")
@Tag(name = "1. Gestión de Clientes", description = "APIs para gestionar clientes - Altas, modificaciones y consultas")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {
    
    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);

    @Autowired
    ClienteService clienteService;

    @Autowired
    private SolicitudTransporteService solicitudTransporteService;

    @Operation(
        summary = "Obtener todos los clientes",
        description = """
            Retorna la lista completa de clientes del sistema.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de clientes obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = ClienteDto[].class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClienteDto>> buscarTodosLosClientes() {
        logger.info("GET /api/clientes: Solicitud para buscar todos los clientes.");
        List<ClienteDto> clientes = clienteService.buscarTodos();
        logger.info("Se encontraron {} clientes.", clientes.size());
        return ResponseEntity.ok(clientes);
    }

    @Operation(
        summary = "Crear nuevo cliente",
        description = """
            Da de alta un nuevo cliente en el sistema.
            
            **Acceso:** Público (sin autenticación requerida)
            """,
        security = {}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201", 
            description = "Cliente creado exitosamente",
            content = @Content(schema = @Schema(implementation = ClienteDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Conflicto - El email o DNI ya existen")
    })
    @PostMapping
    public ResponseEntity<ClienteDto> guardarCliente(
            @Valid @RequestBody ClienteDto clienteDto) {
        logger.info("POST /api/clientes: Solicitud para crear un nuevo cliente con DNI: {}", clienteDto.getDni());
        
        ClienteDto clienteGuardado = clienteService.guardarCliente(clienteDto);
        
        logger.info("Cliente creado exitosamente con ID: {}", clienteGuardado.getId());
        return status(HttpStatus.CREATED).body(clienteGuardado);
    }

    @Operation(
        summary = "Actualizar cliente",
        description = """
            Actualiza los datos de un cliente existente.
            
            **Roles permitidos:** ADMIN o el propio cliente
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Cliente actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = ClienteDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - No es propietario del cliente o no tiene rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto - El email o DNI ya existen en otro cliente")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @clienteService.esPropietario(#id, authentication.principal.claims['sub'])")
    public ResponseEntity<ClienteDto> actualizarCliente(
            @Parameter(description = "ID del cliente a actualizar", example = "1", required = true) 
            @PathVariable Integer id,
            
            @Valid @RequestBody ClienteDto clienteDto) {
        logger.info("PUT /api/clientes/{}: Solicitud para actualizar cliente con ID.", id);

        Optional<ClienteDto> clienteActualizado = clienteService.actualizarCliente(id, clienteDto);

        if (clienteActualizado.isPresent()) {
            logger.info("Cliente con ID {} actualizado exitosamente.", id);
            return ResponseEntity.ok().body(clienteActualizado.get());
        } else {
            logger.warn("No se pudo encontrar el cliente con ID {} para actualizar.", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Obtener cliente por ID",
        description = """
            Retorna los detalles de un cliente específico por su ID.
            
            **Roles permitidos:** ADMIN o el propio cliente
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Cliente encontrado",
            content = @Content(schema = @Schema(implementation = ClienteDto.class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - No es propietario del cliente o no tiene rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @clienteService.esPropietario(#id, authentication.principal.claims['sub'])")
    public ResponseEntity<ClienteDto> buscarClientePorId(
            @Parameter(description = "ID del cliente", example = "1", required = true) 
            @PathVariable Integer id) {
        logger.info("GET /api/clientes/{}: Solicitud para buscar cliente por ID.", id);

        Optional<ClienteDto> clienteDto = clienteService.buscarPorId(id);

        if (clienteDto.isPresent()) {
            logger.debug("Cliente con ID {} encontrado.", id);
            return ResponseEntity.ok().body(clienteDto.get());
        } else {
            logger.warn("Cliente con ID {} no encontrado.", id);
            return ResponseEntity.notFound().build();
        }
    }

        @Operation(
        summary = "Obtener Solicitudes de Transporte por Cliente",
        description = "Retorna una lista de todas las Solicitudes de Transporte que pertenecen al cliente especificado por su ID.",
        tags = { "Clientes" },
        parameters = {
            @Parameter(name = "id", description = "ID del Cliente", required = true, example = "1")
        },
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Solicitudes encontradas exitosamente.",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SolicitudTransporteDto.class) // Usamos la clase DTO para el esquema
                )
            ),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado (aunque el servicio devuelve lista vacía, se documenta el contexto)", content = @Content)
        }
    )
    /**
     * GET /clientes/{id}/solicitudes
     * Retorna todas las solicitudes asociadas a un cliente específico.
     * @param id ID del cliente.
     * @return Lista de SolicitudTransporteDto.
     */
    @GetMapping("/{id}/solicitudes")
    public ResponseEntity<List<SolicitudTransporteDto>> obtenerSolicitudesPorClienteId(@PathVariable("id") Integer id) {
        logger.info("Recibida solicitud para obtener todas las Solicitudes de Transporte para Cliente ID: {}", id);
        
        List<SolicitudTransporteDto> solicitudes = solicitudTransporteService.buscarPorClienteId(id);
        
        // Retornar 200 OK con una lista vacía si no hay solicitudes, que es lo estándar.
        logger.info("Retornando {} solicitudes para Cliente ID: {}", solicitudes.size(), id);
        return ResponseEntity.ok(solicitudes);
    }
}