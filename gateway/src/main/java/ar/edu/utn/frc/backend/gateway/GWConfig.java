package ar.edu.utn.frc.backend.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GWConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // ----------------------------------------------------
            // 1. RUTA: Servicio de Recursos
            // Equivalente a: /api/recursos/** -> http://recursos:8081/**
            // ----------------------------------------------------
            .route("servicio-recursos", r -> r
                .path("/api/recursos/**")
                .uri("http://recursos:8081"))

            // ----------------------------------------------------
            // 2. RUTA: Servicio de Solicitudes
            // Equivalente a: /api/solicitudes/** -> http://solicitudes:8082/**
            // ----------------------------------------------------
            .route("servicio-solicitudes", r -> r
                .path("/api/solicitudes/**")
                .uri("http://solicitudes:8082"))
            
            // ----------------------------------------------------
            // 3. RUTA: Servicio de Logística
            // Equivalente a: /api/logistica/** -> http://logistica:8083/**
            // ----------------------------------------------------
            .route("servicio-logistica", r -> r
                .path("/api/logistica/**")
                .uri("http://logistica:8083"))
            

            .route("postman-echo-get", r -> r
                .path("/get")
                .uri("http://postman-echo.com"))

            .build();
    }
}