package ar.edu.utn.frc.backend.solicitudes.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

    @Value("${app.openapi.dev-url:http://localhost:8080}")
    private String devUrl;

    @Value("${app.openapi.prod-url:https://solicitudes.produccion.com}")
    private String prodUrl;

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Servidor de Desarrollo");

        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("Servidor de Producción");

        Contact contact = new Contact();
        contact.setEmail("solicitudes@utn.edu.ar");
        contact.setName("Equipo Solicitudes");
        contact.setUrl("https://www.utn.edu.ar");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("API de Gestión de Solicitudes")
                .version("1.0.0")
                .contact(contact)
                .description("""
                    Esta API expone endpoints para gestionar solicitudes de transporte,
                    clientes, contenedores y seguimiento de envíos.
                    
                    ## Características principales:
                    - Gestión de clientes y sus datos
                    - Creación y seguimiento de solicitudes de transporte
                    - Control de estados de contenedores
                    - Histórico de seguimiento de envíos
                    
                    ## Roles de usuario:
                    - **ADMIN**: Acceso completo al sistema
                    - **CLIENTE**: Creación de solicitudes y acceso limitado a sus propias solicitudes y datos
                    
                    ## Tipos de endpoints:
                    - **Públicos internos**: Para comunicación entre microservicios (sin autenticación)
                    - **Protegidos**: Requieren autenticación JWT para usuarios
                    
                    ## Autenticación:
                    Esta API utiliza JWT Bearer Token. Para usar los endpoints protegidos:
                    1. Obtén un token de tu proveedor de identidad (Keycloak)
                    2. Haz clic en el botón "Authorize" ⬆️
                    3. Ingresa: `Bearer <tu-token-jwt>`
                    """)
                .license(mitLicense);

        // Configuración de seguridad JWT
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingrese su token JWT en el formato: Bearer <token>")));
    }
}