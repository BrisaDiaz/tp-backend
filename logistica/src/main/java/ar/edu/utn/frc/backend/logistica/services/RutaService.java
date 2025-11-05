package ar.edu.utn.frc.backend.logistica.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import ar.edu.utn.frc.backend.logistica.dto.DepositoDto;
import ar.edu.utn.frc.backend.logistica.dto.RutaDto;
import ar.edu.utn.frc.backend.logistica.dto.RutaTentativaDto;
import ar.edu.utn.frc.backend.logistica.dto.TramoTentativoDto;
import ar.edu.utn.frc.backend.logistica.entities.Ruta;
import ar.edu.utn.frc.backend.logistica.entities.SolicitudTransporte;
import ar.edu.utn.frc.backend.logistica.entities.Deposito;
import ar.edu.utn.frc.backend.logistica.entities.Camion;
import ar.edu.utn.frc.backend.logistica.entities.Contenedor;
import ar.edu.utn.frc.backend.logistica.exceptions.RecursoNoDisponibleException;
import ar.edu.utn.frc.backend.logistica.exceptions.ResourceNotFoundException;
import ar.edu.utn.frc.backend.logistica.restClient.RecursosClient;
import ar.edu.utn.frc.backend.logistica.restClient.SolicitudesClient;
import ar.edu.utn.frc.backend.logistica.restClient.GoogleMapsClient;
import ar.edu.utn.frc.backend.logistica.dto.helpers.DistanciaDto;
import ar.edu.utn.frc.backend.logistica.repositories.RutaRepository;
import ar.edu.utn.frc.backend.logistica.repositories.SolicitudTransporteRepository;
import ar.edu.utn.frc.backend.logistica.repositories.DepositoRepository;
import ar.edu.utn.frc.backend.logistica.repositories.CamionRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RutaService {
    @Autowired
    private RecursosClient recursosClient;

    @Autowired
    private SolicitudesClient solicitudesClient;

    @Autowired
    private GoogleMapsClient googleMapsClient;

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private SolicitudTransporteRepository solicitudTransporteRepository;

    @Autowired
    private DepositoRepository depositoRepository;

    @Autowired
    private CamionRepository camionRepository;

    @Autowired
    private TramoService tramoService;

    @Autowired
    private ModelMapper modelMapper;

    private static final int SCALE = 2;

    // Método helper para calcular promedios de BigDecimal - ahora con entidades Camion
    private BigDecimal calculateAverage(List<Camion> camiones, Function<Camion, BigDecimal> extractor) {
        if (camiones.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sumaTotal = camiones.stream()
                .map(extractor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal divisor = new BigDecimal(camiones.size());

        return sumaTotal.divide(divisor, SCALE, RoundingMode.HALF_UP);
    }

    // Genera rutas tentativas para una solicitud de transporte dada su ID.
    public List<RutaTentativaDto> obtenerTentativas(Integer solicitudId) {
        log.info("Obteniendo rutas tentativas para solicitud ID: {}", solicitudId);
        
        // 1. OBTENER SOLICITUD desde el repository local
        Optional<SolicitudTransporte> solicitudOpt = solicitudTransporteRepository.findById(solicitudId);
        if (solicitudOpt.isEmpty()) {
            throw new ResourceNotFoundException("Solicitud de transporte no encontrada para el ID: " + solicitudId);
        }

        SolicitudTransporte solicitud = solicitudOpt.get();
        log.info("Solicitud obtenida: {} -> {}", 
                solicitud.getDepositoOrigen().getNombre(), 
                solicitud.getDepositoDestino().getNombre());

        // Obtener contenedor desde la relación de la solicitud
        Contenedor contenedor = solicitud.getContenedor();
        if (contenedor == null) {
            throw new ResourceNotFoundException("Contenedor no encontrado para la solicitud ID: " + solicitudId);
        }

        // Extracción de datos del contenedor
        BigDecimal volumen = contenedor.getVolumen();
        BigDecimal peso = contenedor.getPeso();
        log.info("Requisitos del contenedor - Volumen: {}, Peso: {}", volumen, peso);

        // Obtener depósitos desde el repository local
        List<Deposito> depositosEntities = depositoRepository.findAll();
        if (depositosEntities.isEmpty()) {
            throw new RecursoNoDisponibleException("No se encontraron depósitos disponibles.");
        }

        // Convertir a DTOs para mantener consistencia con la interfaz
        List<DepositoDto> depositos = depositosEntities.stream()
                .map(dep -> modelMapper.map(dep, DepositoDto.class))
                .collect(Collectors.toList());

        // 2. OBTENER TARIFAS Y CAMIONES 
        BigDecimal costoCombustiblePorLitro;
        BigDecimal cargoGestion;
        
        // Obtener camiones disponibles desde el repository local
        List<Camion> camionesDisponibles = camionRepository
                .findByDisponibilidadTrueAndCapacidadVolumenGreaterThanEqualAndCapacidadPesoGreaterThanEqual(volumen, peso);

        try {
            // Solo las tarifas se obtienen via RestClient
            costoCombustiblePorLitro = recursosClient.getCostoCombustiblePorLitro().getPrecioPorLitro();
            cargoGestion = recursosClient.getCargoPorGestion().getCostoPorTramo();

            log.info("Recursos obtenidos - Depósitos: {}, Camiones disponibles: {}, Costo combustible: {}, Cargo gestión: {}", 
                    depositos.size(), camionesDisponibles.size(), costoCombustiblePorLitro, cargoGestion);
        } catch (RestClientException e) {
            throw new RecursoNoDisponibleException("Error de comunicación con el servicio 'Recursos'.", e);
        }

        // 3. VALIDACIÓN DE DATOS OBTENIDOS
        if (costoCombustiblePorLitro == null || cargoGestion == null) {
            throw new RecursoNoDisponibleException(
                    "No se pudieron obtener los parámetros de costo (Combustible o Gestión).");
        }
        if (camionesDisponibles == null || camionesDisponibles.isEmpty()) {
            throw new RecursoNoDisponibleException(
                    "No hay camiones disponibles que cumplan con la capacidad del contenedor.");
        }

        // 4. CÁLCULO DE PROMEDIOS - ahora usando entidades Camion
        BigDecimal consumoPromedioCombustible = calculateAverage(camionesDisponibles,
                Camion::getConsumoCombustiblePromedio);
        BigDecimal costoBasePromedioPorKm = calculateAverage(camionesDisponibles, Camion::getCostoPorKm);
        
        log.info("Promedios calculados - Consumo: {} L/km, Costo base: $/km", 
                consumoPromedioCombustible, costoBasePromedioPorKm);

        // 5. PREPARACIÓN DE DATOS
        DepositoDto depositoOrigen = modelMapper.map(solicitud.getDepositoOrigen(), DepositoDto.class);
        DepositoDto depositoDestino = modelMapper.map(solicitud.getDepositoDestino(), DepositoDto.class);

        // Eliminar los depósitos de origen y destino de la lista de intermedios
        List<DepositoDto> depositosIntermedios = depositos.stream()
                .filter(dep -> !dep.getId().equals(depositoOrigen.getId())
                        && !dep.getId().equals(depositoDestino.getId()))
                .collect(Collectors.toList());

        log.info("Depósitos intermedios disponibles: {}", depositosIntermedios.size());

        // Generar las rutas tentativas (NO se persisten en BD)
        List<RutaTentativaDto> rutasTentativas = generarRutasTentativas(
                depositoOrigen,
                depositoDestino,
                depositosIntermedios,
                consumoPromedioCombustible,
                costoBasePromedioPorKm,
                costoCombustiblePorLitro,
                cargoGestion);

        log.info("Generadas {} rutas tentativas para solicitud ID: {}", rutasTentativas.size(), solicitudId);
        return rutasTentativas;
    }

    public List<RutaTentativaDto> generarRutasTentativas(
            DepositoDto depositoOrigen,
            DepositoDto depositoDestino,
            List<DepositoDto> depositosIntermedios,
            BigDecimal consumoPromedioCombustible,
            BigDecimal costoBasePromedioPorKm,
            BigDecimal costoCombustiblePorLitro,
            BigDecimal cargoGestion) {

        List<RutaTentativaDto> rutasTentativas = new ArrayList<>();

        // Generar diferentes combinaciones de rutas
        
        // Ruta 1: Directa (origen -> destino)
        RutaTentativaDto rutaDirecta = generarRutaDirecta(
            depositoOrigen, depositoDestino,
            consumoPromedioCombustible, costoBasePromedioPorKm,
            costoCombustiblePorLitro, cargoGestion
        );
        if (rutaDirecta != null) {
            rutasTentativas.add(rutaDirecta);
        }

        // Ruta 2: Con 1 depósito intermedio (si hay disponibles)
        if (!depositosIntermedios.isEmpty()) {
            RutaTentativaDto rutaConIntermedio = generarRutaConUnIntermedio(
                depositoOrigen, depositoDestino, depositosIntermedios.get(0),
                consumoPromedioCombustible, costoBasePromedioPorKm,
                costoCombustiblePorLitro, cargoGestion
            );
            if (rutaConIntermedio != null) {
                rutasTentativas.add(rutaConIntermedio);
            }
        }

        // Ruta 3: Con 2 depósitos intermedios (si hay disponibles)
        if (depositosIntermedios.size() >= 2) {
            RutaTentativaDto rutaConDosIntermedios = generarRutaConDosIntermedios(
                depositoOrigen, depositoDestino, 
                depositosIntermedios.get(0), depositosIntermedios.get(1),
                consumoPromedioCombustible, costoBasePromedioPorKm,
                costoCombustiblePorLitro, cargoGestion
            );
            if (rutaConDosIntermedios != null) {
                rutasTentativas.add(rutaConDosIntermedios);
            }
        }

        return rutasTentativas;
    }

    private RutaTentativaDto generarRutaDirecta(
            DepositoDto origen, DepositoDto destino,
            BigDecimal consumoPromedio, BigDecimal costoBasePromedio,
            BigDecimal costoCombustible, BigDecimal cargoGestion) {

        try {
            DistanciaDto distanciaInfo = googleMapsClient.calcularDistancia(
                origen.getLatitud(), origen.getLongitud(),
                destino.getLatitud(), destino.getLongitud()
            );

            Float distancia = (float) distanciaInfo.getKilometros();
            Long tiempoEstimado = convertirDuracionASegundos(distanciaInfo);
            
            BigDecimal costoTramo = calcularCostoTramo(distancia, consumoPromedio, costoBasePromedio, costoCombustible, cargoGestion);

            TramoTentativoDto tramo = TramoTentativoDto.builder()
                    .nroOrden(1)
                    .origen(origen)
                    .destino(destino)
                    .costoEstimado(costoTramo)
                    .tiempoEstimadoSegundos(tiempoEstimado)
                    .distanciaKilometros(distancia)
                    .build();

            return RutaTentativaDto.builder()
                    .cantidadTramos(1)
                    .cantidadDepositos(2)
                    .tramos(List.of(tramo))
                    .build();
        } catch (Exception e) {
            log.error("Error generando ruta directa: {}", e.getMessage());
            return null;
        }
    }

    private RutaTentativaDto generarRutaConUnIntermedio(
            DepositoDto origen, DepositoDto destino, DepositoDto intermedio,
            BigDecimal consumoPromedio, BigDecimal costoBasePromedio,
            BigDecimal costoCombustible, BigDecimal cargoGestion) {

        List<TramoTentativoDto> tramos = new ArrayList<>();

        // Tramo 1: Origen -> Intermedio
        DistanciaDto distancia1 = googleMapsClient.calcularDistancia(
            origen.getLatitud(), origen.getLongitud(),
            intermedio.getLatitud(), intermedio.getLongitud()
        );
        
        BigDecimal costo1 = calcularCostoTramo((float)distancia1.getKilometros(), consumoPromedio, 
                costoBasePromedio, costoCombustible, cargoGestion);

        tramos.add(TramoTentativoDto.builder()
                .nroOrden(1)
                .origen(origen)
                .destino(intermedio)
                .costoEstimado(costo1)
                .tiempoEstimadoSegundos(convertirDuracionASegundos(distancia1))
                .distanciaKilometros((float)distancia1.getKilometros())
                .build());

        // Tramo 2: Intermedio -> Destino
        DistanciaDto distancia2 = googleMapsClient.calcularDistancia(
            intermedio.getLatitud(), intermedio.getLongitud(),
            destino.getLatitud(), destino.getLongitud()
        );
        
        BigDecimal costo2 = calcularCostoTramo((float)distancia2.getKilometros(), consumoPromedio, 
                costoBasePromedio, costoCombustible, cargoGestion);

        tramos.add(TramoTentativoDto.builder()
                .nroOrden(2)
                .origen(intermedio)
                .destino(destino)
                .costoEstimado(costo2)
                .tiempoEstimadoSegundos(convertirDuracionASegundos(distancia2))
                .distanciaKilometros((float)distancia2.getKilometros())
                .build());

        return RutaTentativaDto.builder()
                .cantidadTramos(2)
                .cantidadDepositos(3)
                .tramos(tramos)
                .build();
    }

    private RutaTentativaDto generarRutaConDosIntermedios(
            DepositoDto origen, DepositoDto destino, 
            DepositoDto intermedio1, DepositoDto intermedio2,
            BigDecimal consumoPromedio, BigDecimal costoBasePromedio,
            BigDecimal costoCombustible, BigDecimal cargoGestion) {

        List<TramoTentativoDto> tramos = new ArrayList<>();

        // Tramo 1: Origen -> Intermedio1
        DistanciaDto distancia1 = googleMapsClient.calcularDistancia(
            origen.getLatitud(), origen.getLongitud(),
            intermedio1.getLatitud(), intermedio1.getLongitud()
        );
        
        tramos.add(crearTramoTentativo(1, origen, intermedio1, distancia1, 
                consumoPromedio, costoBasePromedio, costoCombustible, cargoGestion));

        // Tramo 2: Intermedio1 -> Intermedio2
        DistanciaDto distancia2 = googleMapsClient.calcularDistancia(
            intermedio1.getLatitud(), intermedio1.getLongitud(),
            intermedio2.getLatitud(), intermedio2.getLongitud()
        );
        
        tramos.add(crearTramoTentativo(2, intermedio1, intermedio2, distancia2, 
                consumoPromedio, costoBasePromedio, costoCombustible, cargoGestion));

        // Tramo 3: Intermedio2 -> Destino
        DistanciaDto distancia3 = googleMapsClient.calcularDistancia(
            intermedio2.getLatitud(), intermedio2.getLongitud(),
            destino.getLatitud(), destino.getLongitud()
        );
        
        tramos.add(crearTramoTentativo(3, intermedio2, destino, distancia3, 
                consumoPromedio, costoBasePromedio, costoCombustible, cargoGestion));

        return RutaTentativaDto.builder()
                .cantidadTramos(3)
                .cantidadDepositos(4)
                .tramos(tramos)
                .build();
    }

    private TramoTentativoDto crearTramoTentativo(int nroOrden, DepositoDto origen, DepositoDto destino,
            DistanciaDto distanciaInfo, BigDecimal consumoPromedio, BigDecimal costoBasePromedio,
            BigDecimal costoCombustible, BigDecimal cargoGestion) {
        
        BigDecimal costo = calcularCostoTramo((float)distanciaInfo.getKilometros(), consumoPromedio, 
                costoBasePromedio, costoCombustible, cargoGestion);

        return TramoTentativoDto.builder()
                .nroOrden(nroOrden)
                .origen(origen)
                .destino(destino)
                .costoEstimado(costo)
                .tiempoEstimadoSegundos(convertirDuracionASegundos(distanciaInfo))
                .distanciaKilometros((float)distanciaInfo.getKilometros())
                .build();
    }

    private BigDecimal calcularCostoTramo(Float distancia, BigDecimal consumoPromedio, 
            BigDecimal costoBasePromedio, BigDecimal costoCombustible, BigDecimal cargoGestion) {
        BigDecimal costoCombustibleTramo = BigDecimal.valueOf(distancia)
                .multiply(consumoPromedio)
                .multiply(costoCombustible);
        BigDecimal costoBaseTramo = BigDecimal.valueOf(distancia).multiply(costoBasePromedio);
        BigDecimal costoTotal = costoCombustibleTramo.add(costoBaseTramo).add(cargoGestion);
        
        // REDONDEAR a 2 decimales para cumplir con la validación @Digits(integer=8, fraction=2)
        return costoTotal.setScale(2, RoundingMode.HALF_UP);
    }

    // Método para asignar una ruta a una solicitud (aquí SÍ se persiste)
    public Optional<RutaDto> asignarRutaASolicitud(Integer solicitudId, RutaTentativaDto rutaTentativa) {
        log.info("Asignando ruta a solicitud ID: {}", solicitudId);
        
        try {
            // Verificar que la solicitud existe desde el repository local
            Optional<SolicitudTransporte> solicitudOpt = solicitudTransporteRepository.findById(solicitudId);
            if (solicitudOpt.isEmpty()) {
                log.error("Solicitud no encontrada para ID: {}", solicitudId);
                return Optional.empty();
            }

            SolicitudTransporte solicitud = solicitudOpt.get();

            // Calcular costo y tiempo total estimado
            BigDecimal costoTotalEstimado = rutaTentativa.getTramos().stream()
                    .map(TramoTentativoDto::getCostoEstimado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Long tiempoTotalEstimado = rutaTentativa.getTramos().stream()
                    .map(TramoTentativoDto::getTiempoEstimadoSegundos)
                    .reduce(0L, Long::sum);

            log.info("Costo total estimado: {}, Tiempo total estimado: {} segundos", 
                    costoTotalEstimado, tiempoTotalEstimado);

            // Crear entidad Ruta
            Ruta ruta = new Ruta();
            ruta.setCantidadTramos(rutaTentativa.getCantidadTramos());
            ruta.setCantidadDepositos(rutaTentativa.getCantidadDepositos());
            ruta.setSolicitud(solicitud);

            // Guardar ruta
            Ruta rutaGuardada = rutaRepository.save(ruta);
            log.info("Ruta guardada con ID: {}", rutaGuardada.getId());

            // Delegar la creación de tramos al TramoService
            tramoService.crearTramosDesdeRutaTentativa(rutaGuardada, rutaTentativa);
            log.info("Tramos creados exitosamente para ruta ID: {}", rutaGuardada.getId());

            // Actualizar solicitud a estado "Programada" con costo y tiempo estimado (usando RestClient)
            solicitudesClient.actualizarSolicitudAProgramada(solicitudId, costoTotalEstimado, tiempoTotalEstimado);
            log.info("Solicitud {} actualizada a estado 'Programada'", solicitudId);

            // Convertir a DTO y retornar
            RutaDto rutaDto = modelMapper.map(rutaGuardada, RutaDto.class);
            return Optional.of(rutaDto);

        } catch (Exception e) {
            log.error("Error asignando ruta a solicitud {}: {}", solicitudId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Optional<RutaDto> obtenerRutaAsignada(Integer solicitudId) {
        Optional<Ruta> rutaOp = rutaRepository.findBySolicitudId(solicitudId);
        return rutaOp.map(ruta -> modelMapper.map(ruta, RutaDto.class));
    }

    private Long convertirDuracionASegundos(DistanciaDto distanciaInfo) {
        // Siempre usar duracionSegundos si está disponible
        if (distanciaInfo.getDuracionSegundos() != null && distanciaInfo.getDuracionSegundos() > 0) {
            return distanciaInfo.getDuracionSegundos();
        }
        
        // Fallback: calcular basado en distancia (80 km/h)
        return (long) ((distanciaInfo.getKilometros() / 80.0) * 3600);
    }
}