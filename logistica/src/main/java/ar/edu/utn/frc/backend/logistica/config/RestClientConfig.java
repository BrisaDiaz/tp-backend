package ar.edu.utn.frc.backend.logistica.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
  @Bean
  RestClient recursosClient(@Value("${app.recursos.base-url}") String baseUrl) {
    return RestClient.builder().baseUrl(baseUrl).build();
  }

  @Bean
  RestClient solicitudesClient(@Value("${app.solicitudes.base-url}") String baseUrl) {
    return RestClient.builder().baseUrl(baseUrl).build();
  }

  @Bean
  RestClient googleMapsClient(
      @Value("${app.google.maps.distance-matrix-url}") String baseUrl,
      @Value("${app.google.maps.api-key}") String apiKey
  ) {
      return RestClient.builder()
          .baseUrl(baseUrl)
          // Añade la clave de API como parámetro de consulta fijo a todas las peticiones
          .defaultUriVariables(java.util.Collections.singletonMap("key", apiKey))
          .build();
    }
}
