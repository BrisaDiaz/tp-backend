package ar.edu.utn.frc.backend.logistica.restClient;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import ar.edu.utn.frc.backend.logistica.dto.helpers.CostoYTiempoDto;
import ar.edu.utn.frc.backend.logistica.dto.helpers.InfoDepositoDto;
import ar.edu.utn.frc.backend.logistica.dto.SolicitudTransporteDto;

@Component
public class SolicitudesClient {
    private final RestClient restClient;

    public SolicitudesClient(@Qualifier("solicitudesRestClient") RestClient restClient) {
        this.restClient = restClient;
    };

    // Obtener la solicitud por ID
    public SolicitudTransporteDto getSolicitudById(Integer idSolicitud) {
        String uri = String.format("/solicitudes/%d", idSolicitud);

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(SolicitudTransporteDto.class);
    }

    // Obtener todas las solicitudes en estado "Borrador"
    public List<SolicitudTransporteDto> getSolicitudesBorrador() {
        String uri = "/solicitudes/borrador";

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<List<SolicitudTransporteDto>>() {
                });
    }

    // Actualizar la solicitud a programada
    public SolicitudTransporteDto actualizarSolicitudAProgramada(Integer idSolicitud, BigDecimal costoEstimado,
            Long tiempoEstimado) {
        CostoYTiempoDto costoYTiempoDto = CostoYTiempoDto.builder()
                .costo(costoEstimado)
                .tiempo(tiempoEstimado)
                .build();
        String uri = String.format("/solicitudes/%d/programada", idSolicitud);

        return restClient.put()
                .uri(uri)
                .body(costoYTiempoDto)
                .retrieve()
                .body(SolicitudTransporteDto.class);
    }

    // Actualizar la solicitud a en transito
    public SolicitudTransporteDto actualizarSolicitudAEnTransito(Integer idSolicitud) {
        String uri = String.format("/solicitudes/%d/en-transito", idSolicitud);

        return restClient.put()
                .uri(uri)
                .retrieve()
                .body(SolicitudTransporteDto.class);
    }

    // Actualizar la solicitud a programada
    public SolicitudTransporteDto actualizarSolicitudAEntregada(Integer idSolicitud, BigDecimal costoReal, Long tiempoReal) {
        CostoYTiempoDto costoYTiempoDto = CostoYTiempoDto.builder()
                .costo(costoReal)
                .tiempo(tiempoReal)
                .build();
        String uri = String.format("/solicitudes/%d/entregada", idSolicitud);

        return restClient.put()
                .uri(uri)
                .body(costoYTiempoDto)
                .retrieve()
                .body(SolicitudTransporteDto.class);
    }

    // Actualizar el contenedor de una solicitud a en viaje
    public SolicitudTransporteDto actualizarContenedorEnViaje(Integer idSolicitud, String nombreDeposito) {
        InfoDepositoDto infoDepositoDto = InfoDepositoDto.builder()
                .nombre(nombreDeposito)
                .build();
        String uri = String.format("/solicitudes/%d/contenedor/en-viaje", idSolicitud);

        return restClient.put()
                .uri(uri)
                .body(infoDepositoDto)
                .retrieve()
                .body(SolicitudTransporteDto.class);
    }

    // Actualizar el contenedor de una solicitud a en deposito
    public SolicitudTransporteDto actualizarContenedorEnDeposito(Integer idSolicitud, String nombreDeposito) {
        InfoDepositoDto infoDepositoDto = InfoDepositoDto.builder()
                .nombre(nombreDeposito)
                .build();
        String uri = String.format("/solicitudes/%d/contenedor/en-deposito", idSolicitud);

        return restClient.put()
                .uri(uri)
                .body(infoDepositoDto)
                .retrieve()
                .body(SolicitudTransporteDto.class);
    }
}
