package ar.edu.utn.frc.backend.logistica.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestClientConfig {
  
  @Bean(name = "recursosRestClient")
  RestClient recursosClient(@Value("${app.recursos.base-url}") String baseUrl) {
    return RestClient.builder().baseUrl(baseUrl).build();
  }

  @Bean(name = "solicitudesRestClient")
  RestClient solicitudesClient(@Value("${app.solicitudes.base-url}") String baseUrl) {
    return RestClient.builder().baseUrl(baseUrl).build();
  }

  @Bean(name = "googleMapsRestClient")
  RestClient googleMapsClient(
      @Value("${app.google.maps.distance-matrix-url}") String baseUrl,
      @Value("${app.google.maps.api-key}") String apiKey
  ) {
      // Configuración simple sin parámetros por defecto
      return RestClient.builder()
          .baseUrl(baseUrl)
          .build();
  }
}