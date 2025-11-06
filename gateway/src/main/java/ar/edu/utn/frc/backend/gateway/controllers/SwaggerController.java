package ar.edu.utn.frc.backend.gateway.controllers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class SwaggerController {

    @Bean
    public RouterFunction<ServerResponse> swaggerRoutes() {
        return route(GET("/swagger").and(accept(MediaType.TEXT_HTML)), 
            request -> ServerResponse.temporaryRedirect(
                java.net.URI.create("/swagger-ui.html")
            ).build());
    }
}