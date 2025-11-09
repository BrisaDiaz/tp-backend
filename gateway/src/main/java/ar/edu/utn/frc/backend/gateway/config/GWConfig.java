package ar.edu.utn.frc.backend.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GWConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

            // ========== RUTAS DE SWAGGER PARA CADA MICROSERVICIO ==========
            
            // Swagger UI para Recursos
            .route("recursos-swagger", r -> r
                .path("/recursos/swagger-ui/**", "/recursos/v3/api-docs/**")
                // Filtro necesario para quitar el prefijo /recursos
                .filters(f -> f.rewritePath("/recursos/(?<segment>.*)", "/${segment}"))
                .uri("http://recursos:8081"))

            // Swagger UI para Solicitudes
            .route("solicitudes-swagger", r -> r
                .path("/solicitudes/swagger-ui/**", "/solicitudes/v3/api-docs/**")
                // Filtro necesario para quitar el prefijo /solicitudes
                .filters(f -> f.rewritePath("/solicitudes/(?<segment>.*)", "/${segment}"))
                .uri("http://solicitudes:8082"))

            // Swagger UI para Logística
            .route("logistica-swagger", r -> r
                .path("/logistica/swagger-ui/**", "/logistica/v3/api-docs/**")
                // Filtro necesario para quitar el prefijo /logistica
                .filters(f -> f.rewritePath("/logistica/(?<segment>.*)", "/${segment}"))
                .uri("http://logistica:8083"))

            // ========== RUTAS DE API (PATH PRESERVADO) ==========

            // Servicio de Recursos: El path completo /api/recursos/** se envía a http://recursos:8081
            .route("recursos-service", r -> r
                .path("/api/recursos/**")
                .uri("http://recursos:8081"))

            // Servicio de Solicitudes: El path completo /api/solicitudes/** se envía a http://solicitudes:8082
            .route("solicitudes-service", r -> r
                .path("/api/solicitudes/**")
                .uri("http://solicitudes:8082"))

            // Servicio de Logística: El path completo /api/logistica/** se envía a http://logistica:8083
            .route("logistica-service", r -> r
                .path("/api/logistica/**")
                .uri("http://logistica:8083"))

            // Rutas específicas para mantener compatibilidad (Path Preservado)
            .route("recursos-camiones", r -> r
                .path("/api/camiones/**")
                .uri("http://recursos:8081"))

            .route("recursos-depositos", r -> r
                .path("/api/depositos/**")
                .uri("http://recursos:8081"))

            .route("recursos-tarifas", r -> r
                .path("/api/tarifas/**")
                .uri("http://recursos:8081"))
                
            .route("recursos-ciudades", r -> r
                .path("/api/ciudades/**")
                .uri("http://recursos:8081"))

            .route("solicitudes-clientes", r -> r
                .path("/api/clientes/**")
                .uri("http://solicitudes:8082"))
                
            .route("solicitudes-contenedores", r -> r
                .path("/api/contenedores/**")
                .uri("http://solicitudes:8082"))

            .route("logistica-rutas", r -> r
                .path("/api/rutas/**")
                .uri("http://logistica:8083"))
                
            .route("logistica-tramos", r -> r
                .path("/api/tramos/**")
                .uri("http://logistica:8083"))

            .build();
    }
}