package ar.edu.utn.frc.backend.solicitudes.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
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
        HistoricoEstadoContenedor nuevoHistorico = HistoricoEstadoContenedor.builder()
            .contenedor(contenedor)
            .estado(estado)
            .fechaHoraDesde(fechaDesde)
            .descripcion(descripcion)
            .build();

        return historicoEstadoRepository.save(nuevoHistorico);
    }

    // Buscar un historicoEstado por ID
    public Optional<HistoricoEstadoContenedorDto> buscarPorId(Integer id) {
        Optional<HistoricoEstadoContenedor> historicoEstadoOpt = historicoEstadoRepository.findById(id);
        return historicoEstadoOpt
                .map(historicoEstado -> modelMapper.map(historicoEstado, HistoricoEstadoContenedorDto.class));
    }

    // Buscar todos los historicos de un contenedor por su ID en orden ascendente de fechaHoraDesde
    public List<HistoricoEstadoContenedorDto> buscarHistoricoPorContenedorId(Integer contenedorId) {
        List<HistoricoEstadoContenedor> historicos = historicoEstadoRepository
            .findByContenedorIdOrderByFechaHoraDesdeAsc(contenedorId);

        return historicos.stream()
            .map(this::mapearADto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void cerrarHistoricoAnterior(Integer contenedorId, LocalDateTime fechaHasta) {
        historicoEstadoRepository.findByContenedorIdAndFechaHoraHastaIsNull(contenedorId)
                .ifPresent(historicoAnterior -> {
                    historicoAnterior.setFechaHoraHasta(fechaHasta);
                    historicoEstadoRepository.save(historicoAnterior);
                });
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
