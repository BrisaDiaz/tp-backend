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
@EnableMethodSecurity // Permite usar @PreAuthorize a nivel de método (para lógica de propiedad)
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

                // === ZONAS PÚBLICAS ===
                // Permite el registro de nuevos clientes (POST /clientes), ya que aún no tienen token
                .requestMatchers(HttpMethod.POST, "/clientes").permitAll()
                // Otras rutas designadas como públicas
                .requestMatchers("/publico/**").permitAll()

                // === ACCESO LOGISTICA (Service-to-Service) ===
                // 2. Solo Logística puede actualizar estados de Solicitudes y Contenedores
                .requestMatchers(HttpMethod.PUT, "/solicitudes/{id}/programada").hasRole("LOGISTICA")
                .requestMatchers(HttpMethod.PUT, "/solicitudes/{id}/en-transito").hasRole("LOGISTICA")
                .requestMatchers(HttpMethod.PUT, "/solicitudes/{id}/entregada").hasRole("LOGISTICA")
                .requestMatchers(HttpMethod.PUT, "/solicitudes/{id}/contenedor/**").hasRole("LOGISTICA")

                // === ACCESO CLIENTE ===
                // 3. Crear Solicitudes (POST /solicitudes)
                .requestMatchers(HttpMethod.POST, "/solicitudes").hasRole("CLIENTE")

                // 4. Consultas de Clientes (Datos propios, Solicitudes propias, Histórico)
                // Usamos 'authenticated()' aquí y luego @PreAuthorize en los Controllers
                // para imponer la restricción de "solo ver lo propio".
                .requestMatchers(HttpMethod.GET, "/clientes/{id}").authenticated() 
                .requestMatchers(HttpMethod.PUT, "/clientes/{id}").authenticated() // Cliente puede actualizar sus propios datos
                .requestMatchers(HttpMethod.GET, "/solicitudes/{id}").authenticated()
                .requestMatchers(HttpMethod.GET, "/solicitudes/{id}/contenedor/seguimiento").authenticated()

                // === ACCESO ADMIN / GESTIÓN ===
                // 5. Contenedores: Listados y detalles (Administración)
                .requestMatchers("/contenedores/**").hasAnyRole("ADMIN", "LOGISTICA") 
                // 6. Solicitudes: Listado de borradores (Administración)
                .requestMatchers(HttpMethod.GET, "/solicitudes/borrador").hasRole("ADMIN")

                // 7. RESTO: Todas las demás operaciones (Listados generales, PUT/DELETE de Clientes/Solicitudes, etc.)
                // Quedan restringidas al rol más alto (ADMIN).
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