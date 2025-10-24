package ar.edu.utn.frc.backend.recursos.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
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

    @Autowired
    private DepositoRepository depositoRepository;
    @Autowired
    private CiudadRepository ciudadRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public DepositoDto guardarDeposito(DepositoDto depositoDto) {
        // 1. Verificar existencia de Ciudad
        Ciudad ciudad = ciudadRepository.findByNombre(depositoDto.getCiudad())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Ciudad", depositoDto.getCiudad()));

        // 2. Mapear Deposito y enlazar
        Deposito deposito = modelMapper.map(depositoDto, Deposito.class);
        deposito.setCiudad(ciudad);

        Deposito depositoGuardado = depositoRepository.save(deposito);

        return convertToDto(depositoGuardado);
    }

    public Optional<DepositoDto> buscarPorId(Integer id) {
        return depositoRepository.findById(id).map(this::convertToDto);
    }

    public List<DepositoDto> buscarTodosLosDepositos() {
        return depositoRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<DepositoDto> actualizarDeposito(Integer id, DepositoDto depositoDto) {
        Deposito depositoExistente = depositoRepository.findById(id)
                // Se reemplaza RuntimeException por ResourceNotFoundException
                .orElseThrow(() -> new ResourceNotFoundException("Depósito", id));

        // 1. Verificar existencia de Ciudad
        Ciudad ciudad = ciudadRepository.findByNombre(depositoDto.getCiudad())
                .orElseThrow(() -> new ResourceNotFoundException("Ciudad", depositoDto.getCiudad()));

        // 2. Actualizar campos del Deposito
        depositoExistente.setNombre(depositoDto.getNombre());
        depositoExistente.setDireccion(depositoDto.getDireccion());
        depositoExistente.setPrecioPorDia(depositoDto.getPrecioPorDia());
        depositoExistente.setLatitud(depositoDto.getLatitud());
        depositoExistente.setLongitud(depositoDto.getLongitud());
        depositoExistente.setCiudad(ciudad);

        Deposito depositoGuardado = depositoRepository.save(depositoExistente);
        return Optional.ofNullable(convertToDto(depositoGuardado));
    }

    @Transactional
    public void eliminarDeposito(Integer id) {
        if (!depositoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Depósito", id);
        }
        depositoRepository.deleteById(id);
    }

    // Método auxiliar para mapeo de Entidad a DTO (maneja el campo Ciudad ID)
    private DepositoDto convertToDto(Deposito deposito) {
        DepositoDto dto = modelMapper.map(deposito, DepositoDto.class);
        dto.setCiudad(deposito.getCiudad().getNombre());
        return dto;
    }
}