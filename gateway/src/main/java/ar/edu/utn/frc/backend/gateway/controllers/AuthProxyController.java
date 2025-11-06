package ar.edu.utn.frc.backend.gateway.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/auth")
public class AuthProxyController {

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    // Se mantiene la inyección por compatibilidad con la configuración general, pero el valor no se usará.
    @Value("${keycloak.client-secret}")
    private String clientSecret;

    // Se recomienda WebClient en proyectos WebFlux, pero RestTemplate funcionará por ahora
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/token")
    public ResponseEntity<?> getAccessToken(@RequestParam String username, @RequestParam String password) {
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", keycloakUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        // Configuración de Content-Type y Accept
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));


        // **USO DE MultiValueMap para un envío de formulario más robusto y seguro**
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            // Intentar el intercambio con Keycloak
            ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);
            return ResponseEntity.ok(response.getBody());

        } catch (HttpClientErrorException e) {
            // Capturar errores HTTP específicos (como 401) de Keycloak y retornarlos
            // Esto evita que el Gateway muestre un error 500 interno si Keycloak responde con 401
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            // Capturar otros errores de conexión o parsing
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al comunicarse con el servidor de autenticación: " + e.getMessage());
        }
    }
}
