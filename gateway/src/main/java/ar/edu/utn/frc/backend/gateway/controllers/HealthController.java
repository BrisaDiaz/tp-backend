package ar.edu.utn.frc.backend.gateway.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, Object> index() {
        return Map.of(
            "service", "API Gateway",
            "status", "UP",
            "message", "Gateway operativo 🚀"
        );
    }
}
