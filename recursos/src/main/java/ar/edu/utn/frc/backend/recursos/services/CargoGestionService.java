package ar.edu.utn.frc.backend.recursos.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.backend.recursos.dto.CargoGestionDto;
import ar.edu.utn.frc.backend.recursos.entities.CargoGestion;
import ar.edu.utn.frc.backend.recursos.repositories.CargoGestionRepository;
import jakarta.transaction.Transactional;

@Service
public class CargoGestionService {

    private static final Logger logger = LoggerFactory.getLogger(CargoGestionService.class);

    @Autowired
    private CargoGestionRepository cargoGestionRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Guardar un nuevo precio de combustible, finalizando el vigente
    @Transactional
    public CargoGestionDto guardarNuevoCargo(CargoGestionDto newPriceDto) {
        logger.info("Iniciando: Guardar nuevo cargo de gestión con valor: {}", newPriceDto.getCostoPorTramo());
        LocalDateTime now = LocalDateTime.now();

        // 1. Finalizar la vigencia del precio actual (fechaHoraHasta = NULL)
        List<CargoGestion> currentPrices = cargoGestionRepository.findByFechaHoraHastaIsNullOrderByFechaHoraDesdeDesc();
        if (!currentPrices.isEmpty()) {
            CargoGestion currentPrice = currentPrices.get(0);
            currentPrice.setFechaHoraHasta(now);
            cargoGestionRepository.save(currentPrice);
            logger.info("Cargo de gestión anterior (ID: {}) finalizado con fecha: {}", currentPrice.getId(), now);
        } else {
            logger.info("No se encontró ningún cargo de gestión vigente anterior para finalizar.");
        }

        // 2. Crear y guardar el nuevo precio vigente
        CargoGestion newPrice = modelMapper.map(newPriceDto, CargoGestion.class);
        newPrice.setFechaHoraDesde(now);
        newPrice.setFechaHoraHasta(null); // Vigente

        CargoGestion savedPrice = cargoGestionRepository.save(newPrice);
        logger.info("Finalizado: Nuevo cargo de gestión (ID: {}) guardado e iniciado su vigencia desde: {}", savedPrice.getId(), now);
        return modelMapper.map(savedPrice, CargoGestionDto.class);
    }

    // Obtiene el precio actualmente vigente (fechaHoraHasta = NULL).
    public Optional<CargoGestionDto> buscarCargoVigente() {
        logger.debug("Buscando cargo de gestión actualmente vigente.");
        List<CargoGestion> currentPrices = cargoGestionRepository.findByFechaHoraHastaIsNullOrderByFechaHoraDesdeDesc();
        if (!currentPrices.isEmpty()) {
            CargoGestion currentPrice = currentPrices.get(0);
            logger.info("Cargo de gestión vigente encontrado (ID: {}, Valor: {}).", currentPrice.getId(), currentPrice.getCostoPorTramo());
            return Optional.of(modelMapper.map(currentPrice, CargoGestionDto.class));
        } else {
            logger.warn("No se encontró ningún cargo de gestión actualmente vigente.");
            return Optional.empty();
        }
    }
}