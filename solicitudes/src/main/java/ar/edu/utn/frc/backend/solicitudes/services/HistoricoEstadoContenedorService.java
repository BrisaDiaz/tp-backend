package ar.edu.utn.frc.backend.solicitudes.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.backend.solicitudes.dto.HistoricoEstadoContenedorDto;
import ar.edu.utn.frc.backend.solicitudes.entities.Contenedor;
import ar.edu.utn.frc.backend.solicitudes.entities.Estado;
import ar.edu.utn.frc.backend.solicitudes.entities.HistoricoEstadoContenedor;
import ar.edu.utn.frc.backend.solicitudes.repositories.HistoricoEstadoContenedorRepository;
import jakarta.transaction.Transactional;

@Service
public class HistoricoEstadoContenedorService {
    private static final Logger log = LoggerFactory.getLogger(HistoricoEstadoContenedorService.class);

    @Autowired
    private HistoricoEstadoContenedorRepository historicoEstadoRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Crear un nuevo registro de histórico de estado para un contenedor
    @Transactional
    public HistoricoEstadoContenedor crearNuevoHistorico(
        Contenedor contenedor, 
        Estado estado, 
        LocalDateTime fechaDesde, 
        String descripcion
    ) {
        log.info("Creando nuevo histórico para Contenedor ID: {} con estado: {} (Desde: {})", 
                 contenedor.getId(), estado.getNombre(), fechaDesde);
        HistoricoEstadoContenedor nuevoHistorico = HistoricoEstadoContenedor.builder()
            .contenedor(contenedor)
            .estado(estado)
            .fechaHoraDesde(fechaDesde)
            .descripcion(descripcion)
            .build();

        HistoricoEstadoContenedor historicoGuardado = historicoEstadoRepository.save(nuevoHistorico);
        log.debug("Nuevo histórico guardado con ID: {}", historicoGuardado.getId());
        return historicoGuardado;
    }

    // Buscar un historicoEstado por ID
    public Optional<HistoricoEstadoContenedorDto> buscarPorId(Integer id) {
        log.info("Buscando histórico de estado por ID: {}", id);
        Optional<HistoricoEstadoContenedor> historicoEstadoOpt = historicoEstadoRepository.findById(id);
        if (historicoEstadoOpt.isPresent()) {
            log.info("Histórico de estado encontrado con ID: {}", id);
        } else {
            log.warn("Histórico de estado no encontrado con ID: {}", id);
        }
        return historicoEstadoOpt
                .map(historicoEstado -> modelMapper.map(historicoEstado, HistoricoEstadoContenedorDto.class));
    }

    // Buscar todos los historicos de un contenedor por su ID en orden ascendente de fechaHoraDesde
    public List<HistoricoEstadoContenedorDto> buscarHistoricoPorContenedorId(Integer contenedorId) {
        log.info("Buscando histórico por Contenedor ID: {}", contenedorId);
        List<HistoricoEstadoContenedor> historicos = historicoEstadoRepository
            .findByContenedorIdOrderByFechaHoraDesdeAsc(contenedorId);
        log.info("Se encontraron {} registros de histórico para Contenedor ID: {}", historicos.size(), contenedorId);

        return historicos.stream()
            .map(this::mapearADto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void cerrarHistoricoAnterior(Integer contenedorId, LocalDateTime fechaHasta) {
        log.debug("Intentando cerrar histórico anterior para Contenedor ID: {} con fechaHasta: {}", contenedorId, fechaHasta);
        historicoEstadoRepository.findByContenedorIdAndFechaHoraHastaIsNull(contenedorId)
                .ifPresent(historicoAnterior -> {
                    log.info("Cerrando histórico ID: {} para Contenedor ID: {}", historicoAnterior.getId(), contenedorId);
                    historicoAnterior.setFechaHoraHasta(fechaHasta);
                    historicoEstadoRepository.save(historicoAnterior);
                    log.debug("Histórico ID: {} cerrado exitosamente.", historicoAnterior.getId());
                });
        log.debug("Finalizado intento de cierre de histórico anterior para Contenedor ID: {}", contenedorId);
    }

    private HistoricoEstadoContenedorDto mapearADto(HistoricoEstadoContenedor entity) {
        return HistoricoEstadoContenedorDto.builder()
                .id(entity.getId())
                .idContenedor(entity.getContenedor().getId())
                .nombreEstado(entity.getEstado().getNombre())
                .fechaHoraDesde(entity.getFechaHoraDesde())
                .fechaHoraHasta(entity.getFechaHoraHasta())
                .descripcion(entity.getDescripcion())
                .build();
    }

}