package ar.edu.utn.frc.backend.recursos.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.backend.recursos.dto.CargoGestionDto;
import ar.edu.utn.frc.backend.recursos.entities.CargoGestion;
import ar.edu.utn.frc.backend.recursos.repositories.CargoGestionRepository;
import jakarta.transaction.Transactional;

@Service
public class CargoGestionService {

    @Autowired
    private CargoGestionRepository cargoGestionRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Guardar un nuevo precio de combustible, finalizando el vigente
    @Transactional
    public CargoGestionDto guardarNuevoCargo(CargoGestionDto newPriceDto) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Finalizar la vigencia del precio actual (fechaHoraHasta = NULL)
        List<CargoGestion> currentPrices = cargoGestionRepository.findByFechaHoraHastaIsNullOrderByFechaHoraDesdeDesc();
        if (!currentPrices.isEmpty()) {
            CargoGestion currentPrice = currentPrices.get(0);
            currentPrice.setFechaHoraHasta(now);
            cargoGestionRepository.save(currentPrice);
        }

        // 2. Crear y guardar el nuevo precio vigente
        CargoGestion newPrice = modelMapper.map(newPriceDto, CargoGestion.class);
        newPrice.setFechaHoraDesde(now);
        newPrice.setFechaHoraHasta(null); // Vigente

        CargoGestion savedPrice = cargoGestionRepository.save(newPrice);
        return modelMapper.map(savedPrice, CargoGestionDto.class);
    }

    // Obtiene el precio actualmente vigente (fechaHoraHasta = NULL).
    public Optional<CargoGestionDto> buscarCargoVigente() {
        List<CargoGestion> currentPrices = cargoGestionRepository.findByFechaHoraHastaIsNullOrderByFechaHoraDesdeDesc();
        if (!currentPrices.isEmpty()) {
            CargoGestion currentPrice = currentPrices.get(0);
            return Optional.of(modelMapper.map(currentPrice, CargoGestionDto.class));
        } else {
            return Optional.empty();
        }
    }
}