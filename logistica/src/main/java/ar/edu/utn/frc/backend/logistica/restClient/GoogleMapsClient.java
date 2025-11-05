package ar.edu.utn.frc.backend.logistica.restClient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

import ar.edu.utn.frc.backend.logistica.dto.helpers.DistanciaDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GoogleMapsClient {
    
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey; // Agregar este campo

    // Modificar el constructor para recibir el API Key
    public GoogleMapsClient(
            @Qualifier("googleMapsRestClient") RestClient restClient,
            @Value("${app.google.maps.api-key}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        log.info("GoogleMapsClient inicializado con API Key: {}", 
                 apiKey != null ? "PRESENTE (longitud: " + apiKey.length() + ")" : "AUSENTE");
    }

    public DistanciaDto calcularDistancia(Float latitudOrg, Float LongitudOrg, 
                                         Float latitudDest, Float LongitudDest) {
        try {
            String origen = latitudOrg + "," + LongitudOrg;
            String destino = latitudDest + "," + LongitudDest;

            String uri = String.format("?destinations=%s&origins=%s&units=metric&key=%s", 
                                     destino, origen, apiKey);

            log.debug("Consultando Google Maps Distance Matrix: {}", 
                     uri.replace(apiKey, "API_KEY_REDACTED")); // Log seguro
            
            String response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            log.debug("Google Maps response: {}", response);
            
            JsonNode root = objectMapper.readTree(response);
            
            // Verificar status general
            String apiStatus = root.path("status").asText();
            if (!"OK".equals(apiStatus)) {
                log.error("Google Maps API error status: {}", apiStatus);
                return crearDistanciaFallback(origen, destino, latitudOrg, LongitudOrg, latitudDest, LongitudDest);
            }
            
            JsonNode rows = root.path("rows");
            if (rows.isEmpty() || !rows.has(0)) {
                log.error("No rows in Google Maps response");
                return crearDistanciaFallback(origen, destino, latitudOrg, LongitudOrg, latitudDest, LongitudDest);
            }
            
            JsonNode elements = rows.get(0).path("elements");
            if (elements.isEmpty() || !elements.has(0)) {
                log.error("No elements in Google Maps response");
                return crearDistanciaFallback(origen, destino, latitudOrg, LongitudOrg, latitudDest, LongitudDest);
            }
            
            JsonNode element = elements.get(0);
            String elementStatus = element.path("status").asText();
            
            if ("OK".equals(elementStatus)) {
                JsonNode distanceNode = element.path("distance");
                JsonNode durationNode = element.path("duration");
                
                if (distanceNode.isMissingNode() || durationNode.isMissingNode()) {
                    log.error("Missing distance or duration in Google Maps response");
                    return crearDistanciaFallback(origen, destino, latitudOrg, LongitudOrg, latitudDest, LongitudDest);
                }
                
                double metros = distanceNode.path("value").asDouble();
                String duracionTexto = durationNode.path("text").asText();
                
                // CORRECCIÓN: Usar asLong() en lugar de casting
                Long duracionSegundos = durationNode.path("value").asLong();
                
                log.info("Google Maps response - Distance: {} km, Duration: {} segundos", 
                        metros / 1000.0, duracionSegundos);
                
                return DistanciaDto.builder()
                    .origen(origen)
                    .destino(destino)
                    .kilometros(metros / 1000.0)
                    .duracionTexto(duracionTexto)
                    .duracionSegundos(duracionSegundos)
                    .build();
            } else {
                log.warn("Google Maps element status: {}", elementStatus);
                return crearDistanciaFallback(origen, destino, latitudOrg, LongitudOrg, latitudDest, LongitudDest);
            }
            
        } catch (Exception e) {
            log.error("Error calling Google Maps API: {}", e.getMessage(), e);
            return crearDistanciaFallback(
                latitudOrg + "," + LongitudOrg, 
                latitudDest + "," + LongitudDest,
                latitudOrg, LongitudOrg, latitudDest, LongitudDest
            );
        }
    }

    private DistanciaDto crearDistanciaFallback(String origen, String destino, 
                                               Float lat1, Float lon1, Float lat2, Float lon2) {
        double distancia = calcularDistanciaHaversine(lat1, lon1, lat2, lon2);
        long segundos = (long) ((distancia / 80.0) * 3600); // 80 km/h promedio
        
        String duracionTexto;
        int horas = (int) (segundos / 3600);
        int minutos = (int) ((segundos % 3600) / 60);
        
        if (horas > 0) {
            duracionTexto = horas + " hour" + (horas > 1 ? "s" : "") + " " + minutos + " min" + (minutos > 1 ? "s" : "");
        } else {
            duracionTexto = minutos + " min" + (minutos > 1 ? "s" : "");
        }
        
        log.info("Using fallback distance: {} km, {} segundos", distancia, segundos);
        
        return DistanciaDto.builder()
            .origen(origen)
            .destino(destino)
            .kilometros(distancia)
            .duracionTexto(duracionTexto)
            .duracionSegundos(segundos)
            .build();
    }
    
    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}