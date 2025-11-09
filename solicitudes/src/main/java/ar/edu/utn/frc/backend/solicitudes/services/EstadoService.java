package ar.edu.utn.frc.backend.solicitudes.services;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // Se añade si no estaba para consistencia.
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.backend.solicitudes.dto.EstadoDto;
import ar.edu.utn.frc.backend.solicitudes.entities.Estado;
import ar.edu.utn.frc.backend.solicitudes.repositories.EstadoRepository;
import jakarta.transaction.Transactional;

@Service
public class EstadoService {
    private static final Logger log = LoggerFactory.getLogger(EstadoService.class);

    @Autowired
    private EstadoRepository estadoRepository;
    @Autowired
    private ModelMapper modelMapper;

      // Guardar un nuevo estado
    @Transactional
    public EstadoDto guardarEstado(EstadoDto estadoDto) {
        log.info("Iniciando guardarEstado con EstadoDto: {}", estadoDto.getNombre());
        Estado estadoEntity = modelMapper.map(estadoDto, Estado.class);
        Estado estadoGuardado = estadoRepository.save(estadoEntity);
        EstadoDto resultado = modelMapper.map(estadoGuardado, EstadoDto.class);
        log.info("Estado guardado con ID: {}", resultado.getId());
        return resultado;
    }

    // Buscar un estado por ID
    public Optional<EstadoDto> buscarPorId(Integer id) {
        log.info("Buscando estado por ID: {}", id);
        Optional<Estado> estadoOpt = estadoRepository.findById(id);
        if (estadoOpt.isPresent()) {
            log.info("Estado encontrado con ID: {}", id);
        } else {
            log.warn("Estado no encontrado con ID: {}", id);
        }
        return estadoOpt.map(estado -> modelMapper.map(estado, EstadoDto.class));
    }

    // Buscar un estado por nombre
    public Optional<EstadoDto> buscarPorNombre(String nombre) {
        log.info("Buscando estado por nombre: {}", nombre);
        Optional<Estado> estadoOpt = estadoRepository.findByNombre(nombre);
        if (estadoOpt.isPresent()) {
            log.info("Estado encontrado: {}", nombre);
        } else {
            log.warn("Estado no encontrado con nombre: {}", nombre);
        }
        return estadoOpt.map(estado -> modelMapper.map(estado, EstadoDto.class));
    }

}