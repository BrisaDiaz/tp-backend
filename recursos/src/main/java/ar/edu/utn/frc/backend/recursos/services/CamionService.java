package ar.edu.utn.frc.backend.recursos.services;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.backend.recursos.dto.CamionDto;
import ar.edu.utn.frc.backend.recursos.entities.Camion;
import ar.edu.utn.frc.backend.recursos.repositories.CamionRepository;
import jakarta.transaction.Transactional;

@Service
public class CamionService {

    @Autowired
    private CamionRepository camionRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Dar de alta un nuevo camión
    @Transactional
    public CamionDto guardarCamion(CamionDto camionDto) {
        Camion camion = modelMapper.map(camionDto, Camion.class);
        Camion camionGuardado = camionRepository.save(camion);
        return modelMapper.map(camionGuardado, CamionDto.class);
    }

    // Buscar camiones disponibles por capacidad mínima de volumen y peso
    public List<CamionDto> buscarCamionesDisponibles(BigDecimal volumenMinimo, BigDecimal pesoMinimo) {
        List<Camion> camiones = camionRepository
                .findByDisponibilidadTrueAndCapacidadVolumenGreaterThanEqualAndCapacidadPesoGreaterThanEqual(
                        volumenMinimo, pesoMinimo);
        return camiones.stream()
                .map(camion -> modelMapper.map(camion, CamionDto.class))
                .collect(Collectors.toList());
    }

    // buscar camión por ID
    public Optional<CamionDto> buscarPorId(Integer id) {
        Optional<Camion> camionOpt = camionRepository.findById(id);
        return camionOpt.map(camion -> modelMapper.map(camion, CamionDto.class));
    }

    // buscar todos los camiones
    public List<CamionDto> buscarTodosLosCamiones() {
        List<Camion> camiones = camionRepository.findAll();
        return camiones.stream()
                .map(camion -> modelMapper.map(camion, CamionDto.class))
                .collect(Collectors.toList());
    }

    // Buscar camión por dominio
    public Optional<CamionDto> buscarPorDominio(String dominio) {
        Optional<Camion> camionOpt = camionRepository.findByDominio(dominio);
        return camionOpt.map(camion -> modelMapper.map(camion, CamionDto.class));
    }

    // Actualizar camión existente
    @Transactional
    public Optional<CamionDto> actualizarCamion(Integer id, CamionDto camionDto) {
        Optional<Camion> camionOpt = camionRepository.findById(id);
        if (camionOpt.isPresent()) {
            Camion camionExistente = camionOpt.get();
            modelMapper.map(camionDto, camionExistente);
            Camion camionActualizado = camionRepository.save(camionExistente);
            return Optional.of(modelMapper.map(camionActualizado, CamionDto.class));
        } else {
            return Optional.empty();
        }
    }

    // Eliminar camión
    @Transactional
    public boolean eliminarCamion(Integer id) {
        if (camionRepository.existsById(id)) {
            camionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Marcar camión como ocupado
    @Transactional
    public Optional<CamionDto> setCamionOcupado(Integer id) {
        return camionRepository.findById(id).map(camion -> {
            camion.liberar();
            return modelMapper.map(camionRepository.save(camion), CamionDto.class);
        });
    }

    // Marcar camión como libre
    @Transactional
    public Optional<CamionDto> setCamionLibre(Integer id) {
        return camionRepository.findById(id).map(camion -> {
            camion.ocupar();
            return modelMapper.map(camionRepository.save(camion), CamionDto.class);
        });
    }

}
