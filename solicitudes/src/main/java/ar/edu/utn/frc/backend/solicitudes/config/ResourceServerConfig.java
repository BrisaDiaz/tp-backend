package ar.edu.utn.frc.backend.solicitudes.config;

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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permite usar @PreAuthorize a nivel de método
public class ResourceServerConfig {

    /**
     * Configura la cadena de filtros de seguridad para el servidor de recursos.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Deshabilitar CSRF (estándar en APIs REST sin cookies)
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize

                // === 1. RUTAS PUBLICAS (PERMIT ALL) ===
                
                // Dar de alta un cliente (Tu requerimiento)
                .requestMatchers(HttpMethod.POST, "/api/clientes").permitAll()
                // Rutas públicas genéricas
                .requestMatchers("/api/publico/**").permitAll()

                // LOGÍSTICA (Actualizaciones de estado que deben ser públicas/internas)
                .requestMatchers(HttpMethod.PUT, "/api/solicitudes/{id}/programada").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/solicitudes/{id}/en-transito").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/solicitudes/{id}/entregada").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/solicitudes/{id}/contenedor/**").permitAll()
                
                // === 2. RUTAS DE CLIENTES AUTENTICADOS (hasRole / authenticated) ===

                // Creación de Solicitud de envío
                .requestMatchers(HttpMethod.POST, "/api/solicitudes").hasRole("CLIENTE") 
                // Lectura de seguimiento, perfil y modificación de perfil (asume que la validación de propiedad se hace en el código)
                .requestMatchers(HttpMethod.GET, "/api/clientes/{id}").authenticated() 
                .requestMatchers(HttpMethod.PUT, "/api/clientes/{id}").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/solicitudes/{id}").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/solicitudes/{id}/contenedor/seguimiento").authenticated()

                // === 3. RUTAS DE ADMINISTRACIÓN (ADMIN) ===

                // Contenedores
                .requestMatchers("/api/contenedores/**").hasRole("ADMIN")
                // Solicitudes en borrador
                .requestMatchers(HttpMethod.GET, "/api/solicitudes/borrador").hasRole("ADMIN")

                // === 4. REGLA CATCH-ALL ===
                
                // Cualquier otra ruta no especificada arriba requiere el rol ADMIN
                .anyRequest().hasRole("ADMIN")

            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    /**
     * Define el conversor para extraer los roles de Keycloak.
     * Convierte el claim 'realm_access.roles' del JWT en autoridades de Spring Security (ROLE_ROL).
     */
    @Bean
    Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            // El claim "realm_access" es donde Keycloak almacena los roles del Realm.
            @SuppressWarnings("unchecked")
            Map<String, List<String>> realmAccess = jwt.getClaim("realm_access");

            // Mapea los roles, asegurándose de que tengan el prefijo 'ROLE_' y estén en mayúsculas.
            List<SimpleGrantedAuthority> authorities = realmAccess != null && realmAccess.containsKey("roles")
                ? realmAccess.get("roles").stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList())
                : List.of(); // Si no hay roles, devuelve una lista vacía.
            
            return new JwtAuthenticationToken(jwt, authorities);
        };
    }
}