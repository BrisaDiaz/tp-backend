package ar.edu.utn.frc.backend.logistica.config;

import java.util.Collection;
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
            // Deshabilitar CSRF (Crucial para APIs REST sin estado)
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                // Rutas públicas (Swagger/SpringDoc y Actuator)
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/publico/**").permitAll() 

                // === REGLAS ESPECÍFICAS DE LOGÍSTICA ===
                
                // Solo ADMIN puede crear/consultar rutas completas
                .requestMatchers(HttpMethod.POST, "/rutas/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/rutas/**").hasRole("ADMIN")
                
                // Solo ADMIN puede asignar un tramo a un camión
                .requestMatchers(HttpMethod.PUT, "/tramos/{id}/asignado").hasRole("ADMIN")
                
                // Solo TRANSPORTISTA puede ver/actualizar sus tramos
                .requestMatchers(HttpMethod.GET, "/tramos/asignado/{camionId}").hasRole("TRANSPORTISTA")
                .requestMatchers(HttpMethod.PUT, "/tramos/{id}/iniciado").hasRole("TRANSPORTISTA")
                .requestMatchers(HttpMethod.PUT, "/tramos/{id}/finalizado").hasRole("TRANSPORTISTA")
                
                // Cualquier otra solicitud requiere autenticación (Catch-all)
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    /**
     * Define el conversor para extraer los roles de Keycloak del claim 'realm_access'.
     */
    @Bean
    Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            // Manejo seguro de roles (asegura que realmAccess no sea nulo y que roles sea una lista)
            Collection<GrantedAuthority> authorities = List.of();
            
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