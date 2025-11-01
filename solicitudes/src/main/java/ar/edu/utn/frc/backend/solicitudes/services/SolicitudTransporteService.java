package ar.edu.utn.frc.backend.solicitudes.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    // Guardar una nueva solicitud de transporte y su contenedor asociado
    @Transactional
    public SolicitudTransporteDto guardarSolicitud(SolicitudTransportePostDto postDto) {
        // ... (Lógica de guardar solicitud sin cambios)
        Estado estadoBorrador = estadoRepository.findByNombre(ESTADO_BORRADOR)
                .orElseThrow(() -> new ResourceNotFoundException("Estado", ESTADO_BORRADOR + " no configurado."));

        Cliente cliente = clienteRepository.findById(postDto.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", postDto.getIdCliente()));

        Deposito depositoOrigen = depositoRepository.findById(postDto.getIdDepositoOrigen())
                .orElseThrow(() -> new ResourceNotFoundException("Depósito de Origen", postDto.getIdDepositoOrigen()));

        Deposito depositoDestino = depositoRepository.findById(postDto.getIdDepositoDestino())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Depósito de Destino", postDto.getIdDepositoDestino()));

        Contenedor contenedor = contenedorService.guardarContenedor(
                postDto.getVolumenContenedor(),
                postDto.getPesoContenedor());

        SolicitudTransporte solicitud = SolicitudTransporte.builder()
                .fechaSolicitud(LocalDate.now())
                .estado(estadoBorrador)
                .cliente(cliente)
                .contenedor(contenedor)
                .depositoOrigen(depositoOrigen)
                .depositoDestino(depositoDestino)
                .build();

        SolicitudTransporte solicitudGuardada = solicitudRepository.save(solicitud);

        return mapearADto(solicitudGuardada);
    }

    // Actualizar el estado de una solicitud a "Programada"
    @Transactional
    public SolicitudTransporteDto actualizarEstadoAProgramada(Integer id, BigDecimal costoEstimado, int tiempoEstimado) {
        // ... (Lógica de actualización de estado sin cambios)
        SolicitudTransporte solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_SOLICITUD, id));

        Estado estadoProgramada = estadoRepository.findByNombre(ESTADO_PROGRAMADA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado", ESTADO_PROGRAMADA + " no configurado."));

        solicitud.setEstado(estadoProgramada);
        solicitud.setCostoEstimado(costoEstimado);
        solicitud.setTiempoEstimado(tiempoEstimado);
        SolicitudTransporte solicitudActualizada = solicitudRepository.save(solicitud);

        return mapearADto(solicitudActualizada);
    }

   // Actualizar el estado de una solicitud a "Entregada"
    @Transactional
    public SolicitudTransporteDto actualizarEstadoAEntregada(Integer id, BigDecimal costoReal, int tiempoReal) {
        // ... (Lógica de actualización de estado sin cambios)
        SolicitudTransporte solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_SOLICITUD, id));

        Estado estadoEntregada = estadoRepository.findByNombre(ESTADO_ENTREGADA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado", ESTADO_ENTREGADA + " no configurado."));

        contenedorService.marcarComoEntregado(solicitud.getContenedor().getId());

        solicitud.setEstado(estadoEntregada);
        solicitud.setCostoReal(costoReal);
        solicitud.setTiempoReal(tiempoReal);
        SolicitudTransporte solicitudActualizada = solicitudRepository.save(solicitud);

        return mapearADto(solicitudActualizada);
    }

    // Actualizar el estado de una solicitud a "En Tránsito"
    @Transactional
    public SolicitudTransporteDto actualizarEstadoAEnTransito(Integer id) {
        // ... (Lógica de actualización de estado sin cambios)
        SolicitudTransporte solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_SOLICITUD, id));

        Estado estadoEnTransito = estadoRepository.findByNombre(ESTADO_EN_TRANSITO)
                .orElseThrow(() -> new ResourceNotFoundException("Estado", ESTADO_EN_TRANSITO + " no configurado."));

        String nombreDepositoOrigen = solicitud.getDepositoOrigen().getNombre();
        contenedorService.marcarEnViaje(solicitud.getContenedor().getId(), nombreDepositoOrigen);

        solicitud.setEstado(estadoEnTransito);
        SolicitudTransporte solicitudActualizada = solicitudRepository.save(solicitud);

        return mapearADto(solicitudActualizada);
    }

    // Actualizar el estado del contenedor de una solicitud a "En Viaje"
    @Transactional
    public Optional<ContenedorDto> actualizarContenedorAEnViaje(Integer id, String nombreDeposito) {
             SolicitudTransporte solicitud = solicitudRepository.findById(id)
                             .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_SOLICITUD, id));
             return contenedorService.marcarEnViaje(solicitud.getContenedor().getId(), nombreDeposito);
    }
    
    // Actualizar el estado del contenedor de una solicitud a "En Depósito"
    @Transactional
    public Optional<ContenedorDto> actualizarContenedorAEnDeposito(Integer id, String nombreDeposito) {
             SolicitudTransporte solicitud = solicitudRepository.findById(id)
                             .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_SOLICITUD, id));
             return contenedorService.marcarEnDeposito(solicitud.getContenedor().getId(), nombreDeposito);
    }
    
    // Obtener el seguimiento de los estados del contenedor asociado a una solicitud
    public List<HistoricoEstadoContenedorDto> obtenerSeguimientoContenedor(Integer idSolicitud) {
        SolicitudTransporte solicitud = solicitudRepository.findById(idSolicitud)
            .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_SOLICITUD, idSolicitud));
        return contenedorService.obtenerSeguimientoHistorico(solicitud.getContenedor().getId());
   }

    // Buscar una solicitud por ID
    public Optional<SolicitudTransporteDto> buscarPorId(Integer id) {
        return solicitudRepository.findById(id)
            .map(this::mapearADto);
    }

    // Buscar solicitudes por estado
    public List<SolicitudTransporteDto> buscarPorEstado(String nombreEstado) {
        List<SolicitudTransporte> solicitudes = solicitudRepository.findByEstadoNombre(nombreEstado);
        return solicitudes.stream()
                .map(this::mapearADto)
                .toList();
    }

    // Bucar solicitudes en estado "Borrador"
    public List<SolicitudTransporteDto> buscarBorradores() {
        return buscarPorEstado(ESTADO_BORRADOR);
    }

    // ====================================================================
    // MÉTODOS DE SEGURIDAD PARA @PreAuthorize
    // ====================================================================

    public boolean esDuenioDeSolicitud(Integer solicitudId, String authIdDelToken) {
        
        Optional<SolicitudTransporte> solicitudOpt = solicitudRepository.findById(solicitudId);
        
        // Si no se encuentra la solicitud, devolvemos false para ocultar su existencia a usuarios no autorizados.
        if (solicitudOpt.isEmpty() || authIdDelToken == null || authIdDelToken.trim().isEmpty()) {
            return false;
        }

        SolicitudTransporte solicitud = solicitudOpt.get();
        Cliente clienteDuenio = solicitud.getCliente();

        // Si el cliente dueño de la solicitud no tiene un authId (aún no ha iniciado sesión vía Keycloak)
        if (clienteDuenio.getAuthId() == null) {
            return false;
        }
        
        // Comparamos el authId de la solicitud con el authId del token.
        return clienteDuenio.getAuthId().equals(authIdDelToken);
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
                // El clienteId sigue siendo el ID numérico interno para el DTO
                .clienteId(entity.getCliente().getId()) 
                .contenedor(modelMapper.map(entity.getContenedor(), ContenedorDto.class))
                .depositoOrigen(modelMapper.map(entity.getDepositoOrigen(), DepositoDto.class))
                .depositoDestino(modelMapper.map(entity.getDepositoDestino(), DepositoDto.class))
                .build();
    }
}