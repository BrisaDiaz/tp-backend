package ar.edu.utn.frc.backend.recursos.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.backend.recursos.dto.CiudadDto;
import ar.edu.utn.frc.backend.recursos.entities.Ciudad;
import ar.edu.utn.frc.backend.recursos.exceptions.DataConflictException;
import ar.edu.utn.frc.backend.recursos.repositories.CiudadRepository;
import ar.edu.utn.frc.backend.recursos.repositories.DepositoRepository;
import jakarta.transaction.Transactional;

@Service
public class CiudadService {

    private static final Logger logger = LoggerFactory.getLogger(CiudadService.class);

    @Autowired
    private CiudadRepository ciudadRepository;
    @Autowired
    private DepositoRepository depositoRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Dar de alta una ciudad
    @Transactional
    public CiudadDto guardarCiudad(CiudadDto ciudadDto) {
        logger.info("Iniciando: Guardar nueva ciudad con nombre: {} y CP: {}", ciudadDto.getNombre(), ciudadDto.getCodigoPostal());
        Ciudad ciudad = modelMapper.map(ciudadDto, Ciudad.class);
        Ciudad ciudadGuardado = ciudadRepository.save(ciudad);
        logger.info("Finalizado: Ciudad guardada exitosamente con ID: {}", ciudadGuardado.getId());
        return modelMapper.map(ciudadGuardado, CiudadDto.class);
    }

    // Buscar ciudad por código postal
    public Optional<CiudadDto> buscarPorCodigoPostal(String codigoPostal) {
        logger.debug("Buscando ciudad por Código Postal: {}", codigoPostal);
        Optional<CiudadDto> ciudadOpt = ciudadRepository.findByCodigoPostal(codigoPostal)
                .map(ciudad -> modelMapper.map(ciudad, CiudadDto.class));
        if (ciudadOpt.isPresent()) {
            logger.info("Ciudad con CP: {} encontrada.", codigoPostal);
        } else {
            logger.warn("Ciudad con CP: {} no encontrada.", codigoPostal);
        }
        return ciudadOpt;
    }

    // Buscar ciudad por ID
    public Optional<CiudadDto> buscarPorId(Integer id) {
        logger.debug("Buscando ciudad por ID: {}", id);
        Optional<CiudadDto> ciudadOpt = ciudadRepository.findById(id)
                .map(ciudad -> modelMapper.map(ciudad, CiudadDto.class));
        if (ciudadOpt.isPresent()) {
            logger.info("Ciudad ID: {} encontrada.", id);
        } else {
            logger.warn("Ciudad ID: {} no encontrada.", id);
        }
        return ciudadOpt;
    }

    public List<CiudadDto> buscarTodos() {
        logger.debug("Buscando todas las ciudades.");
        List<CiudadDto> ciudades = ciudadRepository.findAll().stream()
                .map(ciudad -> modelMapper.map(ciudad, CiudadDto.class))
                .collect(Collectors.toList());
        logger.info("Encontradas {} ciudades en total.", ciudades.size());
        return ciudades;
    }

    // Actualizar ciudad existente
    @Transactional
    public Optional<CiudadDto> actualizarCiudad(Integer id, CiudadDto ciudadDto) {
        logger.info("Iniciando: Actualización de ciudad con ID: {}", id);
        Optional<Ciudad> ciudadOpt = ciudadRepository.findById(id);
        if (ciudadOpt.isPresent()) {
            Ciudad ciudadExistente = ciudadOpt.get();
            modelMapper.map(ciudadDto, ciudadExistente);
            Ciudad ciudadActualizado = ciudadRepository.save(ciudadExistente);
            logger.info("Finalizado: Ciudad ID: {} actualizada exitosamente.", id);
            return Optional.of(modelMapper.map(ciudadActualizado, CiudadDto.class));
        } else {
            logger.warn("Actualización fallida: Ciudad ID: {} no encontrada.", id);
            return Optional.empty();
        }
    }

    @Transactional
    public boolean eliminarCiudad(Integer id) {
        logger.info("Iniciando: Eliminación de ciudad con ID: {}", id);
        if (ciudadRepository.existsById(id)) {
            // VERIFICAR si hay depósitos que dependen de esta ciudad
            if (depositoRepository.existsByCiudadId(id)) {
                logger.error("Error de eliminación: Ciudad ID: {} tiene depósitos asociados.", id);
                throw new DataConflictException("No se puede eliminar la ciudad: tiene depósitos asociados");
            }
            ciudadRepository.deleteById(id);
            logger.info("Finalizado: Ciudad ID: {} eliminada exitosamente.", id);
            return true;
        }
        logger.warn("Eliminación fallida: Ciudad ID: {} no existe.", id);
        return false;
    }

}