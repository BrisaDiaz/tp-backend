package ar.edu.utn.frc.backend.logistica.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Crear tramos a partir de una ruta tentativa
     */
    @Transactional
    public void crearTramosDesdeRutaTentativa(Ruta ruta, RutaTentativaDto rutaTentativa) {
        log.info("Creando tramos para ruta ID: {}", ruta.getId());
        
        Optional<Estado> estadoEstimado = estadoRepository.findByNombre(ESTADO_ESTIMADO);
        if (estadoEstimado.isEmpty()) {
            throw new ResourceNotFoundException("Estado '" + ESTADO_ESTIMADO + "' no encontrado en la base de datos");
        }

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
                        String errorMsg = String.format("No se encontraron depósitos para el tramo %d. Origen: %d, Destino: %d", 
                                tramoTentativo.getNroOrden(), 
                                tramoTentativo.getOrigen().getId(), 
                                tramoTentativo.getDestino().getId());
                        log.error(errorMsg);
                        throw new ResourceNotFoundException(errorMsg);
                    }
                    
                    log.info("Tramo {} creado: {} -> {}", 
                            tramo.getNroOrden(), 
                            tramo.getOrigen().getNombre(),
                            tramo.getDestino().getNombre());
                    
                    return tramo;
                })
                .collect(Collectors.toList());

        tramoRepository.saveAll(tramos);
        log.info("Creados {} tramos para ruta ID: {}", tramos.size(), ruta.getId());
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
        List<Tramo> tramos = tramoRepository.findByEstadoNombre(estado);
        if (tramos.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron tramos con estado: " + estado);
        }
        
        return tramos.stream()
                .map(tramo -> modelMapper.map(tramo, TramoDto.class))
                .collect(Collectors.toList());
    }

    // Buscar todos los tramos asociados a una ruta
    public List<TramoDto> buscarPorRuta(Integer idRuta) {
        List<Tramo> tramos = tramoRepository.findByIdRuta(idRuta);
        if (tramos.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron tramos para la ruta ID: " + idRuta);
        }
        
        return tramos.stream()
                .map(tramo -> modelMapper.map(tramo, TramoDto.class))
                .collect(Collectors.toList());
    }

    // Asignar camión a un tramo
    @Transactional
    public Optional<TramoDto> asignarCamion(Integer idTramo, Integer idCamion) {
        Optional<Tramo> optTramo = tramoRepository.findById(idTramo);
        Optional<Camion> optCamion = camionRepository.findById(idCamion);
        Optional<Estado> optEstado = estadoRepository.findByNombre(ESTADO_ASIGNADO);

        if (optTramo.isEmpty()) {
            throw new ResourceNotFoundException("Tramo no encontrado con ID: " + idTramo);
        }
        if (optCamion.isEmpty()) {
            throw new ResourceNotFoundException("Camión no encontrado con ID: " + idCamion);
        }
        if (optEstado.isEmpty()) {
            throw new ResourceNotFoundException("Estado '" + ESTADO_ASIGNADO + "' no encontrado");
        }

        Tramo tramo = optTramo.get();
        Camion camion = optCamion.get();

        // ✅ VALIDACIÓN: Verificar que el tramo no tenga ya un camión asignado
        if (tramo.getCamion() != null) {
            throw new DataConflictException("El tramo ID " + idTramo + " ya tiene un camión asignado");
        }

        // ✅ VALIDACIÓN: Verificar que el camión no esté ocupado
        if (!camion.getDisponibilidad()) {
            throw new DataConflictException("El camión ID " + idCamion + " ya está ocupado");
        }

        tramo.setCamion(camion);
        tramo.setEstado(optEstado.get());
        tramoRepository.save(tramo);

        try {
            recursosClient.setCamionOcupado(idCamion);
            log.info("Camión {} asignado al tramo {}", idCamion, idTramo);
        } catch (Exception e) {
            log.error("Error al actualizar estado del camión: {}", e.getMessage());
            // No lanzamos excepción aquí para no revertir la transacción
        }

        return Optional.of(modelMapper.map(tramo, TramoDto.class));
    }

    // Iniciar tramo
    @Transactional
    public Optional<TramoDto> iniciarTramo(Integer idTramo) {
        Optional<Tramo> optTramo = tramoRepository.findById(idTramo);
        Optional<Estado> optEstado = estadoRepository.findByNombre(ESTADO_INICIADO);

        if (optTramo.isEmpty()) {
            throw new ResourceNotFoundException("Tramo no encontrado con ID: " + idTramo);
        }
        if (optEstado.isEmpty()) {
            throw new ResourceNotFoundException("Estado '" + ESTADO_INICIADO + "' no encontrado");
        }

        Tramo tramo = optTramo.get();

        // ✅ VALIDACIÓN: Verificar que el tramo esté en estado "Asignado"
        if (!tramo.getEstado().getNombre().equals(ESTADO_ASIGNADO)) {
            throw new DataConflictException("El tramo ID " + idTramo + " no está en estado 'Asignado'. Estado actual: " + tramo.getEstado().getNombre());
        }

        // ✅ VALIDACIÓN: Verificar que el tramo tenga camión asignado
        if (tramo.getCamion() == null) {
            throw new DataConflictException("El tramo ID " + idTramo + " no tiene un camión asignado para iniciar");
        }

        tramo.setEstado(optEstado.get());
        tramo.setFechaHoraInicio(LocalDateTime.now());
        tramoRepository.save(tramo);

        try {
            Integer idSolicitud = tramo.getRuta().getSolicitud().getId();
            String tipoTramo = tramo.getTipoTramo().name();
            String depositoOrigen = tramo.getOrigen().getNombre();

            if (tipoTramo.equals(TipoTramo.ORIGEN_DESTINO.name()) ||
                tipoTramo.equals(TipoTramo.ORIGEN_DEPOSITO.name())) {
                solicitudesClient.actualizarSolicitudAEnTransito(idSolicitud);
            } else {
                solicitudesClient.actualizarContenedorEnViaje(idSolicitud, depositoOrigen);
            }
            log.info("Tramo {} iniciado, solicitud {} actualizada", idTramo, idSolicitud);

        } catch (Exception e) {
            log.error("Error al comunicar con microservicio de solicitudes: {}", e.getMessage());
            // No lanzamos excepción aquí para no revertir la transacción
        }

        return Optional.of(modelMapper.map(tramo, TramoDto.class));
    }

    // Finalizar tramo
    @Transactional
    public Optional<TramoDto> finalizarTramo(Integer idTramo) {
        Optional<Tramo> optTramo = tramoRepository.findById(idTramo);
        Optional<Estado> optEstado = estadoRepository.findByNombre(ESTADO_FINALIZADO);

        if (optTramo.isEmpty()) {
            throw new ResourceNotFoundException("Tramo no encontrado con ID: " + idTramo);
        }
        if (optEstado.isEmpty()) {
            throw new ResourceNotFoundException("Estado '" + ESTADO_FINALIZADO + "' no encontrado");
        }

        Tramo tramo = optTramo.get();

        // ✅ VALIDACIÓN: Verificar que el tramo esté en estado "Iniciado"
        if (!tramo.getEstado().getNombre().equals(ESTADO_INICIADO)) {
            throw new DataConflictException("El tramo ID " + idTramo + " no está en estado 'Iniciado'. Estado actual: " + tramo.getEstado().getNombre());
        }

        // ✅ VALIDACIÓN: Verificar que tenga fecha de inicio
        if (tramo.getFechaHoraInicio() == null) {
            throw new DataConflictException("El tramo ID " + idTramo + " no tiene fecha/hora de inicio registrada");
        }

        tramo.setEstado(optEstado.get());
        tramo.setFechaHoraFin(LocalDateTime.now());
        tramoRepository.save(tramo);

        try {
            Integer idSolicitud = tramo.getRuta().getSolicitud().getId();
            String depositoDestino = tramo.getDestino().getNombre();
            String tipoTramo = tramo.getTipoTramo().name();

            if (tipoTramo.equals(TipoTramo.DEPOSITO_DESTINO.name()) ||
                tipoTramo.equals(TipoTramo.ORIGEN_DESTINO.name())) {
                
                // Calcular costos y tiempo real de la solicitud
                List<Tramo> tramosRuta = tramoRepository.findByIdRuta(tramo.getRuta().getId())
                        .stream()
                        .sorted(Comparator.comparingInt(Tramo::getNroOrden))
                        .collect(Collectors.toList());

                BigDecimal cargoPorGestion = recursosClient.getCargoPorGestion().getCostoPorTramo();
                BigDecimal costoCombustiblePorLitro = recursosClient.getCostoCombustiblePorLitro().getPrecioPorLitro();

                BigDecimal costoTotal = BigDecimal.ZERO;
                Long tiempoTotalSegundos = 0L;

                for (int i = 0; i < tramosRuta.size(); i++) {
                    Tramo actual = tramosRuta.get(i);
                    LocalDateTime inicio = actual.getFechaHoraInicio();
                    LocalDateTime fin = actual.getFechaHoraFin();

                    if (inicio == null || fin == null) continue;

                    Long tiempoReal = Duration.between(inicio, fin).toSeconds();
                    tiempoTotalSegundos += tiempoReal;

                    Long diasEstadiaOrigen = 0L;
                    if (i > 0) {
                        Tramo anterior = tramosRuta.get(i - 1);
                        if (anterior.getFechaHoraFin() != null) {
                            Duration d = Duration.between(anterior.getFechaHoraFin(), actual.getFechaHoraInicio());
                            diasEstadiaOrigen = (long) Math.ceil(d.toHours() / 24.0);
                        }
                    }

                    BigDecimal costoEstadia = actual.getOrigen().getPrecioPorDia()
                            .multiply(BigDecimal.valueOf(diasEstadiaOrigen));

                    BigDecimal costoKm = actual.getCamion().getCostoPorKm()
                            .multiply(BigDecimal.valueOf(actual.getDistanciaKm()));

                    BigDecimal costoCombustible = BigDecimal.valueOf(actual.getDistanciaKm())
                            .multiply(actual.getCamion().getConsumoCombustiblePromedio())
                            .multiply(costoCombustiblePorLitro);

                    BigDecimal costoReal = costoEstadia.add(costoKm).add(costoCombustible);

                    actual.setCostoReal(costoReal);
                    actual.setTiempoReal(tiempoReal);
                    tramoRepository.save(actual);

                    costoTotal = costoTotal.add(costoReal);
                }

                costoTotal = costoTotal.add(cargoPorGestion
                        .multiply(BigDecimal.valueOf(tramosRuta.size())))
                        .setScale(2, RoundingMode.HALF_UP);

                solicitudesClient.actualizarSolicitudAEntregada(idSolicitud, costoTotal, tiempoTotalSegundos);
            } else {
                solicitudesClient.actualizarContenedorEnDeposito(idSolicitud, depositoDestino);
            }

            recursosClient.setCamionLibre(tramo.getCamion().getId());
            log.info("Tramo {} finalizado correctamente y camión liberado", idTramo);

        } catch (Exception e) {
            log.error("Error al finalizar tramo o comunicar con microservicios: {}", e.getMessage());
            // No lanzamos excepción aquí para no revertir la transacción
        }

        return Optional.of(modelMapper.map(tramo, TramoDto.class));
    }

    /**
     * Buscar todos los tramos ASIGNADOS a un camión específico.
     */
    public List<TramoDto> buscarTramosAsignadosCamion(Integer idCamion) {
        List<Tramo> tramos = tramoRepository.findByCamionAsignado(idCamion);
        if (tramos.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron tramos asignados al camión ID: " + idCamion);
        }

        return tramos.stream()
                .map(tramo -> modelMapper.map(tramo, TramoDto.class))
                .collect(Collectors.toList());
    }

    /**
     * Verifica si el usuario autenticado es el propietario del camión asignado al tramo
     */
    public boolean esPropietarioDelTramo(Integer tramoId, String authId) {
        log.info("Verificando propiedad del tramo {} para authId: {}", tramoId, authId);

        Optional<Tramo> tramoOpt = tramoRepository.findById(tramoId);
        if (tramoOpt.isEmpty()) {
            log.warn("Tramo no encontrado: {}", tramoId);
            return false;
        }

        Tramo tramo = tramoOpt.get();

        // Verificar que el tramo tenga un camión asignado
        if (tramo.getCamion() == null) {
            log.warn("El tramo {} no tiene camión asignado", tramoId);
            return false;
        }

        // Verificar que el camión tenga auth_id
        if (tramo.getCamion().getAuthId() == null) {
            log.warn("El camión {} del tramo {} no tiene auth_id", tramo.getCamion().getId(), tramoId);
            return false;
        }

        boolean esPropietario = tramo.getCamion().getAuthId().equals(authId);

        log.info(
                "Verificación de propiedad - Tramo ID: {}, Camión asignado: {}, AuthId del camión: {}, AuthId del usuario: {}, Resultado: {}",
                tramoId, tramo.getCamion().getId(), tramo.getCamion().getAuthId(), authId, esPropietario);

        return esPropietario;
    }
    
    /**
     * Verifica si el usuario autenticado es propietario del camión
     */
    public boolean esPropietarioDelCamion(Integer camionId, String authId) {
        log.info("Verificando propiedad del camión {} para authId: {}", camionId, authId);
        
        Optional<Camion> camionOpt = camionRepository.findById(camionId);
        if (camionOpt.isEmpty()) {
            log.warn("Camión no encontrado: {}", camionId);
            return false;
        }
        
        Camion camion = camionOpt.get();
        
        if (camion.getAuthId() == null) {
            log.warn("El camión {} no tiene auth_id", camionId);
            return false;
        }
        
        boolean esPropietario = camion.getAuthId().equals(authId);
        
        log.info("Verificación de propiedad del camión - Solicitado: {}, AuthId del camión: {}, AuthId del usuario: {}, Resultado: {}", 
                camionId, camion.getAuthId(), authId, esPropietario);
        
        return esPropietario;
    }
}