package ar.edu.utn.frc.backend.solicitudes.services;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import ar.edu.utn.frc.backend.solicitudes.dto.EstadoDto;
import ar.edu.utn.frc.backend.solicitudes.entities.Estado;
import ar.edu.utn.frc.backend.solicitudes.repositories.EstadoRepository;
import jakarta.transaction.Transactional;

public class EstadoService {
    @Autowired
    private EstadoRepository estadoRepository;
    @Autowired
    private ModelMapper modelMapper;

     // Guardar un nuevo estado
    @Transactional
    public EstadoDto guardarEstado(EstadoDto estadoDto) {
        Estado estadoEntity = modelMapper.map(estadoDto, Estado.class);
        Estado estadoGuardado = estadoRepository.save(estadoEntity);
        return modelMapper.map(estadoGuardado, EstadoDto.class);
    }

    // Buscar un estado por ID
    public Optional<EstadoDto> buscarPorId(Integer id) {
        Optional<Estado> estadoOpt = estadoRepository.findById(id);
        return estadoOpt.map(estado -> modelMapper.map(estado, EstadoDto.class));
    }

    // Buscar un estado por nombre
    public Optional<EstadoDto> buscarPorNombre(String nombre) {
        Optional<Estado> estadoOpt = estadoRepository.findByNombre(nombre);
        return estadoOpt.map(estado -> modelMapper.map(estado, EstadoDto.class));
    }

}
