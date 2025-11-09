package ar.edu.utn.frc.backend.recursos.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.backend.recursos.dto.CamionDto;
import ar.edu.utn.frc.backend.recursos.entities.Camion;
import ar.edu.utn.frc.backend.recursos.repositories.CamionRepository;
import jakarta.transaction.Transactional;

@Service
public class CamionService {

    private static final Logger logger = LoggerFactory.getLogger(CamionService.class);

    @Autowired
    private CamionRepository camionRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Dar de alta un nuevo camión
    @Transactional
    public CamionDto guardarCamion(CamionDto camionDto) {
        logger.info("Iniciando: Guardar nuevo camión con dominio: {}", camionDto.getDominio());
        Camion camion = modelMapper.map(camionDto, Camion.class);
        Camion camionGuardado = camionRepository.save(camion);
        logger.info("Finalizado: Camión guardado exitosamente con ID: {}", camionGuardado.getId());
        return modelMapper.map(camionGuardado, CamionDto.class);
    }

    // Buscar camiones disponibles por capacidad mínima de volumen y peso
    public List<CamionDto> buscarCamionesDisponibles(BigDecimal volumenMinimo, BigDecimal pesoMinimo) {
        logger.debug("Buscando camiones disponibles con volumenMinimo: {} y pesoMinimo: {}", volumenMinimo, pesoMinimo);
        List<Camion> camiones = camionRepository
                .findByDisponibilidadTrueAndCapacidadVolumenGreaterThanEqualAndCapacidadPesoGreaterThanEqual(
                        volumenMinimo, pesoMinimo);
        logger.info("Encontrados {} camiones disponibles.", camiones.size());
        return camiones.stream()
                .map(camion -> modelMapper.map(camion, CamionDto.class))
                .collect(Collectors.toList());
    }

    // buscar camión por ID
    public Optional<CamionDto> buscarPorId(Integer id) {
        logger.debug("Buscando camión por ID: {}", id);
        Optional<Camion> camionOpt = camionRepository.findById(id);
        if (camionOpt.isPresent()) {
            logger.info("Camión ID: {} encontrado.", id);
        } else {
            logger.warn("Camión ID: {} no encontrado.", id);
        }
        return camionOpt.map(camion -> modelMapper.map(camion, CamionDto.class));
    }

    // buscar todos los camiones
    public List<CamionDto> buscarTodosLosCamiones() {
        logger.debug("Buscando todos los camiones.");
        List<Camion> camiones = camionRepository.findAll();
        logger.info("Encontrados {} camiones en total.", camiones.size());
        return camiones.stream()
                .map(camion -> modelMapper.map(camion, CamionDto.class))
                .collect(Collectors.toList());
    }

    // Buscar camión por dominio
    public Optional<CamionDto> buscarPorDominio(String dominio) {
        logger.debug("Buscando camión por Dominio: {}", dominio);
        Optional<Camion> camionOpt = camionRepository.findByDominio(dominio);
        if (camionOpt.isPresent()) {
            logger.info("Camión Dominio: {} encontrado.", dominio);
        } else {
            logger.warn("Camión Dominio: {} no encontrado.", dominio);
        }
        return camionOpt.map(camion -> modelMapper.map(camion, CamionDto.class));
    }

    // Actualizar camión existente
    @Transactional
    public Optional<CamionDto> actualizarCamion(Integer id, CamionDto camionDto) {
        logger.info("Iniciando: Actualización de camión con ID: {}", id);
        Optional<Camion> camionOpt = camionRepository.findById(id);
        if (camionOpt.isPresent()) {
            Camion camionExistente = camionOpt.get();
            modelMapper.map(camionDto, camionExistente);
            Camion camionActualizado = camionRepository.save(camionExistente);
            logger.info("Finalizado: Camión ID: {} actualizado exitosamente.", id);
            return Optional.of(modelMapper.map(camionActualizado, CamionDto.class));
        } else {
            logger.warn("Actualización fallida: Camión ID: {} no encontrado.", id);
            return Optional.empty();
        }
    }

    // Eliminar camión
    @Transactional
    public boolean eliminarCamion(Integer id) {
        logger.info("Iniciando: Eliminación de camión con ID: {}", id);
        if (camionRepository.existsById(id)) {
            camionRepository.deleteById(id);
            logger.info("Finalizado: Camión ID: {} eliminado exitosamente.", id);
            return true;
        }
        logger.warn("Eliminación fallida: Camión ID: {} no existe.", id);
        return false;
    }

    // Marcar camión como ocupado
    @Transactional
    public Optional<CamionDto> setCamionOcupado(Integer id) {
        logger.info("Intentando: Marcar camión ID: {} como ocupado.", id);
        return camionRepository.findById(id).map(camion -> {
            if (!camion.getDisponibilidad()) {
                logger.error("Error al ocupar: Camión ID: {} ya estaba ocupado.", id);
                throw new IllegalStateException("No se puede ocupar el camión: ya está ocupado");
            }
            camion.ocupar();
            Camion camionActualizado = camionRepository.save(camion);
            logger.info("Finalizado: Camión ID: {} marcado como OCUPADO.", id);
            return modelMapper.map(camionActualizado, CamionDto.class);
        });
    }

    // Marcar camión como libre  
    @Transactional
    public Optional<CamionDto> setCamionLibre(Integer id) {
        logger.info("Intentando: Marcar camión ID: {} como libre.", id);
        return camionRepository.findById(id).map(camion -> {
            if (camion.getDisponibilidad()) {
                logger.error("Error al liberar: Camión ID: {} ya estaba libre.", id);
                throw new IllegalStateException("No se puede liberar el camión: ya está libre");
            }
            camion.liberar();
            Camion camionActualizado = camionRepository.save(camion);
            logger.info("Finalizado: Camión ID: {} marcado como LIBRE.", id);
            return modelMapper.map(camionActualizado, CamionDto.class);
        });
    }

}