package ar.edu.utn.frc.backend.recursos.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.backend.recursos.dto.DepositoDto;
import ar.edu.utn.frc.backend.recursos.services.DepositoService;
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
@RequestMapping("/api/depositos")
@Tag(name = "3. Gestión de Depósitos", description = "APIs para gestionar depósitos - Altas, bajas, modificaciones y consultas")
@SecurityRequirement(name = "bearerAuth")
public class DepositoController {
    
    @Autowired
    private DepositoService depositoService;

    @Operation(
        summary = "Obtener todos los depósitos",
        description = """
            Retorna la lista completa de depósitos del sistema.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de depósitos obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = DepositoDto[].class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DepositoDto>> obtenerTodosLosDepositos() {
        List<DepositoDto> depositos = depositoService.buscarTodosLosDepositos();
        return ResponseEntity.ok().body(depositos);
    }

    @Operation(
        summary = "Obtener depósito por ID",
        description = """
            Retorna los detalles de un depósito específico por su ID.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Depósito encontrado",
            content = @Content(schema = @Schema(implementation = DepositoDto.class))
        ),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Depósito no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepositoDto> obtenerDepositoPorId(
            @Parameter(description = "ID del depósito", example = "1", required = true) 
            @PathVariable Integer id) {
        return depositoService.buscarPorId(id)
                .map(depositoDto -> ResponseEntity.ok().body(depositoDto))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Crear nuevo depósito",
        description = """
            Da de alta un nuevo depósito en el sistema.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201", 
            description = "Depósito creado exitosamente",
            content = @Content(schema = @Schema(implementation = DepositoDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Ciudad no encontrada"),
        @ApiResponse(responseCode = "409", description = "Conflicto - El depósito ya existe")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepositoDto> agregarDeposito(@Valid @RequestBody DepositoDto depositoDto) {
        DepositoDto depositoGuardado = depositoService.guardarDeposito(depositoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(depositoGuardado);
    }

    @Operation(
        summary = "Actualizar depósito",
        description = """
            Actualiza los datos de un depósito existente.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Depósito actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = DepositoDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Depósito no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto - Los datos ya existen")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepositoDto> actualizarDeposito(
            @Parameter(description = "ID del depósito a actualizar", example = "1", required = true) 
            @PathVariable Integer id, 
            
            @Valid @RequestBody DepositoDto depositoDto) {
        return depositoService.actualizarDeposito(id, depositoDto)
                .map(depositoActualizado -> ResponseEntity.ok().body(depositoActualizado))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(
        summary = "Eliminar depósito",
        description = """
            Elimina un depósito del sistema.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Depósito eliminado exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "Depósito no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto - El depósito no puede ser eliminado (tiene relaciones activas)")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarDeposito(
            @Parameter(description = "ID del depósito a eliminar", example = "1", required = true) 
            @PathVariable Integer id) {
        depositoService.eliminarDeposito(id);
        return ResponseEntity.noContent().build();
    }
}