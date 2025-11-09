package ar.edu.utn.frc.backend.recursos.controllers;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frc.backend.recursos.dto.CiudadDto;
import ar.edu.utn.frc.backend.recursos.services.CiudadService;
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
@RequestMapping("/api/ciudades")
@Tag(name = "2. Gestión de Ciudades", description = "APIs para gestionar ciudades - Altas, bajas, modificaciones y consultas")
@SecurityRequirement(name = "bearerAuth")
public class CiudadController {

  @Autowired
  private CiudadService ciudadService;

  private static final Logger logger = LoggerFactory.getLogger(CiudadController.class);


  @Operation(
    summary = "Obtener todas las ciudades",
    description = """
      Retorna la lista completa de ciudades del sistema.
      
      **Roles permitidos:** ADMIN
      """,
    security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200", 
      description = "Lista de ciudades obtenida exitosamente",
      content = @Content(schema = @Schema(implementation = CiudadDto[].class))
    ),
    @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
    @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
  })
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<CiudadDto>> obtenerTodasLasCiudades() {
        logger.info("INICIO - GET /api/ciudades. Obteniendo todas las ciudades.");
    List<CiudadDto> ciudades = ciudadService.buscarTodos();
        logger.info("FIN - GET /api/ciudades. Total de {} ciudades encontradas.", ciudades.size());
    return ResponseEntity.ok(ciudades);
  }

  @Operation(
    summary = "Buscar ciudad por código postal",
    description = """
      Busca una ciudad por su código postal.
      
      **Roles permitidos:** ADMIN
      """,
    security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200", 
      description = "Ciudad encontrada",
      content = @Content(schema = @Schema(implementation = CiudadDto.class))
    ),
    @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
    @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
    @ApiResponse(responseCode = "404", description = "Ciudad no encontrada")
  })
  @GetMapping("/buscar")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CiudadDto> obtenerCiudadPorCodigoPostal(
      @Parameter(description = "Código postal de la ciudad", example = "X5000", required = true) 
      @RequestParam String codigoPostal) {
        logger.info("INICIO - GET /api/ciudades/buscar?codigoPostal={}. Buscando ciudad por CP.", codigoPostal);
    Optional<CiudadDto> ciudadOpt = ciudadService.buscarPorCodigoPostal(codigoPostal);
        if (ciudadOpt.isPresent()) {
            logger.info("FIN - GET /api/ciudades/buscar. Ciudad encontrada para CP {}.", codigoPostal);
        } else {
            logger.warn("FIN - GET /api/ciudades/buscar. Ciudad no encontrada para CP {} (404).", codigoPostal);
        }
    return ciudadOpt.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @Operation(
    summary = "Obtener ciudad por ID",
    description = """
      Retorna los detalles de una ciudad específica por su ID.
      
      **Roles permitidos:** ADMIN
      """,
    security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200", 
      description = "Ciudad encontrada",
      content = @Content(schema = @Schema(implementation = CiudadDto.class))
    ),
    @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
    @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
    @ApiResponse(responseCode = "404", description = "Ciudad no encontrada")
  })
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CiudadDto> obtenerCiudadPorId(
      @Parameter(description = "ID de la ciudad", example = "1", required = true) 
      @PathVariable Integer id) {
        logger.info("INICIO - GET /api/ciudades/{}. Buscando ciudad por ID.", id);
    Optional<CiudadDto> ciudadOpt = ciudadService.buscarPorId(id);
        if (ciudadOpt.isPresent()) {
            logger.info("FIN - GET /api/ciudades/{}. Ciudad encontrada.", id);
        } else {
            logger.warn("FIN - GET /api/ciudades/{}. Ciudad no encontrada (404).", id);
        }
    return ciudadOpt.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @Operation(
    summary = "Crear nueva ciudad",
    description = """
      Da de alta una nueva ciudad en el sistema.
      
      **Roles permitidos:** ADMIN
      """,
    security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "201", 
      description = "Ciudad creada exitosamente",
      content = @Content(schema = @Schema(implementation = CiudadDto.class))
    ),
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
    @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
    @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
    @ApiResponse(responseCode = "409", description = "Conflicto - La ciudad o código postal ya existe")
  })
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CiudadDto> crearCiudad(@Valid @RequestBody CiudadDto ciudadDto) {
        logger.info("INICIO - POST /api/ciudades. Creando ciudad: {} (CP: {}).", ciudadDto.getNombre(), ciudadDto.getCodigoPostal());
    CiudadDto nuevaCiudad = ciudadService.guardarCiudad(ciudadDto);
        logger.info("FIN - POST /api/ciudades. Ciudad ID {} creada con éxito.", nuevaCiudad.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCiudad);
  }

  @Operation(
    summary = "Actualizar ciudad",
    description = """
      Actualiza los datos de una ciudad existente.
      
      **Roles permitidos:** ADMIN
      """,
    security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200", 
      description = "Ciudad actualizada exitosamente",
      content = @Content(schema = @Schema(implementation = CiudadDto.class))
    ),
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
    @ApiResponse(responseCode = "401", description = "No autenticado - Token JWT inválido o faltante"),
    @ApiResponse(responseCode = "403", description = "No autorizado - Se requiere rol ADMIN"),
    @ApiResponse(responseCode = "404", description = "Ciudad no encontrada"),
    @ApiResponse(responseCode = "409", description = "Conflicto - El código postal ya existe")
  })
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CiudadDto> actualizarCiudad(
      @Parameter(description = "ID de la ciudad a actualizar", example = "1", required = true) 
      @PathVariable Integer id, 
      
      @Valid @RequestBody CiudadDto ciudadDto) {
        logger.info("INICIO - PUT /api/ciudades/{}. Actualizando ciudad a: {} (CP: {}).", id, ciudadDto.getNombre(), ciudadDto.getCodigoPostal());
    Optional<CiudadDto> ciudadActualizada = ciudadService.actualizarCiudad(id, ciudadDto);
        if (ciudadActualizada.isPresent()) {
            logger.info("FIN - PUT /api/ciudades/{}. Ciudad actualizada con éxito.", id);
        } else {
            logger.warn("FIN - PUT /api/ciudades/{}. Ciudad no encontrada para actualizar (404).", id);
        }
    return ciudadActualizada.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }
}