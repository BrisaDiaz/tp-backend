package ar.edu.utn.frc.backend.logistica.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import ar.edu.utn.frc.backend.logistica.controllers.RutaController;
import ar.edu.utn.frc.backend.logistica.dto.DepositoDto;
import ar.edu.utn.frc.backend.logistica.dto.RutaTentativaDto;
import ar.edu.utn.frc.backend.logistica.dto.TramoDto;
import ar.edu.utn.frc.backend.logistica.entities.Camion;
import ar.edu.utn.frc.backend.logistica.entities.Deposito;
import ar.edu.utn.frc.backend.logistica.entities.Estado;
import ar.edu.utn.frc.backend.logistica.entities.Ruta;
import ar.edu.utn.frc.backend.logistica.entities.TipoTramo;
import ar.edu.utn.frc.backend.logistica.entities.Tramo;
import ar.edu.utn.frc.backend.logistica.exceptions.DataConflictException;
import ar.edu.utn.frc.backend.logistica.exceptions.ResourceNotFoundException;
import ar.edu.utn.frc.backend.logistica.repositories.CamionRepository;
import ar.edu.utn.frc.backend.logistica.repositories.DepositoRepository;
import ar.edu.utn.frc.backend.logistica.repositories.EstadoRepository;
import ar.edu.utn.frc.backend.logistica.repositories.TramoRepository;
import ar.edu.utn.frc.backend.logistica.restClient.RecursosClient;
import ar.edu.utn.frc.backend.logistica.restClient.SolicitudesClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TramoService {

    private static final String ESTADO_ESTIMADO = "Estimado";
    private static final String ESTADO_ASIGNADO = "Asignado";
    private static final String ESTADO_INICIADO = "Iniciado";
    private static final String ESTADO_FINALIZADO = "Finalizado";
    private static final int SCALE = 2;

    @Autowired
    private TramoRepository tramoRepository;

    @Autowired
    private CamionRepository camionRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    @Autowired
    private DepositoRepository depositoRepository;

    @Autowired
    private RecursosClient recursosClient;

    @Autowired
    private SolicitudesClient solicitudesClient;

    @Autowired
    private ModelMapper modelMapper;

    private static final Logger logger = LoggerFactory.getLogger(RutaController.class);

    /**
     * Crear tramos a partir de una ruta tentativa
     */
    @Transactional
    public void crearTramosDesdeRutaTentativa(Ruta ruta, RutaTentativaDto rutaTentativa) {
        logger.info("Iniciando creación de tramos para ruta ID: {}", ruta.getId());
        
        Optional<Estado> estadoEstimado = estadoRepository.findByNombre(ESTADO_ESTIMADO);
        if (estadoEstimado.isEmpty()) {
            logger.error("Estado '{}' no encontrado en la base de datos.", ESTADO_ESTIMADO);
            throw new ResourceNotFoundException("Estado '" + ESTADO_ESTIMADO + "' no encontrado en la base de datos");
        }
        logger.debug("Estado inicial 'Estimado' obtenido.");

        List<Tramo> tramos = rutaTentativa.getTramos().stream()
                .map(tramoTentativo -> {
                    Tramo tramo = new Tramo();
                    tramo.setNroOrden(tramoTentativo.getNroOrden());
                    tramo.setCostoEstimado(tramoTentativo.getCostoEstimado());
                    tramo.setTiempoEstimado(tramoTentativo.getTiempoEstimadoSegundos());
                    tramo.setDistanciaKm(tramoTentativo.getDistanciaKilometros());
                    
                    // Determinar tipo de tramo basado en la posición
                    TipoTramo tipoTramo = determinarTipoTramo(
                        tramoTentativo.getNroOrden(), 
                        rutaTentativa.getCantidadTramos(),
                        tramoTentativo.getOrigen().getId().equals(ruta.getSolicitud().getDepositoOrigen().getId()),
                        tramoTentativo.getDestino().getId().equals(ruta.getSolicitud().getDepositoDestino().getId())
                    );
                    tramo.setTipoTramo(tipoTramo);
                    
                    tramo.setEstado(estadoEstimado.get());
                    tramo.setRuta(ruta);
                    
                    // Buscar y asignar depósitos reales
                    Optional<Deposito> origenOpt = depositoRepository.findById(tramoTentativo.getOrigen().getId());
                    Optional<Deposito> destinoOpt = depositoRepository.findById(tramoTentativo.getDestino().getId());
                    
                    if (origenOpt.isPresent() && destinoOpt.isPresent()) {
                        tramo.setOrigen(origenOpt.get());
                        tramo.setDestino(destinoOpt.get());
                    } else {
                        String errorMsg = String.format("No se encontraron depósitos para el tramo %d. Origen ID: %d, Destino ID: %d", 
                                tramoTentativo.getNroOrden(), 
                                tramoTentativo.getOrigen().getId(), 
                                tramoTentativo.getDestino().getId());
                        logger.error(errorMsg);
                        throw new ResourceNotFoundException(errorMsg);
                    }
                    
                    logger.debug("Tramo Tentativo procesado {} (Tipo: {}): {} -> {} | Costo: {}", 
                                tramo.getNroOrden(), 
                                tramo.getTipoTramo(),
                                tramo.getOrigen().getNombre(),
                                tramo.getDestino().getNombre(),
                                tramo.getCostoEstimado());
                    
                    return tramo;
                })
                .collect(Collectors.toList());

        tramoRepository.saveAll(tramos);
        logger.info("Finalizada la creación. Persistidos {} tramos para ruta ID: {}", tramos.size(), ruta.getId());
    }

    /**
     * Método auxiliar para determinar el tipo de tramo
     */
    private TipoTramo determinarTipoTramo(Integer nroOrden, Integer totalTramos, boolean esOrigen, boolean esDestino) {
        if (totalTramos == 1) {
            return TipoTramo.ORIGEN_DESTINO;
        } else if (nroOrden == 1) {
            return TipoTramo.ORIGEN_DEPOSITO;
        } else if (nroOrden.equals(totalTramos)) {
            return TipoTramo.DEPOSITO_DESTINO;
        } else {
            return TipoTramo.DEPOSITO_DEPOSITO;
        }
    }

    // Buscar todos los tramos por estado
    public List<TramoDto> buscarPorEstado(String estado) {
        logger.info("Buscando tramos con estado: {}", estado);
        List<Tramo> tramos = tramoRepository.findByEstadoNombre(estado);
        
        if (tramos.isEmpty()) {
            logger.warn("No se encontraron tramos con estado: {}", estado);
            throw new ResourceNotFoundException("No se encontraron tramos con estado: " + estado);
        }
        
        logger.info("Encontrados {} tramos con estado: {}", tramos.size(), estado);
        return tramos.stream()
                .map(this::mapTramoToDto)
                .collect(Collectors.toList());
    }

    /**
     * Buscar todos los tramos asociados a una ruta (Retorna DTOs)
     */
    public List<TramoDto> buscarPorRuta(Integer idRuta) {
        logger.info("Buscando tramos para ruta ID: {}", idRuta);
        List<Tramo> tramos = tramoRepository.findByIdRuta(idRuta);
        
        if (tramos.isEmpty()) {
            logger.debug("No se encontraron tramos para la ruta ID: {}", idRuta);
            return List.of(); // Retorna lista vacía si no encuentra (más seguro para lógica interna)
        }
        
        logger.info("Encontrados {} tramos para ruta ID: {}", tramos.size(), idRuta);
        return tramos.stream()
                .map(this::mapTramoToDto)
                .collect(Collectors.toList());
    }

    // Asignar camión a un tramo
    @Transactional
    public Optional<TramoDto> asignarCamion(Integer idTramo, Integer idCamion) {
        logger.info("Iniciando asignación de Camión ID: {} al Tramo ID: {}", idCamion, idTramo);
        Optional<Tramo> optTramo = tramoRepository.findById(idTramo);
        Optional<Camion> optCamion = camionRepository.findById(idCamion);
        Optional<Estado> optEstado = estadoRepository.findByNombre(ESTADO_ASIGNADO);

        if (optTramo.isEmpty()) {
            logger.error("Tramo no encontrado con ID: {}", idTramo);
            throw new ResourceNotFoundException("Tramo no encontrado con ID: " + idTramo);
        }
        if (optCamion.isEmpty()) {
            logger.error("Camión no encontrado con ID: {}", idCamion);
            throw new ResourceNotFoundException("Camión no encontrado con ID: " + idCamion);
        }
        if (optEstado.isEmpty()) {
            logger.error("Estado '{}' no encontrado", ESTADO_ASIGNADO);
            throw new ResourceNotFoundException("Estado '" + ESTADO_ASIGNADO + "' no encontrado");
        }

        Tramo tramo = optTramo.get();
        Camion camion = optCamion.get();
        logger.debug("Tramo (Estado: {}) y Camión obtenidos.", tramo.getEstado().getNombre());

        // ✅ VALIDACIÓN: Verificar que el tramo no tenga ya un camión asignado
        if (tramo.getCamion() != null) {
            logger.error("El tramo ID {} ya tiene un camión asignado (ID {})", idTramo, tramo.getCamion().getId());
            throw new DataConflictException("El tramo ID " + idTramo + " ya tiene un camión asignado");
        }

        // ✅ VALIDACIÓN: Verificar que el camión no esté ocupado
        if (!camion.getDisponibilidad()) {
            logger.error("El camión ID {} ya está ocupado", idCamion);
            throw new DataConflictException("El camión ID " + idCamion + " ya está ocupado");
        }
        
        // Asignación en BD Logística
        tramo.setCamion(camion);
        tramo.setEstado(optEstado.get());
        Tramo tramoAsignado = tramoRepository.save(tramo);
        logger.info("Tramo ID: {} actualizado a estado 'Asignado' con Camión ID: {}", idTramo, idCamion);

        try {
            // Actualización de estado del camión en BD Recursos (RestClient)
            recursosClient.setCamionOcupado(idCamion);
            logger.info("Estado de disponibilidad del Camión ID: {} actualizado a Ocupado en Recursos.", idCamion);
        } catch (RestClientException e) {
            logger.error("Error al actualizar estado del camión ID {} en Recursos: {}", idCamion, e.getMessage());
            // Se registra el error, pero se permite que la transacción local de Logística continúe.
        }

        return Optional.of(mapTramoToDto(tramoAsignado));
    }

    // Iniciar tramo
    @Transactional
    public Optional<TramoDto> iniciarTramo(Integer idTramo) {
        logger.info("Iniciando Tramo ID: {}", idTramo);
        Optional<Tramo> optTramo = tramoRepository.findById(idTramo);
        Optional<Estado> optEstado = estadoRepository.findByNombre(ESTADO_INICIADO);

        if (optTramo.isEmpty()) {
            logger.error("Tramo no encontrado con ID: {}", idTramo);
            throw new ResourceNotFoundException("Tramo no encontrado con ID: " + idTramo);
        }
        if (optEstado.isEmpty()) {
            logger.error("Estado '{}' no encontrado", ESTADO_INICIADO);
            throw new ResourceNotFoundException("Estado '" + ESTADO_INICIADO + "' no encontrado");
        }

        Tramo tramo = optTramo.get();

        // ✅ VALIDACIÓN: Verificar que el tramo esté en estado "Asignado"
        if (!tramo.getEstado().getNombre().equals(ESTADO_ASIGNADO)) {
            logger.error("El tramo ID {} no está en estado 'Asignado'. Estado actual: {}", idTramo, tramo.getEstado().getNombre());
            throw new DataConflictException("El tramo ID " + idTramo + " no está en estado 'Asignado'. Estado actual: " + tramo.getEstado().getNombre());
        }

        // ✅ VALIDACIÓN: Verificar que el tramo tenga camión asignado
        if (tramo.getCamion() == null) {
            logger.error("El tramo ID {} no tiene un camión asignado para iniciar", idTramo);
            throw new DataConflictException("El tramo ID " + idTramo + " no tiene un camión asignado para iniciar");
        }

        // Actualización en BD Logística
        tramo.setEstado(optEstado.get());
        tramo.setFechaHoraInicio(LocalDateTime.now());
        Tramo tramoIniciado = tramoRepository.save(tramo);
        logger.info("Tramo ID: {} actualizado a estado 'Iniciado' en fecha: {}", idTramo, tramo.getFechaHoraInicio());

        try {
            // Actualización de estado de la solicitud (RestClient)
            Integer idSolicitud = tramo.getRuta().getSolicitud().getId();
            String tipoTramo = tramo.getTipoTramo().name();
            String depositoOrigen = tramo.getOrigen().getNombre();

            if (tipoTramo.equals(TipoTramo.ORIGEN_DESTINO.name()) ||
                tipoTramo.equals(TipoTramo.ORIGEN_DEPOSITO.name())) {
                
                logger.debug("Tramo inicial de ruta, actualizando Solicitud ID: {} a En Tránsito", idSolicitud);
                solicitudesClient.actualizarSolicitudAEnTransito(idSolicitud);
            } else {
                logger.debug("Tramo intermedio, actualizando Contenedor ID: {} a En Viaje desde {}", tramo.getRuta().getSolicitud().getContenedor().getId(), depositoOrigen);
                solicitudesClient.actualizarContenedorEnViaje(idSolicitud, depositoOrigen);
            }
            logger.info("Tramo {} iniciado. Solicitud {} actualizada exitosamente.", idTramo, idSolicitud);

        } catch (RestClientException e) {
            logger.error("Error de comunicación con Solicitudes al iniciar tramo {}: {}", idTramo, e.getMessage());
        }

        return Optional.of(mapTramoToDto(tramoIniciado));
    }

    // Finalizar tramo
    @Transactional
    public Optional<TramoDto> finalizarTramo(Integer idTramo) {
        logger.info("Finalizando Tramo ID: {}", idTramo);
        Optional<Tramo> optTramo = tramoRepository.findById(idTramo);
        Optional<Estado> optEstado = estadoRepository.findByNombre(ESTADO_FINALIZADO);

        if (optTramo.isEmpty()) {
            logger.error("Tramo no encontrado con ID: {}", idTramo);
            throw new ResourceNotFoundException("Tramo no encontrado con ID: " + idTramo);
        }
        if (optEstado.isEmpty()) {
            logger.error("Estado '{}' no encontrado", ESTADO_FINALIZADO);
            throw new ResourceNotFoundException("Estado '" + ESTADO_FINALIZADO + "' no encontrado");
        }

        Tramo tramo = optTramo.get();

        // ✅ VALIDACIÓN: Verificar que el tramo esté en estado "Iniciado"
        if (!tramo.getEstado().getNombre().equals(ESTADO_INICIADO)) {
            logger.error("El tramo ID {} no está en estado 'Iniciado'. Estado actual: {}", idTramo, tramo.getEstado().getNombre());
            throw new DataConflictException("El tramo ID " + idTramo + " no está en estado 'Iniciado'. Estado actual: " + tramo.getEstado().getNombre());
        }

        // ✅ VALIDACIÓN: Verificar que tenga fecha de inicio
        if (tramo.getFechaHoraInicio() == null) {
            logger.error("El tramo ID {} no tiene fecha/hora de inicio registrada", idTramo);
            throw new DataConflictException("El tramo ID " + idTramo + " no tiene fecha/hora de inicio registrada");
        }

        // Actualización en BD Logística
        tramo.setEstado(optEstado.get());
        tramo.setFechaHoraFin(LocalDateTime.now());
        Tramo tramoFinalizado = tramoRepository.save(tramo);
        logger.info("Tramo ID: {} actualizado a estado 'Finalizado' en fecha: {}", idTramo, tramo.getFechaHoraFin());

        try {
            Integer idSolicitud = tramo.getRuta().getSolicitud().getId();
            String depositoDestino = tramo.getDestino().getNombre();
            String tipoTramo = tramo.getTipoTramo().name();
            
            // 1. Calcular costos y tiempo real del tramo y la ruta
            BigDecimal cargoPorGestion = recursosClient.getCargoPorGestion().getCostoPorTramo();
            BigDecimal costoCombustiblePorLitro = recursosClient.getCostoCombustiblePorLitro().getPrecioPorLitro();

            // Calcular tiempo real y costos del tramo actual
            Long tiempoRealTramo = Duration.between(tramo.getFechaHoraInicio(), tramo.getFechaHoraFin()).toSeconds();
            tramoFinalizado.setTiempoReal(tiempoRealTramo);

            Long diasEstadiaOrigen = 0L;
            // Solo se calcula estadía si no es el primer tramo (o ruta directa)
            if (tramo.getNroOrden() > 1 || tramo.getNroOrden() == 1 && tramo.getRuta().getCantidadTramos() > 1) {
                // Buscamos el tramo anterior
                Optional<Tramo> anteriorOpt = tramoRepository.findByRutaIdAndNroOrden(tramo.getRuta().getId(), tramo.getNroOrden() - 1);
                if (anteriorOpt.isPresent() && anteriorOpt.get().getFechaHoraFin() != null) {
                    Duration d = Duration.between(anteriorOpt.get().getFechaHoraFin(), tramo.getFechaHoraInicio());
                    diasEstadiaOrigen = (long) Math.ceil(d.toHours() / 24.0);
                    logger.debug("Estadía en depósito {} calculada: {} días", tramo.getOrigen().getNombre(), diasEstadiaOrigen);
                }
            }

            BigDecimal costoEstadia = tramo.getOrigen().getPrecioPorDia()
                    .multiply(BigDecimal.valueOf(diasEstadiaOrigen));
            BigDecimal costoKm = tramo.getCamion().getCostoPorKm()
                    .multiply(BigDecimal.valueOf(tramo.getDistanciaKm()));
            BigDecimal costoCombustible = BigDecimal.valueOf(tramo.getDistanciaKm())
                    .multiply(tramo.getCamion().getConsumoCombustiblePromedio())
                    .multiply(costoCombustiblePorLitro);
            
            // Cargo por gestión ya está incluido en el costo estimado, se recalcula aquí con valores reales
            BigDecimal costoRealTramo = costoEstadia.add(costoKm).add(costoCombustible).add(cargoPorGestion).setScale(SCALE, RoundingMode.HALF_UP);

            tramoFinalizado.setCostoReal(costoRealTramo);
            tramoRepository.save(tramoFinalizado);
            logger.info("Tramo ID: {} guardado con Costo Real: {} y Tiempo Real: {} segundos", idTramo, costoRealTramo, tiempoRealTramo);

            // 2. Liberar el camión
            recursosClient.setCamionLibre(tramo.getCamion().getId());
            logger.info("Camión ID: {} liberado en Recursos.", tramo.getCamion().getId());

            // 3. Actualizar estado de la solicitud (RestClient)
            if (tipoTramo.equals(TipoTramo.DEPOSITO_DESTINO.name()) ||
                tipoTramo.equals(TipoTramo.ORIGEN_DESTINO.name())) {
                
                // Fin de la ruta: Recalcular costo/tiempo total
                List<Tramo> tramosRutaFinalizados = tramoRepository.findByIdRuta(tramo.getRuta().getId())
                        .stream()
                        .filter(t -> t.getCostoReal() != null && t.getTiempoReal() != null)
                        .collect(Collectors.toList());

                BigDecimal costoTotalFinal = tramosRutaFinalizados.stream()
                        .map(Tramo::getCostoReal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                Long tiempoTotalSegundos = tramosRutaFinalizados.stream()
                        .map(Tramo::getTiempoReal)
                        .reduce(0L, Long::sum);

                logger.info("Fin de Ruta ID: {}. Solicitud ID: {} a Entregada. Costo Total Real: {}, Tiempo Total Real: {}s", 
                    tramo.getRuta().getId(), idSolicitud, costoTotalFinal, tiempoTotalSegundos);
                
                solicitudesClient.actualizarSolicitudAEntregada(idSolicitud, costoTotalFinal, tiempoTotalSegundos);
            } else {
                logger.debug("Tramo intermedio finalizado. Contenedor ID: {} actualizado a En Depósito: {}", tramo.getRuta().getSolicitud().getContenedor().getId(), depositoDestino);
                solicitudesClient.actualizarContenedorEnDeposito(idSolicitud, depositoDestino);
            }

        } catch (RestClientException e) {
            logger.error("Error al finalizar tramo o comunicar con microservicios (Solicitudes/Recursos): {}", e.getMessage());
            // Se registra el error, pero se permite que la transacción local continúe.
        } catch (Exception e) {
            logger.error("Error inesperado en finalizarTramo {}: {}", idTramo, e.getMessage(), e);
        }

        return Optional.of(mapTramoToDto(tramoFinalizado));
    }

    /**
     * Buscar todos los tramos ASIGNADOS a un camión específico.
     */
    public List<TramoDto> buscarTramosAsignadosCamion(Integer idCamion) {
        logger.info("Buscando tramos asignados/en curso para Camión ID: {}", idCamion);
        List<Tramo> tramos = tramoRepository.findByCamionAsignado(idCamion);
        
        if (tramos.isEmpty()) {
            logger.warn("No se encontraron tramos asignados al camión ID: {}", idCamion);
            throw new ResourceNotFoundException("No se encontraron tramos asignados al camión ID: " + idCamion);
        }
        
        logger.info("Encontrados {} tramos para Camión ID: {}", tramos.size(), idCamion);
        return tramos.stream()
                .map(this::mapTramoToDto)
                .collect(Collectors.toList());
    }

    /**
     * Verifica si el usuario autenticado es el propietario del camión asignado al tramo
     * Retorna una entidad Tramo para verificación de propiedad
     */
    public boolean esPropietarioDelTramo(Integer tramoId, String authId) {
        logger.debug("Verificando propiedad del tramo {} para authId: {}", tramoId, authId);

        Optional<Tramo> tramoOpt = tramoRepository.findById(tramoId);
        if (tramoOpt.isEmpty()) {
            logger.warn("Verificación de propiedad fallida: Tramo {} no encontrado.", tramoId);
            return false;
        }

        Tramo tramo = tramoOpt.get();

        // Verificar que el tramo tenga un camión asignado
        if (tramo.getCamion() == null) {
            logger.warn("Verificación de propiedad fallida: El tramo {} no tiene camión asignado", tramoId);
            return false;
        }

        // Verificar que el camión tenga auth_id
        if (tramo.getCamion().getAuthId() == null) {
            logger.warn("Verificación de propiedad fallida: El camión {} del tramo {} no tiene auth_id",
                    tramo.getCamion().getId(), tramoId);
            return false;
        }

        boolean esPropietario = tramo.getCamion().getAuthId().equals(authId);

        logger.info(
                "Verificación de propiedad - Tramo ID: {}, Camión asignado: {}, AuthId del camión: {}, Resultado: {}",
                tramoId, tramo.getCamion().getId(), tramo.getCamion().getAuthId(), esPropietario);

        return esPropietario;
    }
    
    
    /**
     * Verifica si el usuario autenticado es propietario del camión
     */
    public boolean esPropietarioDelCamion(Integer camionId, String authId) {
        logger.debug("Verificando propiedad del camión {} para authId: {}", camionId, authId);
        
        Optional<Camion> camionOpt = camionRepository.findById(camionId);
        if (camionOpt.isEmpty()) {
            logger.warn("Verificación de propiedad fallida: Camión {} no encontrado.", camionId);
            return false;
        }
        
        Camion camion = camionOpt.get();
        
        if (camion.getAuthId() == null) {
            logger.warn("Verificación de propiedad fallida: El camión {} no tiene auth_id", camionId);
            return false;
        }
        
        boolean esPropietario = camion.getAuthId().equals(authId);
        
        logger.info("Verificación de propiedad del camión - Camión: {}, AuthId del camión: {}, Resultado: {}", 
                camionId, camion.getAuthId(), esPropietario);
        
        return esPropietario;
    }

    /**
     * Helper para mapear Tramo Entity a Tramo DTO
     */
    /**
     * Helper para mapear Tramo Entity a Tramo DTO
     */
    private TramoDto mapTramoToDto(Tramo tramo) {
        // 1. Mapeo base para Tramo
        TramoDto tramoDto = modelMapper.map(tramo, TramoDto.class);
        
        // ✅ SOLUCIÓN: Mapeo explícito de los campos de tiempo para corregir la discrepancia de nombres
        // y asegurar que devuelven 0L si son null.
        tramoDto.setTiempoEstimadoSegundos(
            tramo.getTiempoEstimado() != null ? tramo.getTiempoEstimado() : 0L
        );
        tramoDto.setTiempoRealSegundos(
            tramo.getTiempoReal() != null ? tramo.getTiempoReal() : 0L
        );
        
        // Mapear campos de relación que ModelMapper no maneja automáticamente o requiere lógica
        if (tramo.getEstado() != null) {
            tramoDto.setNombreEstado(tramo.getEstado().getNombre());
        }
        if (tramo.getTipoTramo() != null) {
            tramoDto.setTipoTramo(tramo.getTipoTramo().name());
        }

        // Depósito Origen
        if (tramo.getOrigen() != null) {
            // Mapeamos el Deposito ENTIDAD a Deposito DTO
            DepositoDto origenDto = modelMapper.map(tramo.getOrigen(), DepositoDto.class);

            // Asignamos el nombre de la ciudad explícitamente desde la entidad
            if (tramo.getOrigen().getCiudad() != null) {
                origenDto.setCiudad(tramo.getOrigen().getCiudad().getNombre());
            } else {
                // Manejar caso si la ciudad es nula
                origenDto.setCiudad(null); 
            }
            tramoDto.setOrigen(origenDto);
        }

        // Depósito Destino
        if (tramo.getDestino() != null) {
            // Mapeamos el Deposito ENTIDAD a Deposito DTO
            DepositoDto destinoDto = modelMapper.map(tramo.getDestino(), DepositoDto.class);

            // Asignamos el nombre de la ciudad explícitamente desde la entidad
            if (tramo.getDestino().getCiudad() != null) {
                destinoDto.setCiudad(tramo.getDestino().getCiudad().getNombre());
            } else {
                // Manejar caso si la ciudad es nula
                destinoDto.setCiudad(null);
            }
            tramoDto.setDestino(destinoDto);
        }
        
        return tramoDto;
    }
}