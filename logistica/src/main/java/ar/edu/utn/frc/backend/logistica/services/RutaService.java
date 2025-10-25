package ar.edu.utn.frc.backend.logistica.services;

import ar.edu.utn.frc.backend.logistica.dto.CamionDto;
import ar.edu.utn.frc.backend.logistica.dto.DepositoDto;
import ar.edu.utn.frc.backend.logistica.dto.SolicitudTransporteDto;
import ar.edu.utn.frc.backend.logistica.dto.RutaDto;
import ar.edu.utn.frc.backend.logistica.exceptions.RecursoNoDisponibleException;
import ar.edu.utn.frc.backend.logistica.restClient.RecursosClient;
import ar.edu.utn.frc.backend.logistica.restClient.SolicitudesClient;
import ar.edu.utn.frc.backend.logistica.util.KShortestPathFinder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

@Service
public class RutaService {
    @Autowired
    private SolicitudesClient solicitudesClient;
    @Autowired
    private RecursosClient recursosClient;
    @Autowired
    private KShortestPathFinder pathFinder;

    private static final int SCALE = 2;

    // Método helper para calcular promedios de BigDecimal
    private BigDecimal calculateAverage(List<CamionDto> camiones, Function<CamionDto, BigDecimal> extractor) {
        if (camiones.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sumaTotal = camiones.stream()
                .map(extractor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal divisor = new BigDecimal(camiones.size());

        // División con modo de redondeo explícito
        return sumaTotal.divide(divisor, SCALE, RoundingMode.HALF_UP);
    }

    // Genera rutas tentativas para una solicitud de transporte dada su ID.
    public List<RutaDto> obtenerTentativas(Integer solicitudId) {
        SolicitudTransporteDto solicitud;
        List<DepositoDto> depositos;
        BigDecimal costoCombustiblePorKm;
        BigDecimal cargoGestion;
        List<CamionDto> camionesDisponibles;

        // 1. OBTENER SOLICITUD
        try {
            solicitud = solicitudesClient.getSolicitudById(solicitudId);
            if (solicitud == null) {
                throw new RecursoNoDisponibleException("Solicitud de transporte no encontrada para el ID: " + solicitudId);
            }
        } catch (RestClientException e) {
            throw new RecursoNoDisponibleException("Error al obtener la Solicitud del servicio 'Solicitudes'.", e);
        }

        // Extracción de datos del contenedor
        BigDecimal volumen = solicitud.getContenedor().getVolumen();
        BigDecimal peso = solicitud.getContenedor().getPeso();

        // 2. OBTENER DEPÓSITOS, TARIFAS Y CAMIONES con manejo de errores de conexión
        try {
            depositos = recursosClient.getDepositos();
            costoCombustiblePorKm = recursosClient.getCostoCombustiblePorLitro();
            cargoGestion = recursosClient.getCargoPorGestion();
            camionesDisponibles = recursosClient.getCamionesDisponibles(volumen, peso);
        } catch (RestClientException e) {
            throw new RecursoNoDisponibleException("Error de comunicación con el servicio 'Recursos'.", e);
        }
        
        // 3. VALIDACIÓN DE DATOS OBTENIDOS (Abortar si son nulos o vacíos)
        if (depositos == null || depositos.isEmpty()) {
            throw new RecursoNoDisponibleException("No se encontraron depósitos intermedios disponibles.");
        }
        if (costoCombustiblePorKm == null || cargoGestion == null) {
            throw new RecursoNoDisponibleException("No se pudieron obtener los parámetros de costo (Combustible o Gestión).");
        }
        if (camionesDisponibles == null || camionesDisponibles.isEmpty()) {
            throw new RecursoNoDisponibleException("No hay camiones disponibles que cumplan con la capacidad del contenedor.");
        }

        // 4. CÁLCULO DE PROMEDIOS (Refactorizado con método helper)
        BigDecimal consumoPromedioCombustible = calculateAverage(camionesDisponibles, CamionDto::getConsumoCombustiblePromedio);
        BigDecimal costoBasePromedioPorKm = calculateAverage(camionesDisponibles, CamionDto::getCostoPorKm);

        // 5. PREPARACIÓN DE DATOS Y GENERACIÓN DE RUTAS
        DepositoDto depositoOrigen = solicitud.getDepositoOrigen();
        DepositoDto depositoDestino = solicitud.getDepositoDestino();
        
        // Eliminar los depósitos de origen y destino de la lista de intermedios
        List<DepositoDto> depositosIntermedios = depositos.stream()
                .filter(dep -> !dep.getId().equals(depositoOrigen.getId()) && !dep.getId().equals(depositoDestino.getId()))
                .toList();

        // Generar las rutas tentativas utilizando el algoritmo K-Shortest Path.
        return pathFinder.generarRutasTentativas(
                depositoOrigen,
                depositoDestino,
                depositosIntermedios,
                consumoPromedioCombustible,
                costoBasePromedioPorKm,
                costoCombustiblePorKm,
                cargoGestion
        );
    }
}
