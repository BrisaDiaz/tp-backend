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

            // Servicio de Recursos: Camiones
            .route("recursos-camiones", r -> r
                .path("/api/camiones/**")
                .uri("http://recursos:8081"))  // Puerto interno 8081

            // Servicio de Recursos: Depositos
            .route("recursos-depositos", r -> r
                .path("/api/depositos/**")
                .uri("http://recursos:8081"))  // Puerto interno 8081

            // Servicio de Recursos: Tarifas
            .route("recursos-tarifas", r -> r
                .path("/api/tarifas/**")
                .uri("http://recursos:8081"))  // Puerto interno 8081
                
            // Servicio de Recursos: Ciudades
            .route("recursos-ciudades", r -> r
                .path("/api/ciudades/**")
                .uri("http://recursos:8081"))  // Puerto interno 8081

            // Servicio de Solicitudes
            .route("solicitudes-clientes", r -> r
                .path("/api/clientes/**")
                .uri("http://solicitudes:8082"))  // Puerto interno 8082
            
            .route("solicitudes-solicitudes", r -> r
                .path("/api/solicitudes/**")
                .uri("http://solicitudes:8082"))  // Puerto interno 8082
                
            .route("solicitudes-contenedores", r -> r
                .path("/api/contenedores/**")
                .uri("http://solicitudes:8082"))  // Puerto interno 8082

            // Servicio de Logística
            .route("logistica-rutas", r -> r
                .path("/api/rutas/**")
                .uri("http://logistica:8083"))  // Puerto interno 8083
                
            .route("logistica-tramos", r -> r
                .path("/api/tramos/**")
                .uri("http://logistica:8083"))  // Puerto interno 8083

            .build();
    }
}