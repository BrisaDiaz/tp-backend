package ar.edu.utn.frc.backend.recursos.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.backend.recursos.dto.DepositoDto;
import ar.edu.utn.frc.backend.recursos.entities.Ciudad;
import ar.edu.utn.frc.backend.recursos.entities.Deposito;
import ar.edu.utn.frc.backend.recursos.exceptions.ResourceNotFoundException;
import ar.edu.utn.frc.backend.recursos.repositories.CiudadRepository;
import ar.edu.utn.frc.backend.recursos.repositories.DepositoRepository;
import jakarta.transaction.Transactional;

@Service
public class DepositoService {

    private static final Logger logger = LoggerFactory.getLogger(DepositoService.class);

    @Autowired
    private DepositoRepository depositoRepository;
    @Autowired
    private CiudadRepository ciudadRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public DepositoDto guardarDeposito(DepositoDto depositoDto) {
        logger.info("Iniciando: Guardar nuevo depósito con nombre: {} en ciudad: {}", depositoDto.getNombre(), depositoDto.getCiudad());
        
        // 1. Verificar existencia de Ciudad
        Ciudad ciudad = ciudadRepository.findByNombre(depositoDto.getCiudad())
                .orElseThrow(
                        () -> {
                            logger.error("Error al guardar depósito: Ciudad '{}' no encontrada.", depositoDto.getCiudad());
                            return new ResourceNotFoundException("Ciudad", depositoDto.getCiudad());
                        });
        logger.debug("Ciudad '{}' encontrada (ID: {}).", ciudad.getNombre(), ciudad.getId());

        // 2. Mapear Deposito y enlazar
        Deposito deposito = modelMapper.map(depositoDto, Deposito.class);
        deposito.setCiudad(ciudad);

        Deposito depositoGuardado = depositoRepository.save(deposito);
        logger.info("Finalizado: Depósito guardado exitosamente con ID: {}", depositoGuardado.getId());

        return convertToDto(depositoGuardado);
    }

    public Optional<DepositoDto> buscarPorId(Integer id) {
        logger.debug("Buscando depósito por ID: {}", id);
        Optional<DepositoDto> depositoOpt = depositoRepository.findById(id).map(this::convertToDto);
        if (depositoOpt.isPresent()) {
            logger.info("Depósito ID: {} encontrado.", id);
        } else {
            logger.warn("Depósito ID: {} no encontrado.", id);
        }
        return depositoOpt;
    }

    public List<DepositoDto> buscarTodosLosDepositos() {
        logger.debug("Buscando todos los depósitos.");
        List<DepositoDto> depositos = depositoRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        logger.info("Encontrados {} depósitos en total.", depositos.size());
        return depositos;
    }

    @Transactional
    public Optional<DepositoDto> actualizarDeposito(Integer id, DepositoDto depositoDto) {
        logger.info("Iniciando: Actualización de depósito con ID: {}", id);

        Deposito depositoExistente = depositoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Error al actualizar: Depósito ID: {} no encontrado.", id);
                    return new ResourceNotFoundException("Depósito", id);
                });

        // 1. Verificar existencia de Ciudad
        Ciudad ciudad = ciudadRepository.findByNombre(depositoDto.getCiudad())
                .orElseThrow(() -> {
                    logger.error("Error al actualizar depósito: Ciudad '{}' no encontrada.", depositoDto.getCiudad());
                    return new ResourceNotFoundException("Ciudad", depositoDto.getCiudad());
                });

        // 2. Actualizar campos del Deposito
        depositoExistente.setNombre(depositoDto.getNombre());
        depositoExistente.setDireccion(depositoDto.getDireccion());
        depositoExistente.setPrecioPorDia(depositoDto.getPrecioPorDia());
        depositoExistente.setLatitud(depositoDto.getLatitud());
        depositoExistente.setLongitud(depositoDto.getLongitud());
        depositoExistente.setCiudad(ciudad);
        logger.debug("Datos de depósito ID: {} actualizados. Ciudad asignada: {}", id, ciudad.getNombre());

        Deposito depositoGuardado = depositoRepository.save(depositoExistente);
        logger.info("Finalizado: Depósito ID: {} actualizado exitosamente.", id);
        return Optional.ofNullable(convertToDto(depositoGuardado));
    }

    @Transactional
    public void eliminarDeposito(Integer id) {
        logger.info("Iniciando: Eliminación de depósito con ID: {}", id);
        if (!depositoRepository.existsById(id)) {
            logger.error("Error de eliminación: Depósito ID: {} no encontrado.", id);
            throw new ResourceNotFoundException("Depósito", id);
        }
        depositoRepository.deleteById(id);
        logger.info("Finalizado: Depósito ID: {} eliminado exitosamente.", id);
    }

    // Método auxiliar para mapeo de Entidad a DTO (maneja el campo Ciudad ID)
    private DepositoDto convertToDto(Deposito deposito) {
        DepositoDto dto = modelMapper.map(deposito, DepositoDto.class);
        dto.setCiudad(deposito.getCiudad().getNombre());
        return dto;
    }
}