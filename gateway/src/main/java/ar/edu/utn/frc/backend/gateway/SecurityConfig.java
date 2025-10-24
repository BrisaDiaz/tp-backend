package ar.edu.utn.frc.backend.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {


    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            // Deshabilita la protección CSRF (necesaria para APIs REST)
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            // Permite el acceso a todas las peticiones (sin autenticación)
            .authorizeExchange(exchanges -> exchanges
                .anyExchange().permitAll()
            );
        return http.build();
    }
}