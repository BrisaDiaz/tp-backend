package ar.edu.utn.frc.backend.solicitudes.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.backend.solicitudes.dto.ContenedorDto;
import ar.edu.utn.frc.backend.solicitudes.entities.Contenedor;
import ar.edu.utn.frc.backend.solicitudes.entities.Estado;
import ar.edu.utn.frc.backend.solicitudes.exceptions.ResourceNotFoundException;
import ar.edu.utn.frc.backend.solicitudes.repositories.ContenedorRepository;
import ar.edu.utn.frc.backend.solicitudes.repositories.EstadoRepository;
import jakarta.transaction.Transactional;

@Service
public class ContenedorService {
    private static final String ESTADO_PENDIENTE_ENTREGA = "Pendiente de Entrega";
    private static final String ESTADO_EN_DEPOSITO = "En Depósito";
    private static final String ESTADO_ENTREGADO = "Entregado";
    private static final String ESTADO_EN_VIAJE = "En Viaje";
    private static final String TEMPLATE_DESCRIPCION_PENDIENTE_ENTREGA = "Tu contenedor aún no ha sido retirado.";
    private static final String TEMPLATE_DESCRIPCION_EN_DEPOSITO = "Tu contenedo ingresó al depósito de %s.";
    private static final String TEMPLATE_DESCRIPCION_EN_VIAJE = "Tu contenedo salió del depósito de %s y sigue en viaje.";
    private static final String TEMPLATE_DESCRIPCION_ENTREGADO = "Tu contenedor ha llegado a destino.";

    @Autowired
    private ContenedorRepository contenedorRepository;
    @Autowired
    private EstadoRepository estadoRepository;
    @Autowired
    private HistoricoEstadoContenedorService historicoEstadoService;
    @Autowired
    private ModelMapper modelMapper;

    // Guardar un nuevo contenedor
    public Contenedor guardarContenedor(BigDecimal volumen, BigDecimal peso) {

        // 1. Obtener el estado inicial (Pendiente de Entrega)
        Estado estadoInicial = estadoRepository.findByNombre(ESTADO_PENDIENTE_ENTREGA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado",
                        ESTADO_PENDIENTE_ENTREGA + " no está configurado."));

        // 2. Crear el Contenedor
        Contenedor contenedorEntity = Contenedor.builder()
                .volumen(volumen)
                .peso(peso)
                .estadoActual(estadoInicial)
                .build();

        Contenedor contenedorGuardado = contenedorRepository.save(contenedorEntity);

        // 4. Registrar el primer Histórico de Estado
        LocalDate fechaInicio = LocalDate.now();

        historicoEstadoService.crearNuevoHistorico(
                contenedorGuardado,
                estadoInicial,
                fechaInicio,
                TEMPLATE_DESCRIPCION_PENDIENTE_ENTREGA);

        // 5. Devolver el DTO
        return contenedorGuardado;
    }

    // Buscar un contenedor por ID
    public Optional<ContenedorDto> buscarPorId(Integer id) {
        Optional<Contenedor> contenedorOpt = contenedorRepository.findById(id);
        return contenedorOpt.map(contenedor -> modelMapper.map(contenedor, ContenedorDto.class));
    }

    // Buscar todos los contenedores
    public List<ContenedorDto> buscarTodos() {
        List<Contenedor> contenedores = contenedorRepository.findAll();
        return contenedores.stream()
                .map(contenedor -> modelMapper.map(contenedor, ContenedorDto.class))
                .collect(Collectors.toList());
    }

    // Buscar contenedores por estado
    public List<ContenedorDto> buscarPorEstado(String estado) {
        List<Contenedor> contenedores = contenedorRepository.findByEstadoNombre(estado);
        return contenedores.stream()
                .map(contenedor -> modelMapper.map(contenedor, ContenedorDto.class))
                .collect(Collectors.toList());
    }

    // Buscar contenedores pendientes de entrega
    public List<ContenedorDto> buscarContenedoresPendientesDeEntrega() {
        return buscarPorEstado(ESTADO_PENDIENTE_ENTREGA);
    }

    // Buscar contenedores en depósito
    public List<ContenedorDto> buscarContenedoresEnDeposito() {
        return buscarPorEstado(ESTADO_EN_DEPOSITO);
    }

    // Buscar contenedores en viaje
    public List<ContenedorDto> buscarContenedoresEnViaje() {
        return buscarPorEstado(ESTADO_EN_VIAJE);
    }

    // Buscar contenedores por ID de cliente
    public List<ContenedorDto> buscarPorIdCliente(Integer idCliente) {
        List<Contenedor> contenedores = contenedorRepository.findBySolicitudClienteId(idCliente);
        return contenedores.stream()
                .map(contenedor -> modelMapper.map(contenedor, ContenedorDto.class))
                .collect(Collectors.toList());
    }

    // Actualizar estado de un contenedor a "Entregado"
    @Transactional
    public Optional<ContenedorDto> marcarComoEntregado(Integer id) {
        String descripcionFormateada = String.format(TEMPLATE_DESCRIPCION_ENTREGADO);
        return actualizarEstadoContenedor(id, ESTADO_ENTREGADO, descripcionFormateada);
    }
    // Actualizar estado de un contenedor a "En Viaje"
    @Transactional
    public Optional<ContenedorDto> marcarEnViaje(Integer id, String nombreDeposito) {
        String descripcionFormateada = String.format(TEMPLATE_DESCRIPCION_EN_VIAJE, nombreDeposito);
        return actualizarEstadoContenedor(id, ESTADO_EN_VIAJE, descripcionFormateada);
    }
    // Actualizar estado de un contenedor a "En Depósito"
    @Transactional
    public Optional<ContenedorDto> marcarEnDeposito(Integer id, String nombreDeposito) {
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
        // 1. Buscar Contenedor
        Contenedor contenedor = contenedorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contenedor", id));

        // 2. Buscar Estado
        Estado nuevoEstado = estadoRepository.findByNombre(nuevoEstadoNombre)
            .orElseThrow(() -> new ResourceNotFoundException("Estado", nuevoEstadoNombre));

        // ********* LÓGICA CLAVE DE CORRECCIÓN: Manejo de Histórico *********
        LocalDate ahora = LocalDate.now();

        // 2. Cerrar el histórico anterior
        historicoEstadoService.cerrarHistoricoAnterior(contenedor.getId(), ahora);

        // 3. Actualizar el Contenedor
        contenedor.setEstadoActual(nuevoEstado);
        Contenedor contenedorActualizado = contenedorRepository.save(contenedor);

        // 4. Crear y guardar el nuevo historicoEstado
        historicoEstadoService.crearNuevoHistorico(
            contenedorActualizado,
            nuevoEstado,
            ahora,
            descripcion
        );

        return Optional.of(modelMapper.map(contenedorActualizado, ContenedorDto.class));
    }
}
