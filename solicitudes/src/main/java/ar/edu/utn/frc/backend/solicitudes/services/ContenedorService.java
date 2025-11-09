package ar.edu.utn.frc.backend.solicitudes.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.backend.solicitudes.dto.ContenedorDto;
import ar.edu.utn.frc.backend.solicitudes.dto.HistoricoEstadoContenedorDto;
import ar.edu.utn.frc.backend.solicitudes.entities.Contenedor;
import ar.edu.utn.frc.backend.solicitudes.entities.Estado;
import ar.edu.utn.frc.backend.solicitudes.exceptions.ResourceNotFoundException;
import ar.edu.utn.frc.backend.solicitudes.repositories.ContenedorRepository;
import ar.edu.utn.frc.backend.solicitudes.repositories.EstadoRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Service
public class ContenedorService {
    private static final Logger log = LoggerFactory.getLogger(ContenedorService.class);

    private static final String ESTADO_PENDIENTE_ENTREGA = "Pendiente de Entrega";
    private static final String ESTADO_EN_DEPOSITO = "En Depósito";
    private static final String ESTADO_ENTREGADO = "Entregado";
    private static final String ESTADO_EN_VIAJE = "En Viaje";
    private static final String TEMPLATE_DESCRIPCION_PENDIENTE_ENTREGA = "Tu contenedor se encuentra en espera de ser retirado.";
    private static final String TEMPLATE_DESCRIPCION_EN_DEPOSITO = "Tu contenedo ingresó al %s.";
    private static final String TEMPLATE_DESCRIPCION_EN_VIAJE = "Tu contenedo salió de %s y sigue en viaje.";
    private static final String TEMPLATE_DESCRIPCION_ENTREGADO = "Tu contenedor ha llegado a destino.";

    @Autowired
    private ContenedorRepository contenedorRepository;
    @Autowired
    private EstadoRepository estadoRepository;
    @Autowired
    private HistoricoEstadoContenedorService historicoEstadoService;
    @Autowired
    private ModelMapper modelMapper;

    @PostConstruct
    public void setupMapper() {
        log.info("Configurando ModelMapper para ContenedorService.");
        modelMapper.createTypeMap(Contenedor.class, ContenedorDto.class).addMapping(
                src -> src.getEstadoActual().getNombre(),
                ContenedorDto::setEstadoActual);
    }

    // Guardar un nuevo contenedor
    public Contenedor guardarContenedor(BigDecimal volumen, BigDecimal peso) {
        log.info("Iniciando guardarContenedor. Volumen: {}, Peso: {}", volumen, peso);

        // 1. Obtener el estado inicial (Pendiente de Entrega)
        Estado estadoInicial = estadoRepository.findByNombre(ESTADO_PENDIENTE_ENTREGA)
                .orElseThrow(() -> {
                    log.error("Estado inicial '{}' no configurado.", ESTADO_PENDIENTE_ENTREGA);
                    return new ResourceNotFoundException("Estado",
                            ESTADO_PENDIENTE_ENTREGA + " no está configurado.");
                });
        log.debug("Estado inicial encontrado: {}", estadoInicial.getNombre());

        // 2. Crear el Contenedor
        Contenedor contenedorEntity = Contenedor.builder()
                .volumen(volumen)
                .peso(peso)
                .estadoActual(estadoInicial)
                .build();
        log.debug("Contenedor a guardar: {}", contenedorEntity);

        Contenedor contenedorGuardado = contenedorRepository.save(contenedorEntity);
        log.info("Contenedor guardado con ID: {}", contenedorGuardado.getId());

        // 4. Registrar el primer Histórico de Estado
        LocalDateTime fechaInicio = LocalDateTime.now();

        historicoEstadoService.crearNuevoHistorico(
                contenedorGuardado,
                estadoInicial,
                fechaInicio,
                TEMPLATE_DESCRIPCION_PENDIENTE_ENTREGA);
        log.debug("Registrado histórico inicial para Contenedor ID: {}", contenedorGuardado.getId());

        // 5. Devolver la entidad
        return contenedorGuardado;
    }

    // Buscar un contenedor por ID
    public Optional<ContenedorDto> buscarPorId(Integer id) {
        log.info("Buscando contenedor por ID: {}", id);
        Optional<Contenedor> contenedorOpt = contenedorRepository.findById(id);
        if (contenedorOpt.isPresent()) {
            log.info("Contenedor encontrado con ID: {}", id);
        } else {
            log.warn("Contenedor no encontrado con ID: {}", id);
        }
        return contenedorOpt.map(contenedor -> modelMapper.map(contenedor, ContenedorDto.class));
    }

    // Buscar todos los contenedores
    public List<ContenedorDto> buscarTodos() {
        log.info("Buscando todos los contenedores.");
        List<Contenedor> contenedores = contenedorRepository.findAll();
        List<ContenedorDto> resultado = contenedores.stream()
                .map(contenedor -> modelMapper.map(contenedor, ContenedorDto.class))
                .collect(Collectors.toList());
        log.info("Se encontraron {} contenedores.", resultado.size());
        return resultado;
    }

    // Buscar contenedores por estado
    public List<ContenedorDto> buscarPorEstado(String estado) {
        log.info("Buscando contenedores por estado: {}", estado);
        List<Contenedor> contenedores = contenedorRepository.findByEstadoNombre(estado);
        List<ContenedorDto> resultado = contenedores.stream()
                .map(contenedor -> modelMapper.map(contenedor, ContenedorDto.class))
                .collect(Collectors.toList());
        log.info("Se encontraron {} contenedores con estado: {}", resultado.size(), estado);
        return resultado;
    }

    // Buscar contenedores pendientes de entrega
    public List<ContenedorDto> buscarContenedoresPendientesDeEntrega() {
        log.info("Buscando contenedores pendientes de entrega.");
        return buscarPorEstado(ESTADO_PENDIENTE_ENTREGA);
    }

    // Buscar contenedores en depósito
    public List<ContenedorDto> buscarContenedoresEnDeposito() {
        log.info("Buscando contenedores en depósito.");
        return buscarPorEstado(ESTADO_EN_DEPOSITO);
    }

    // Buscar contenedores en viaje
    public List<ContenedorDto> buscarContenedoresEnViaje() {
        log.info("Buscando contenedores en viaje.");
        return buscarPorEstado(ESTADO_EN_VIAJE);
    }

    // Buscar contenedores por ID de cliente
    public List<ContenedorDto> buscarPorIdCliente(Integer idCliente) {
        log.info("Buscando contenedores por ID de cliente: {}", idCliente);
        List<Contenedor> contenedores = contenedorRepository.findBySolicitudClienteId(idCliente);
        List<ContenedorDto> resultado = contenedores.stream()
                .map(contenedor -> modelMapper.map(contenedor, ContenedorDto.class))
                .collect(Collectors.toList());
        log.info("Se encontraron {} contenedores para el cliente ID: {}", resultado.size(), idCliente);
        return resultado;
    }

    // Actualizar estado de un contenedor a "Entregado"
    @Transactional
    public Optional<ContenedorDto> marcarComoEntregado(Integer id) {
        log.info("Intentando marcar contenedor ID: {} como '{}'.", id, ESTADO_ENTREGADO);
        String descripcionFormateada = String.format(TEMPLATE_DESCRIPCION_ENTREGADO);
        return actualizarEstadoContenedor(id, ESTADO_ENTREGADO, descripcionFormateada);
    }
    // Actualizar estado de un contenedor a "En Viaje"
    @Transactional
    public Optional<ContenedorDto> marcarEnViaje(Integer id, String nombreDeposito) {
        log.info("Intentando marcar contenedor ID: {} como '{}' saliendo de {}.", id, ESTADO_EN_VIAJE, nombreDeposito);
        String descripcionFormateada = String.format(TEMPLATE_DESCRIPCION_EN_VIAJE, nombreDeposito);
        return actualizarEstadoContenedor(id, ESTADO_EN_VIAJE, descripcionFormateada);
    }
    // Actualizar estado de un contenedor a "En Depósito"
    @Transactional
    public Optional<ContenedorDto> marcarEnDeposito(Integer id, String nombreDeposito) {
        log.info("Intentando marcar contenedor ID: {} como '{}' ingresando a {}.", id, ESTADO_EN_DEPOSITO, nombreDeposito);
        String descripcionFormateada = String.format(TEMPLATE_DESCRIPCION_EN_DEPOSITO, nombreDeposito);
        return actualizarEstadoContenedor(id, ESTADO_EN_DEPOSITO, descripcionFormateada);
    }

    // Actualizar el estado de un contenedor
    @Transactional
    public Optional<ContenedorDto> actualizarEstadoContenedor(
        Integer id,
        String nuevoEstadoNombre,
        String descripcion
    ) {
        log.info("Iniciando actualización de estado para contenedor ID: {} a '{}'.", id, nuevoEstadoNombre);
        // 1. Buscar Contenedor
        Contenedor contenedor = contenedorRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Contenedor ID: {} no encontrado para actualizar estado.", id);
                    return new ResourceNotFoundException("Contenedor", id);
                });

        // 2. Buscar Estado
        Estado nuevoEstado = estadoRepository.findByNombre(nuevoEstadoNombre)
                .orElseThrow(() -> {
                    log.error("Estado '{}' no encontrado.", nuevoEstadoNombre);
                    return new ResourceNotFoundException("Estado", nuevoEstadoNombre);
                });

        log.debug("Contenedor actual: {}. Nuevo estado: {}", contenedor.getEstadoActual().getNombre(), nuevoEstadoNombre);
        if (contenedor.getEstadoActual().getNombre().equals(nuevoEstadoNombre)) {
            log.warn("El contenedor ID: {} ya tiene el estado {}. No se realiza la actualización.", id, nuevoEstadoNombre);
            return Optional.of(modelMapper.map(contenedor, ContenedorDto.class));
        }

        // ********* LÓGICA CLAVE DE CORRECCIÓN: Manejo de Histórico *********
        LocalDateTime ahora = LocalDateTime.now();

        // 2. Cerrar el histórico anterior
        historicoEstadoService.cerrarHistoricoAnterior(contenedor.getId(), ahora);
        log.debug("Cerrado histórico anterior para Contenedor ID: {}", id);

        // 3. Actualizar el Contenedor
        contenedor.setEstadoActual(nuevoEstado);
        Contenedor contenedorActualizado = contenedorRepository.save(contenedor);
        log.info("Contenedor ID: {} actualizado a estado: {}", id, nuevoEstadoNombre);

        // 4. Crear y guardar el nuevo historicoEstado
        historicoEstadoService.crearNuevoHistorico(
                contenedorActualizado,
                nuevoEstado,
                ahora,
                descripcion);
        log.debug("Creado nuevo histórico para Contenedor ID: {} con descripción: {}", id, descripcion);

        return Optional.of(modelMapper.map(contenedorActualizado, ContenedorDto.class));
    }
    
    // Obtener el seguimiento histórico de un contenedor
    public List<HistoricoEstadoContenedorDto> obtenerSeguimientoHistorico(Integer idContenedor) {
        log.info("Buscando seguimiento histórico para contenedor ID: {}", idContenedor);
        List<HistoricoEstadoContenedorDto> historico = historicoEstadoService.buscarHistoricoPorContenedorId(idContenedor);
        log.info("Se encontraron {} registros de histórico para contenedor ID: {}", historico.size(), idContenedor);
        return historico;
    }
}