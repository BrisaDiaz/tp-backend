package ar.edu.utn.frc.backend.logistica.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ar.edu.utn.frc.backend.logistica.dto.RutaDto;
import ar.edu.utn.frc.backend.logistica.entities.Ruta;

// 1. Marca la clase como configuración para que Spring la escanee.
@Configuration
public class ModelMapperConfig {
    // 2. Define un bean de ModelMapper para que pueda ser inyectado en otras partes de la aplicación.
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

    modelMapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT)
            .setSkipNullEnabled(true);

    // Configurar mapeo para Ruta -> RutaDto
    modelMapper.typeMap(Ruta.class, RutaDto.class)
        .addMappings(mapper -> {
            mapper.map(Ruta::getId, RutaDto::setId);
            mapper.map(Ruta::getCantidadTramos, RutaDto::setCantidadTramos);
            mapper.map(Ruta::getCantidadDepositos, RutaDto::setCantidadDepositos);
            mapper.map(src -> src.getSolicitud().getId(), RutaDto::setIdSolicitud);
            mapper.map(Ruta::getTramos, RutaDto::setTramos);
        });

        return modelMapper;
    }
}