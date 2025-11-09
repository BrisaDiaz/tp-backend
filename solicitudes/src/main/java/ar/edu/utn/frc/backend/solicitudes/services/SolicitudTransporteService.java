package ar.edu.utn.frc.backend.solicitudes.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Import de Spring para @Transactional
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.frc.backend.solicitudes.dto.ContenedorDto;
import ar.edu.utn.frc.backend.solicitudes.dto.DepositoDto;
import ar.edu.utn.frc.backend.solicitudes.dto.HistoricoEstadoContenedorDto;
import ar.edu.utn.frc.backend.solicitudes.dto.SolicitudTransporteDto;
import ar.edu.utn.frc.backend.solicitudes.dto.SolicitudTransportePostDto;
import ar.edu.utn.frc.backend.solicitudes.entities.Cliente;
import ar.edu.utn.frc.backend.solicitudes.entities.Contenedor;
import ar.edu.utn.frc.backend.solicitudes.entities.Deposito;
import ar.edu.utn.frc.backend.solicitudes.entities.Estado;
import ar.edu.utn.frc.backend.solicitudes.entities.SolicitudTransporte;
import ar.edu.utn.frc.backend.solicitudes.exceptions.ResourceNotFoundException;
import ar.edu.utn.frc.backend.solicitudes.repositories.ClienteRepository;
import ar.edu.utn.frc.backend.solicitudes.repositories.DepositoRepository;
import ar.edu.utn.frc.backend.solicitudes.repositories.EstadoRepository;
import ar.edu.utn.frc.backend.solicitudes.repositories.SolicitudTransporteRepository;

@Service
public class SolicitudTransporteService {

    private static final Logger log = LoggerFactory.getLogger(SolicitudTransporteService.class);

    private static final String ESTADO_BORRADOR = "Borrador";
    private static final String ESTADO_ENTREGADA = "Entregada";
    private static final String ESTADO_PROGRAMADA = "Programada";
    private static final String ESTADO_EN_TRANSITO = "En Tránsito";
    private static final String RESOURCE_SOLICITUD = "Solicitud de Transporte";

    @Autowired
    private SolicitudTransporteRepository solicitudRepository;
    @Autowired
    private ContenedorService contenedorService;
    @Autowired
    private EstadoRepository estadoRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private DepositoRepository depositoRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public SolicitudTransporteDto guardarSolicitud(SolicitudTransportePostDto postDto) {
        log.info("Iniciando guardarSolicitud para Cliente ID: {}", postDto.getIdCliente());

        Estado estadoBorrador = estadoRepository.findByNombre(ESTADO_BORRADOR)
                .orElseThrow(() -> {
                    log.error("Estado '{}' no configurado.", ESTADO_BORRADOR);
                    return new ResourceNotFoundException("Estado", ESTADO_BORRADOR + " no configurado.");
                });
        log.debug("Estado inicial '{}' encontrado.", ESTADO_BORRADOR);

        Cliente cliente = clienteRepository.findById(postDto.getIdCliente())
                .orElseThrow(() -> {
                    log.error("Cliente ID: {} no encontrado.", postDto.getIdCliente());
                    return new ResourceNotFoundException("Cliente", postDto.getIdCliente());
                });

        Deposito depositoOrigen = depositoRepository.findById(postDto.getIdDepositoOrigen())
                .orElseThrow(() -> {
                    log.error("Depósito de Origen ID: {} no encontrado.", postDto.getIdDepositoOrigen());
                    return new ResourceNotFoundException("Depósito de Origen", postDto.getIdDepositoOrigen());
                });

        Deposito depositoDestino = depositoRepository.findById(postDto.getIdDepositoDestino())
                .orElseThrow(() -> {
                    log.error("Depósito de Destino ID: {} no encontrado.", postDto.getIdDepositoDestino());
                    return new ResourceNotFoundException("Depósito de Destino", postDto.getIdDepositoDestino());
                });

        Contenedor contenedor = contenedorService.guardarContenedor(
                postDto.getVolumenContenedor(),
                postDto.getPesoContenedor());
        log.debug("Contenedor creado con ID: {}", contenedor.getId());

        SolicitudTransporte solicitud = SolicitudTransporte.builder()
                .fechaSolicitud(LocalDateTime.now())
                .estado(estadoBorrador)
                .cliente(cliente)
                .contenedor(contenedor)
                .depositoOrigen(depositoOrigen)
                .depositoDestino(depositoDestino)
                .build();

        SolicitudTransporte solicitudGuardada = solicitudRepository.save(solicitud);
        SolicitudTransporteDto resultado = mapearADto(solicitudGuardada);
        log.info("Solicitud de Transporte guardada con ID: {}", resultado.getId());
        return resultado;
    }

    @Transactional
    public SolicitudTransporteDto actualizarEstadoAProgramada(Integer id, BigDecimal costoEstimado, Long tiempoEstimado) {
        log.info("Actualizando solicitud ID: {} a estado '{}' con Costo: {} y Tiempo: {}", id, ESTADO_PROGRAMADA, costoEstimado, tiempoEstimado);
        SolicitudTransporte solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Solicitud ID: {} no encontrada para actualización a Programada.", id);
                    return new ResourceNotFoundException(RESOURCE_SOLICITUD, id);
                });

        Estado estadoProgramada = estadoRepository.findByNombre(ESTADO_PROGRAMADA)
                .orElseThrow(() -> {
                    log.error("Estado '{}' no configurado.", ESTADO_PROGRAMADA);
                    return new ResourceNotFoundException("Estado", ESTADO_PROGRAMADA + " no configurado.");
                });

        solicitud.setEstado(estadoProgramada);
        solicitud.setCostoEstimado(costoEstimado);
        solicitud.setTiempoEstimado(tiempoEstimado);

        SolicitudTransporteDto resultado = mapearADto(solicitudRepository.save(solicitud));
        log.info("Solicitud ID: {} actualizada a estado Programada.", id);
        return resultado;
    }

    @Transactional
    public SolicitudTransporteDto actualizarEstadoAEnTransito(Integer id) {
        log.info("Actualizando solicitud ID: {} a estado '{}'.", id, ESTADO_EN_TRANSITO);
        SolicitudTransporte solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Solicitud ID: {} no encontrada para actualización a En Tránsito.", id);
                    return new ResourceNotFoundException(RESOURCE_SOLICITUD, id);
                });

        Estado estadoEnTransito = estadoRepository.findByNombre(ESTADO_EN_TRANSITO)
                .orElseThrow(() -> {
                    log.error("Estado '{}' no configurado.", ESTADO_EN_TRANSITO);
                    return new ResourceNotFoundException("Estado", ESTADO_EN_TRANSITO + " no configurado.");
                });

        log.debug("Marcando contenedor ID: {} como 'En Viaje'.", solicitud.getContenedor().getId());
        contenedorService.marcarEnViaje(solicitud.getContenedor().getId(), solicitud.getDepositoOrigen().getNombre());
        solicitud.setEstado(estadoEnTransito);

        SolicitudTransporteDto resultado = mapearADto(solicitudRepository.save(solicitud));
        log.info("Solicitud ID: {} actualizada a estado En Tránsito.", id);
        return resultado;
    }

    @Transactional
    public SolicitudTransporteDto actualizarEstadoAEntregada(Integer id, BigDecimal costoReal, Long tiempoReal) {
        log.info("Actualizando solicitud ID: {} a estado '{}' con Costo Real: {} y Tiempo Real: {}", id, ESTADO_ENTREGADA, costoReal, tiempoReal);
        SolicitudTransporte solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Solicitud ID: {} no encontrada para actualización a Entregada.", id);
                    return new ResourceNotFoundException(RESOURCE_SOLICITUD, id);
                });

        Estado estadoEntregada = estadoRepository.findByNombre(ESTADO_ENTREGADA)
                .orElseThrow(() -> {
                    log.error("Estado '{}' no configurado.", ESTADO_ENTREGADA);
                    return new ResourceNotFoundException("Estado", ESTADO_ENTREGADA + " no configurado.");
                });

        log.debug("Marcando contenedor ID: {} como 'Entregado'.", solicitud.getContenedor().getId());
        contenedorService.marcarComoEntregado(solicitud.getContenedor().getId());
        solicitud.setEstado(estadoEntregada);
        solicitud.setCostoReal(costoReal);
        solicitud.setTiempoReal(tiempoReal);

        SolicitudTransporteDto resultado = mapearADto(solicitudRepository.save(solicitud));
        log.info("Solicitud ID: {} actualizada a estado Entregada.", id);
        return resultado;
    }

    @Transactional
    public Optional<ContenedorDto> actualizarContenedorAEnViaje(Integer id, String nombreDeposito) {
        log.info("Actualizando contenedor de Solicitud ID: {} a estado 'En Viaje'.", id);
        SolicitudTransporte solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Solicitud ID: {} no encontrada para actualizar contenedor a En Viaje.", id);
                    return new ResourceNotFoundException(RESOURCE_SOLICITUD, id);
                });
        return contenedorService.marcarEnViaje(solicitud.getContenedor().getId(), nombreDeposito);
    }

    @Transactional
    public Optional<ContenedorDto> actualizarContenedorAEnDeposito(Integer id, String nombreDeposito) {
        log.info("Actualizando contenedor de Solicitud ID: {} a estado 'En Depósito'.", id);
        SolicitudTransporte solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Solicitud ID: {} no encontrada para actualizar contenedor a En Depósito.", id);
                    return new ResourceNotFoundException(RESOURCE_SOLICITUD, id);
                });
        return contenedorService.marcarEnDeposito(solicitud.getContenedor().getId(), nombreDeposito);
    }

    public List<HistoricoEstadoContenedorDto> obtenerSeguimientoContenedor(Integer idSolicitud) {
        log.info("Obteniendo seguimiento de contenedor para Solicitud ID: {}", idSolicitud);
        SolicitudTransporte solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> {
                    log.error("Solicitud ID: {} no encontrada para obtener seguimiento.", idSolicitud);
                    return new ResourceNotFoundException(RESOURCE_SOLICITUD, idSolicitud);
                });
        return contenedorService.obtenerSeguimientoHistorico(solicitud.getContenedor().getId());
    }

    public Optional<SolicitudTransporteDto> buscarPorId(Integer id) {
        log.info("Buscando solicitud por ID: {}", id);
        Optional<SolicitudTransporteDto> resultado = solicitudRepository.findById(id).map(this::mapearADto);
        if (resultado.isPresent()) {
            log.info("Solicitud encontrada con ID: {}", id);
        } else {
            log.warn("Solicitud no encontrada con ID: {}", id);
        }
        return resultado;
    }

    public List<SolicitudTransporteDto> buscarTodos() {
        log.info("Buscando todas las solicitudes.");
        List<SolicitudTransporteDto> resultado = solicitudRepository.findAll().stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
        log.info("Se encontraron {} solicitudes.", resultado.size());
        return resultado;
    }

    public List<SolicitudTransporteDto> buscarPorEstado(String nombreEstado) {
        log.info("Buscando solicitudes por estado: {}", nombreEstado);
        List<SolicitudTransporteDto> resultado = solicitudRepository.findByEstadoNombre(nombreEstado).stream()
                .map(this::mapearADto)
                .toList();
        log.info("Se encontraron {} solicitudes con estado: {}", resultado.size(), nombreEstado);
        return resultado;
    }

    public List<SolicitudTransporteDto> buscarBorradores() {
        log.info("Buscando solicitudes en estado '{}'.", ESTADO_BORRADOR);
        return buscarPorEstado(ESTADO_BORRADOR);
    }

    public List<SolicitudTransporteDto> buscarPorClienteId(Integer idCliente) {
        log.info("Buscando solicitudes para Cliente ID: {}", idCliente);

        List<SolicitudTransporteDto> resultado = solicitudRepository.findByClienteId(idCliente).stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());

        log.info("Se encontraron {} solicitudes para Cliente ID: {}", resultado.size(), idCliente);
        return resultado;
    }

    public boolean esClienteAutorizado(Integer idCliente, String authIdDelToken) {
        log.info("Verificando autorización de Cliente ID: {} con token authId.", idCliente);

        if (authIdDelToken == null || authIdDelToken.isBlank()) {
            log.warn("Verificación de Cliente ID {} fallida: El authId del token está vacío.", idCliente);
            return false;
        }

        Optional<Cliente> clienteOpt = clienteRepository.findById(idCliente);

        if (clienteOpt.isEmpty()) {
            log.warn("Verificación de Cliente ID {} fallida: Cliente no encontrado.", idCliente);
            return false;
        }

        Cliente cliente = clienteOpt.get();
        boolean esAutorizado = authIdDelToken.equals(cliente.getAuthId());

        if (esAutorizado) {
            log.info("Cliente ID: {} autorizado. El authId coincide.", idCliente);
        } else {
            log.warn("Cliente ID: {} NO autorizado. Token authId: {} no coincide con Cliente authId: {}.",
                    idCliente, authIdDelToken, cliente.getAuthId());
        }

        return esAutorizado;
    }
    
    public boolean esDuenioDeSolicitud(Integer solicitudId, String authIdDelToken) {
        log.info("Verificando propiedad de Solicitud ID: {} con token authId.", solicitudId);
        Optional<SolicitudTransporte> solicitudOpt = solicitudRepository.findById(solicitudId);
        if (solicitudOpt.isEmpty() || authIdDelToken == null || authIdDelToken.isBlank()) {
            log.warn("Verificación fallida: Solicitud ID {} no existe o token es inválido.", solicitudId);
            return false;
        }

        Cliente clienteDuenio = solicitudOpt.get().getCliente();
        boolean esDuenio = clienteDuenio.getAuthId() != null && clienteDuenio.getAuthId().equals(authIdDelToken);
        if (esDuenio) {
            log.info("El token coincide con el cliente de la solicitud ID: {}. Es Propietario.", solicitudId);
        } else {
            log.warn("El token NO coincide con el cliente de la solicitud ID: {}. No es Propietario. Cliente authId: {}", solicitudId, clienteDuenio.getAuthId());
        }
        return esDuenio;
    }

    private SolicitudTransporteDto mapearADto(SolicitudTransporte entity) {
        return SolicitudTransporteDto.builder()
                .id(entity.getId())
                .fechaSolicitud(entity.getFechaSolicitud())
                .costoEstimado(entity.getCostoEstimado())
                .tiempoEstimado(entity.getTiempoEstimado())
                .costoReal(entity.getCostoReal())
                .tiempoReal(entity.getTiempoReal())
                .estado(entity.getEstado().getNombre())
                .clienteId(entity.getCliente().getId())
                .contenedor(modelMapper.map(entity.getContenedor(), ContenedorDto.class))
                .depositoOrigen(modelMapper.map(entity.getDepositoOrigen(), DepositoDto.class))
                .depositoDestino(modelMapper.map(entity.getDepositoDestino(), DepositoDto.class))
                .build();
    }
}