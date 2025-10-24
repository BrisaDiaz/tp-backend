package ar.edu.utn.frc.backend.recursos.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.utn.frc.backend.recursos.dto.PrecioCombustibleDto;
import ar.edu.utn.frc.backend.recursos.entities.PrecioCombustible;
import ar.edu.utn.frc.backend.recursos.repositories.PrecioCombustibleRepository;
import jakarta.transaction.Transactional;

@Service
public class PrecioCombustibleService {

    @Autowired
    private PrecioCombustibleRepository precioCombustibleRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Guardar un nuevo precio de combustible, finalizando el vigente
    @Transactional
    public PrecioCombustibleDto guardarNuevoPrecio(PrecioCombustibleDto newPriceDto) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Finalizar la vigencia del precio actual (fechaHoraHasta = NULL)
        List<PrecioCombustible> currentPrices = precioCombustibleRepository.findByFechaHoraHastaIsNullOrderByFechaHoraDesdeDesc();
        if (!currentPrices.isEmpty()) {
            PrecioCombustible currentPrice = currentPrices.get(0);
            currentPrice.setFechaHoraHasta(now);
            precioCombustibleRepository.save(currentPrice);
        }

        // 2. Crear y guardar el nuevo precio vigente
        PrecioCombustible newPrice = modelMapper.map(newPriceDto, PrecioCombustible.class);
        newPrice.setFechaHoraDesde(now);
        newPrice.setFechaHoraHasta(null); // Vigente

        PrecioCombustible savedPrice = precioCombustibleRepository.save(newPrice);
        return modelMapper.map(savedPrice, PrecioCombustibleDto.class);
    }

    // Obtiene el precio actualmente vigente (fechaHoraHasta = NULL).
    public Optional<PrecioCombustibleDto> buscarPrecioVigente() {
        List<PrecioCombustible> currentPrices = precioCombustibleRepository.findByFechaHoraHastaIsNullOrderByFechaHoraDesdeDesc();
        if (!currentPrices.isEmpty()) {
            PrecioCombustible currentPrice = currentPrices.get(0);
            return Optional.of(modelMapper.map(currentPrice, PrecioCombustibleDto.class));
        } else {
            return Optional.empty();
        }
    }
}