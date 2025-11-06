package ar.edu.utn.frc.backend.gateway.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ApiDocsController {

    @Autowired
    private WebClient webClient;

    @Value("${app.services.logistica:http://logistica:8083}")
    private String logisticaUrl;

    @Value("${app.services.solicitudes:http://solicitudes:8082}")
    private String solicitudesUrl;

    @Value("${app.services.recursos:http://recursos:8081}")
    private String recursosUrl;

    @GetMapping("/v3/api-docs/logistica")
    public Mono<ResponseEntity<Map>> getLogisticaDocs() {
        return webClient.get()
                .uri(logisticaUrl + "/v3/api-docs")
                .retrieve()
                .toEntity(Map.class)
                .onErrorResume(e -> {
                    // Fallback si el servicio no está disponible
                    return Mono.just(ResponseEntity.ok(createFallbackDocs(
                        "Logística Service", 
                        "Servicio no disponible temporalmente"
                    )));
                });
    }

    @GetMapping("/v3/api-docs/solicitudes")
    public Mono<ResponseEntity<Map>> getSolicitudesDocs() {
        return webClient.get()
                .uri(solicitudesUrl + "/v3/api-docs")
                .retrieve()
                .toEntity(Map.class)
                .onErrorResume(e -> {
                    return Mono.just(ResponseEntity.ok(createFallbackDocs(
                        "Solicitudes Service", 
                        "Servicio no disponible temporalmente"
                    )));
                });
    }

    @GetMapping("/v3/api-docs/recursos")
    public Mono<ResponseEntity<Map>> getRecursosDocs() {
        return webClient.get()
                .uri(recursosUrl + "/v3/api-docs")
                .retrieve()
                .toEntity(Map.class)
                .onErrorResume(e -> {
                    return Mono.just(ResponseEntity.ok(createFallbackDocs(
                        "Recursos Service", 
                        "Servicio no disponible temporalmente"
                    )));
                });
    }

    @GetMapping("/v3/api-docs/swagger-config")
    public Map<String, Object> getSwaggerConfig() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("configUrl", "/v3/api-docs/swagger-config");
        config.put("oauth2RedirectUrl", "http://localhost:8080/swagger-ui/oauth2-redirect.html");
        
        // URLs para el dropdown de Swagger UI
        config.put("urls", List.of(
            Map.of("name", "Logística Service", "url", "/v3/api-docs/logistica"),
            Map.of("name", "Solicitudes Service", "url", "/v3/api-docs/solicitudes"),
            Map.of("name", "Recursos Service", "url", "/v3/api-docs/recursos")
        ));
        
        return config;
    }

    @GetMapping("/services")
    public Map<String, Object> getAvailableServices() {
        return Map.of(
            "services", List.of(
                Map.of(
                    "name", "Logística Service",
                    "description", "Gestión de rutas, tramos y camiones",
                    "basePath", "/api/logistica",
                    "docs", "/v3/api-docs/logistica",
                    "health", "/api/logistica/actuator/health"
                ),
                Map.of(
                    "name", "Solicitudes Service", 
                    "description", "Gestión de solicitudes de transporte y clientes",
                    "basePath", "/api/solicitudes",
                    "docs", "/v3/api-docs/solicitudes",
                    "health", "/api/solicitudes/actuator/health"
                ),
                Map.of(
                    "name", "Recursos Service",
                    "description", "Gestión de tarifas, combustibles y parámetros",
                    "basePath", "/api/recursos", 
                    "docs", "/v3/api-docs/recursos",
                    "health", "/api/recursos/actuator/health"
                )
            ),
            "gateway", Map.of(
                "url", "http://localhost:8080",
                "swagger", "/swagger-ui.html",
                "health", "/actuator/health"
            )
        );
    }

    private Map<String, Object> createFallbackDocs(String title, String description) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("openapi", "3.0.3");
        fallback.put("info", Map.of(
            "title", title,
            "description", description,
            "version", "1.0.0"
        ));
        fallback.put("servers", List.of(
            Map.of("url", "http://localhost:8080", "description", "API Gateway")
        ));
        fallback.put("paths", new HashMap<>());
        fallback.put("components", new HashMap<>());
        return fallback;
    }
}