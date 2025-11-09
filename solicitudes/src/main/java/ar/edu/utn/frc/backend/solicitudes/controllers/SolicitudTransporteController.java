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

	private static final Logger logger = LoggerFactory.getLogger(SolicitudTransporteController.class);

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
		logger.info("GET /api/solicitudes: Solicitud para buscar todas las solicitudes.");
		List<SolicitudTransporteDto> solicitudes = solicitudService.buscarTodos();
		logger.info("Se encontraron {} solicitudes.", solicitudes.size());
		return ResponseEntity.ok(solicitudes);
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
		logger.info("GET /api/solicitudes/{}: Solicitud para buscar solicitud por ID.", id);
		
		Optional<SolicitudTransporteDto> solicitudDto = solicitudService.buscarPorId(id);

		if (solicitudDto.isPresent()) {
			logger.debug("Solicitud con ID {} encontrada.", id);
			return ResponseEntity.ok(solicitudDto.get());
		} else {
			logger.warn("Solicitud con ID {} no encontrada.", id);
			return ResponseEntity.notFound().build();
		}
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
    @PreAuthorize("hasRole('ADMIN') or @solicitudService.esClienteAutorizado(#postDto.idCliente, authentication.principal.claims['sub'])")
	public ResponseEntity<SolicitudTransporteDto> crearSolicitud(
			@Valid @RequestBody SolicitudTransportePostDto postDto) {
		logger.info("POST /api/solicitudes: Solicitud para crear una nueva solicitud desde cliente: {}", postDto.getIdCliente());
		
		SolicitudTransporteDto solicitudGuardada = solicitudService.guardarSolicitud(postDto);
		
		logger.info("Solicitud creada exitosamente con ID: {}", solicitudGuardada.getId());
		return status(HttpStatus.CREATED).body(solicitudGuardada);
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
		logger.info("GET /api/solicitudes/borrador: Solicitud para buscar solicitudes en estado Borrador.");
		List<SolicitudTransporteDto> borradores = solicitudService.buscarBorradores();
		logger.info("Se encontraron {} solicitudes en borrador.", borradores.size());
		return ResponseEntity.ok(borradores);
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
		logger.info("PUT /api/solicitudes/{}/programada: Solicitud para programar solicitud con costo {} y tiempo {} min.", id, dto.getCosto(), dto.getTiempo());
		
		SolicitudTransporteDto solicitudActualizada = solicitudService.actualizarEstadoAProgramada(id, dto.getCosto(), dto.getTiempo());
		
		logger.info("Solicitud {} actualizada a estado Programada.", id);
		return ResponseEntity.ok(solicitudActualizada);
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
		logger.info("PUT /api/solicitudes/{}/en-transito: Solicitud para marcar solicitud como En Tránsito.", id);
		
		SolicitudTransporteDto solicitudActualizada = solicitudService.actualizarEstadoAEnTransito(id);

		logger.info("Solicitud {} actualizada a estado En Tránsito.", id);
		return ResponseEntity.ok(solicitudActualizada);
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
		logger.info("PUT /api/solicitudes/{}/entregada: Solicitud para marcar solicitud como Entregada con costo real {} y tiempo real {} min.", id, dto.getCosto(), dto.getTiempo());
		
		SolicitudTransporteDto solicitudActualizada = solicitudService.actualizarEstadoAEntregada(id, dto.getCosto(), dto.getTiempo());

		logger.info("Solicitud {} actualizada a estado Entregada.", id);
		return ResponseEntity.ok(solicitudActualizada);
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
		logger.info("PUT /api/solicitudes/{}/contenedor/en-viaje: Solicitud para marcar contenedor de solicitud {} como En Viaje desde depósito: {}.", id, id, dto.getNombre());
		
		Optional<ContenedorDto> contenedorActualizado = solicitudService.actualizarContenedorAEnViaje(id, dto.getNombre());

		if (contenedorActualizado.isPresent()) {
			logger.info("Contenedor de solicitud {} marcado como En Viaje desde depósito {}.", id, dto.getNombre());
			return ResponseEntity.ok(contenedorActualizado.get());
		} else {
			logger.warn("No se pudo marcar contenedor como En Viaje para solicitud {} o depósito {} no válido.", id, dto.getNombre());
			return ResponseEntity.notFound().build();
		}
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
		logger.info("PUT /api/solicitudes/{}/contenedor/en-deposito: Solicitud para marcar contenedor de solicitud {} como En Depósito en: {}.", id, id, dto.getNombre());
		
		Optional<ContenedorDto> contenedorActualizado = solicitudService.actualizarContenedorAEnDeposito(id, dto.getNombre());

		if (contenedorActualizado.isPresent()) {
			logger.info("Contenedor de solicitud {} marcado como En Depósito en {}.", id, dto.getNombre());
			return ResponseEntity.ok(contenedorActualizado.get());
		} else {
			logger.warn("No se pudo marcar contenedor como En Depósito para solicitud {} o depósito {} no válido.", id, dto.getNombre());
			return ResponseEntity.notFound().build();
		}
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
		logger.info("GET /api/solicitudes/{}/contenedor/seguimiento: Solicitud para obtener seguimiento del contenedor de solicitud {}.", id, id);
		List<HistoricoEstadoContenedorDto> seguimiento = solicitudService.obtenerSeguimientoContenedor(id);
		logger.info("Se recuperaron {} registros de seguimiento para la solicitud {}.", seguimiento.size(), id);
		return ResponseEntity.ok(seguimiento);
	}
}