package ar.edu.utn.frc.backend.recursos.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Rutas públicas (actuator)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                //LOGISTICA
                .requestMatchers(HttpMethod.GET, "/api/tarifas/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/camiones/{id}/libre").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/camiones/{id}/ocupado").permitAll()
                
                // === PERMISOS LOGISTICA ===
                
                // Camiones: Permite GET (consulta) y PUT (actualización de estado) a LOGISTICA
                .requestMatchers(HttpMethod.GET, "/api/camiones/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/camiones/{id}").hasRole("ADMIN")
                
                // Depósitos: Permite GET (consulta)
                .requestMatchers(HttpMethod.GET, "/api/depositos/**").hasRole("ADMIN")

                // === ACCIONES EXCLUSIVAS DE ADMIN ===
                
                // Camiones: POST (creación) y DELETE (eliminación)
                .requestMatchers(HttpMethod.POST, "/api/camiones/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/camiones/**").hasRole("ADMIN")
                
                // Depósitos: POST (creación), PUT (modificación de datos) y DELETE (eliminación)
                .requestMatchers(HttpMethod.POST, "/api/depositos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/depositos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/depositos/**").hasRole("ADMIN")
                
                // Ciudades: Todas las acciones
                .requestMatchers("/api/ciudades/**").hasRole("ADMIN")
                
                // Tarifas: POST (creación/modificación de tarifas)
                .requestMatchers(HttpMethod.POST, "/api/tarifas/**").hasRole("ADMIN")

                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .csrf(csrf -> csrf.disable()); // Deshabilitar CSRF para APIs

        return http.build();
    }

    @Bean
    Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            
            // Manejo seguro de roles
            List<GrantedAuthority> authorities = List.of();
            
            if (realmAccess != null && realmAccess.get("roles") instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                
                authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
            }
            
            return new JwtAuthenticationToken(jwt, authorities);
        };
    }
}
