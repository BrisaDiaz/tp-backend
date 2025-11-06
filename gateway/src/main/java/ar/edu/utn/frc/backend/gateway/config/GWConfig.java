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
                .filters(f -> f.rewritePath("/recursos/(?<segment>.*)", "/${segment}"))
                .uri("http://recursos:8081"))

            // Swagger UI para Solicitudes
            .route("solicitudes-swagger", r -> r
                .path("/solicitudes/swagger-ui/**", "/solicitudes/v3/api-docs/**")
                .filters(f -> f.rewritePath("/solicitudes/(?<segment>.*)", "/${segment}"))
                .uri("http://solicitudes:8082"))

            // Swagger UI para Logística
            .route("logistica-swagger", r -> r
                .path("/logistica/swagger-ui/**", "/logistica/v3/api-docs/**")
                .filters(f -> f.rewritePath("/logistica/(?<segment>.*)", "/${segment}"))
                .uri("http://logistica:8083"))

            // ========== RUTAS DE API ==========

            // Servicio de Recursos
            .route("recursos-service", r -> r
                .path("/api/recursos/**")
                .filters(f -> f.rewritePath("/api/recursos/(?<segment>.*)", "/api/${segment}"))
                .uri("http://recursos:8081"))

            // Servicio de Solicitudes
            .route("solicitudes-service", r -> r
                .path("/api/solicitudes/**")
                .filters(f -> f.rewritePath("/api/solicitudes/(?<segment>.*)", "/api/${segment}"))
                .uri("http://solicitudes:8082"))

            // Servicio de Logística
            .route("logistica-service", r -> r
                .path("/api/logistica/**")
                .filters(f -> f.rewritePath("/api/logistica/(?<segment>.*)", "/api/${segment}"))
                .uri("http://logistica:8083"))

            // Rutas específicas para mantener compatibilidad
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