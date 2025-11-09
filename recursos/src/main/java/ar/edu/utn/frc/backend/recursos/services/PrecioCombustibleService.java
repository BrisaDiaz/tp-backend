package ar.edu.utn.frc.backend.recursos.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.backend.recursos.dto.PrecioCombustibleDto;
import ar.edu.utn.frc.backend.recursos.entities.PrecioCombustible;
import ar.edu.utn.frc.backend.recursos.repositories.PrecioCombustibleRepository;
import jakarta.transaction.Transactional;

@Service
public class PrecioCombustibleService {

    private static final Logger logger = LoggerFactory.getLogger(PrecioCombustibleService.class);

    @Autowired
    private PrecioCombustibleRepository precioCombustibleRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Guardar un nuevo precio de combustible, finalizando el vigente
    @Transactional
    public PrecioCombustibleDto guardarNuevoPrecio(PrecioCombustibleDto newPriceDto) {
        logger.info("Iniciando: Guardar nuevo precio de combustible con valor: {}", newPriceDto.getPrecioPorLitro());
        LocalDateTime now = LocalDateTime.now();

        // 1. Finalizar la vigencia del precio actual (fechaHoraHasta = NULL)
        List<PrecioCombustible> currentPrices = precioCombustibleRepository.findByFechaHoraHastaIsNullOrderByFechaHoraDesdeDesc();
        if (!currentPrices.isEmpty()) {
            PrecioCombustible currentPrice = currentPrices.get(0);
            currentPrice.setFechaHoraHasta(now);
            precioCombustibleRepository.save(currentPrice);
            logger.info("Precio de combustible anterior (ID: {}) finalizado con fecha: {}", currentPrice.getId(), now);
        } else {
            logger.info("No se encontró ningún precio de combustible vigente anterior para finalizar.");
        }

        // 2. Crear y guardar el nuevo precio vigente
        PrecioCombustible newPrice = modelMapper.map(newPriceDto, PrecioCombustible.class);
        newPrice.setFechaHoraDesde(now);
        newPrice.setFechaHoraHasta(null); // Vigente

        PrecioCombustible savedPrice = precioCombustibleRepository.save(newPrice);
        logger.info("Finalizado: Nuevo precio de combustible (ID: {}) guardado e iniciado su vigencia desde: {}", savedPrice.getPrecioPorLitro(), now);
        return modelMapper.map(savedPrice, PrecioCombustibleDto.class);
    }

    // Obtiene el precio actualmente vigente (fechaHoraHasta = NULL).
    public Optional<PrecioCombustibleDto> buscarPrecioVigente() {
        logger.debug("Buscando precio de combustible actualmente vigente.");
        List<PrecioCombustible> currentPrices = precioCombustibleRepository.findByFechaHoraHastaIsNullOrderByFechaHoraDesdeDesc();
        if (!currentPrices.isEmpty()) {
            PrecioCombustible currentPrice = currentPrices.get(0);
            logger.info("Precio de combustible vigente encontrado (ID: {}, Valor: {}).", currentPrice.getId(), currentPrice.getPrecioPorLitro());
            return Optional.of(modelMapper.map(currentPrice, PrecioCombustibleDto.class));
        } else {
            logger.warn("No se encontró ningún precio de combustible actualmente vigente.");
            return Optional.empty();
        }
    }
}