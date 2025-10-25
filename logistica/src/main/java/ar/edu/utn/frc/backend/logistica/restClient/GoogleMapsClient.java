package ar.edu.utn.frc.backend.logistica.restClient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import ar.edu.utn.frc.backend.logistica.dto.helpers.DistanciaDto;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GoogleMapsClient {
    // Inyecta el RestClient configurado con el base-url de Google Maps Distance Matrix API
    private final @Qualifier("googleMapsClient") RestClient restClient;

    // Calcular distancia entre dos direcciones
    public DistanciaDto calcularDistancia(Float latitudOrg, Float longitudOrg, Float latitudDest, Float longitudDest) {
        String origen = latitudOrg + "," + longitudOrg;
        String destino = latitudDest + "," + longitudDest;

        String uri = String.format("?origins=%s&destinations=%s&units=metric", origen, destino);

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(DistanciaDto.class);
    }

}
