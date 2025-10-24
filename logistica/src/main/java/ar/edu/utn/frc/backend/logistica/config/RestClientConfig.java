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
}
