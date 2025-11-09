package ar.edu.utn.frc.backend.recursos.controllers;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.backend.recursos.dto.CargoGestionDto;
import ar.edu.utn.frc.backend.recursos.dto.PrecioCombustibleDto;
import ar.edu.utn.frc.backend.recursos.services.CargoGestionService;
import ar.edu.utn.frc.backend.recursos.services.PrecioCombustibleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tarifas")
@Tag(name = "4. Gestión de Tarifas", description = "APIs para gestionar tarifas del sistema - Combustible y cargos de gestión")
public class TarifaController {

    private static final Logger logger = LoggerFactory.getLogger(TarifaController.class);

    @Autowired
    private PrecioCombustibleService combustibleService;
    @Autowired
    private CargoGestionService cargoGestionService;

    // --- Endpoints de Precio Combustible ---

    @Operation(
        summary = "Establecer nuevo precio de combustible",
        description = """
            Crea un nuevo precio de combustible y finaliza la vigencia del anterior.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201", 
            description = "Precio de combustible creado exitosamente",
            content = @Content(schema = @Schema(implementation = PrecioCombustibleDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN")
    })
    @PostMapping("/combustible")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PrecioCombustibleDto> setPrecioCombustible(@Valid @RequestBody PrecioCombustibleDto dto) {
        logger.info("POST /api/tarifas/combustible: Intentando establecer nuevo precio de combustible con valor: {}", dto.getPrecioPorLitro());
        
        PrecioCombustibleDto newPrice = combustibleService.guardarNuevoPrecio(dto);
        
        logger.info("Nuevo precio de combustible establecido con ID: {}", newPrice.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(newPrice);
    }

    @Operation(
        summary = "Obtener precio actual de combustible",
        description = """
            Retorna el precio vigente de combustible.
            
            **Acceso:** Público (sin autenticación requerida)
            """,
        security = {}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Precio de combustible obtenido exitosamente",
            content = @Content(schema = @Schema(implementation = PrecioCombustibleDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "No hay precio de combustible vigente")
    })
    @GetMapping("/combustible")
    public ResponseEntity<PrecioCombustibleDto> getPrecioCombustibleActual() {
        logger.debug("GET /api/tarifas/combustible: Buscando precio de combustible vigente.");

        Optional<PrecioCombustibleDto> precioVigente = combustibleService.buscarPrecioVigente();

        if (precioVigente.isPresent()) {
            logger.info("Precio de combustible vigente encontrado.");
            return ResponseEntity.ok(precioVigente.get());
        } else {
            logger.warn("No se encontró precio de combustible vigente.");
            return ResponseEntity.notFound().build();
        }
    }

    // --- Endpoints de Cargo de Gestión ---

    @Operation(
        summary = "Establecer nuevo cargo de gestión",
        description = """
            Crea un nuevo cargo de gestión y finaliza la vigencia del anterior.
            
            **Roles permitidos:** ADMIN
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201", 
            description = "Cargo de gestión creado exitosamente",
            content = @Content(schema = @Schema(implementation = CargoGestionDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
        @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN")
    })
    @PostMapping("/gestion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CargoGestionDto> setCargoGestion(@Valid @RequestBody CargoGestionDto dto) {
        logger.info("POST /api/tarifas/gestion: Intentando establecer nuevo cargo de gestión con valor: {}", dto.getCostoPorTramo());

        CargoGestionDto newCargo = cargoGestionService.guardarNuevoCargo(dto);
        
        logger.info("Nuevo cargo de gestión establecido con ID: {}", newCargo.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(newCargo);
    }

    @Operation(
        summary = "Obtener cargo actual de gestión",
        description = """
            Retorna el cargo vigente de gestión por tramo.
            
            **Acceso:** Público (sin autenticación requerida)
            """,
        security = {}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Cargo de gestión obtenido exitosamente",
            content = @Content(schema = @Schema(implementation = CargoGestionDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "No hay cargo de gestión vigente")
    })
    @GetMapping("/gestion")
    public ResponseEntity<CargoGestionDto> getCargoGestionActual() {
        logger.debug("GET /api/tarifas/gestion: Buscando cargo de gestión vigente.");

        Optional<CargoGestionDto> cargoVigente = cargoGestionService.buscarCargoVigente();

        if (cargoVigente.isPresent()) {
            logger.info("Cargo de gestión vigente encontrado.");
            return ResponseEntity.ok(cargoVigente.get());
        } else {
            logger.warn("No se encontró cargo de gestión vigente.");
            return ResponseEntity.notFound().build();
        }
    }
}