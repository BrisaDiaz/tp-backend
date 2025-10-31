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

            // Servicio de Recursos
            .route("servicio-recursos", r -> r
                .path("/api/recursos/**")
                .uri("http://recursos:8081"))

            // Servicio de Solicitudes
            .route("servicio-solicitudes", r -> r
                .path("/api/solicitudes/**")
                .uri("http://solicitudes:8082"))

            // Servicio de Logística
            .route("servicio-logistica", r -> r
                .path("/api/logistica/**")
                .uri("http://logistica:8083"))

            .build();
    }
}
