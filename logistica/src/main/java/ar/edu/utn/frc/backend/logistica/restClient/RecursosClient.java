package ar.edu.utn.frc.backend.logistica.restClient;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ar.edu.utn.frc.backend.logistica.dto.CamionDto;
import ar.edu.utn.frc.backend.logistica.dto.DepositoDto;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RecursosClient {

    // Inyecta el RestClient configurado con el base-url de Recursos
    private final @Qualifier("recursosClient") RestClient restClient;

    // Obtener los camiones disponibles cuyo volumen y peso mínimos se especifican
    public List<CamionDto> getCamionesDisponibles(BigDecimal volumenMinimo, BigDecimal pesoMinimo) {
        String uri = String.format("/camiones/disponibles?volumen=%s&peso=%s", volumenMinimo, pesoMinimo);

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CamionDto>>() {
                });
    }

    // Marcar un camión como ocupado
    public CamionDto setCamionOcupado(Integer idCamion) {
        String uri = String.format("/camiones/%d/ocupado", idCamion);

        return restClient.put()
                .uri(uri)
                .retrieve()
                .body(CamionDto.class);
    }

    // Marcar un camión como libre.
    public CamionDto setCamionLibre(Integer idCamion) {
        String uri = String.format("/camiones/%d/libre", idCamion);

        return restClient.put()
                .uri(uri)
                .retrieve()
                .body(CamionDto.class);
    }

    // Obtener el costo del combustible por litro
    public BigDecimal getPrecioCombustible() {
        String uri = "/tarifas/combustible";

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(BigDecimal.class);
    }

    // Obtener el cargo por gestión
    public BigDecimal getCargoGestion() {
        String uri = "/tarifas/gestion";

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(BigDecimal.class);
    }

    // Obtener todos los depósitos
    public List<DepositoDto> getDepositos() {
        String uri = "/depositos";

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<List<DepositoDto>>() {
                });
    }

    // Obtener un depósito por su ID
    public DepositoDto getDepositoById(Integer idDeposito) {
        String uri = String.format("/depositos/%d", idDeposito);

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(DepositoDto.class);
    }
}
