package ar.edu.utn.frc.backend.logistica.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 1. Marca la clase como configuración para que Spring la escanee.
@Configuration
public class ModelMapperConfig {
    // 2. Define un bean de ModelMapper para que pueda ser inyectado en otras partes de la aplicación.
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}