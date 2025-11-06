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

import ar.edu.utn.frc.backend.solicitudes.dto.ClienteDto;
import ar.edu.utn.frc.backend.solicitudes.services.ClienteService;
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
    
    @Autowired
    ClienteService clienteService;

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
        List<ClienteDto> clientes = clienteService.buscarTodos();
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
        ClienteDto clienteGuardado = clienteService.guardarCliente(clienteDto);
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
        return clienteService.actualizarCliente(id, clienteDto)
                .map(clienteActualizado -> ResponseEntity.ok().body(clienteActualizado))
                .orElse(ResponseEntity.notFound().build());
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
        return clienteService.buscarPorId(id)
                .map(clienteDto -> ResponseEntity.ok().body(clienteDto))
                .orElse(ResponseEntity.notFound().build());
    }
}