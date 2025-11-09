package ar.edu.utn.frc.backend.logistica.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import ar.edu.utn.frc.backend.logistica.controllers.RutaController;
import ar.edu.utn.frc.backend.logistica.dto.DepositoDto;
import ar.edu.utn.frc.backend.logistica.dto.RutaDto;
import ar.edu.utn.frc.backend.logistica.dto.RutaTentativaDto;
import ar.edu.utn.frc.backend.logistica.dto.TramoDto;
import ar.edu.utn.frc.backend.logistica.dto.TramoTentativoDto;
import ar.edu.utn.frc.backend.logistica.dto.helpers.DistanciaDto;
import ar.edu.utn.frc.backend.logistica.entities.Camion;
import ar.edu.utn.frc.backend.logistica.entities.Contenedor;
import ar.edu.utn.frc.backend.logistica.entities.Deposito;
import ar.edu.utn.frc.backend.logistica.entities.Ruta;
import ar.edu.utn.frc.backend.logistica.entities.SolicitudTransporte;
import ar.edu.utn.frc.backend.logistica.exceptions.DataConflictException; // Nuevo Importe
import ar.edu.utn.frc.backend.logistica.exceptions.RecursoNoDisponibleException;
import ar.edu.utn.frc.backend.logistica.exceptions.ResourceNotFoundException;
import ar.edu.utn.frc.backend.logistica.repositories.CamionRepository;
import ar.edu.utn.frc.backend.logistica.repositories.DepositoRepository;
import ar.edu.utn.frc.backend.logistica.repositories.RutaRepository;
import ar.edu.utn.frc.backend.logistica.repositories.SolicitudTransporteRepository;
import ar.edu.utn.frc.backend.logistica.restClient.GoogleMapsClient;
import ar.edu.utn.frc.backend.logistica.restClient.RecursosClient;
import ar.edu.utn.frc.backend.logistica.restClient.SolicitudesClient;

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

    private static final Logger logger = LoggerFactory.getLogger(RutaController.class);


    // Método helper para calcular promedios de BigDecimal - ahora con entidades Camion
    private BigDecimal calculateAverage(List<Camion> camiones, Function<Camion, BigDecimal> extractor) {
        if (camiones.isEmpty()) {
            logger.warn("calculateAverage: Lista de camiones vacía. Retornando cero.");
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
        logger.info("Iniciando obtención de rutas tentativas para solicitud ID: {}", solicitudId);
        
        // 1. OBTENER SOLICITUD desde el repository local
        Optional<SolicitudTransporte> solicitudOpt = solicitudTransporteRepository.findById(solicitudId);
        if (solicitudOpt.isEmpty()) {
            logger.error("obtenerTentativas: Solicitud de transporte ID {} no encontrada.", solicitudId);
            throw new ResourceNotFoundException("Solicitud de transporte no encontrada para el ID: " + solicitudId);
        }

        SolicitudTransporte solicitud = solicitudOpt.get();
        logger.debug("Solicitud obtenida - Origen: {} ({}) -> Destino: {} ({})", 
                solicitud.getDepositoOrigen().getNombre(), solicitud.getDepositoOrigen().getId(),
                solicitud.getDepositoDestino().getNombre(), solicitud.getDepositoDestino().getId());

        // Obtener contenedor desde la relación de la solicitud
        Contenedor contenedor = solicitud.getContenedor();
        if (contenedor == null) {
            logger.error("obtenerTentativas: Contenedor no asociado a la solicitud ID: {}", solicitudId);
            throw new ResourceNotFoundException("Contenedor no encontrado para la solicitud ID: " + solicitudId);
        }

        // Extracción de datos del contenedor
        BigDecimal volumen = contenedor.getVolumen();
        BigDecimal peso = contenedor.getPeso();
        logger.debug("Requisitos del contenedor - Volumen: {}, Peso: {}", volumen, peso);

        // Obtener depósitos desde el repository local
        List<Deposito> depositosEntities = depositoRepository.findAll();
        if (depositosEntities.isEmpty()) {
            logger.warn("obtenerTentativas: No se encontraron depósitos en el sistema.");
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
        
        logger.debug("Camiones disponibles que cumplen requisitos: {} encontrados.", camionesDisponibles.size());

        try {
            // Solo las tarifas se obtienen via RestClient
            costoCombustiblePorLitro = recursosClient.getCostoCombustiblePorLitro().getPrecioPorLitro();
            cargoGestion = recursosClient.getCargoPorGestion().getCostoPorTramo();

            logger.info("Recursos obtenidos - Costo Combustible: {}, Cargo Gestión: {}", 
                        costoCombustiblePorLitro, cargoGestion);
        } catch (RestClientException e) {
            logger.error("Error al comunicarse con el servicio 'Recursos': {}", e.getMessage(), e);
            throw new RecursoNoDisponibleException("Error de comunicación con el servicio 'Recursos'.", e);
        }

        // 3. VALIDACIÓN DE DATOS OBTENIDOS
        if (costoCombustiblePorLitro == null || cargoGestion == null) {
            logger.error("Faltan parámetros de costo (Combustible o Gestión).");
            throw new RecursoNoDisponibleException(
                        "No se pudieron obtener los parámetros de costo (Combustible o Gestión).");
        }
        if (camionesDisponibles == null || camionesDisponibles.isEmpty()) {
            logger.error("No hay camiones disponibles que cumplan con la capacidad requerida.");
            throw new RecursoNoDisponibleException(
                        "No hay camiones disponibles que cumplan con la capacidad del contenedor.");
        }

        // 4. CÁLCULO DE PROMEDIOS
        BigDecimal consumoPromedioCombustible = calculateAverage(camionesDisponibles,
                Camion::getConsumoCombustiblePromedio);
        BigDecimal costoBasePromedioPorKm = calculateAverage(camionesDisponibles, Camion::getCostoPorKm);
        
        logger.info("Promedios de camiones - Consumo promedio: {} L/km, Costo base promedio: {} $/km", 
                    consumoPromedioCombustible, costoBasePromedioPorKm);

        // 5. PREPARACIÓN DE DATOS
        DepositoDto depositoOrigen = modelMapper.map(solicitud.getDepositoOrigen(), DepositoDto.class);
        DepositoDto depositoDestino = modelMapper.map(solicitud.getDepositoDestino(), DepositoDto.class);

        // Eliminar los depósitos de origen y destino de la lista de intermedios
        List<DepositoDto> depositosIntermedios = depositos.stream()
                .filter(dep -> !dep.getId().equals(depositoOrigen.getId())
                        && !dep.getId().equals(depositoDestino.getId()))
                .collect(Collectors.toList());

        logger.debug("Depósitos intermedios disponibles para rutas: {}", depositosIntermedios.size());

        // Generar las rutas tentativas (NO se persisten en BD)
        List<RutaTentativaDto> rutasTentativas = generarRutasTentativas(
                    depositoOrigen,
                    depositoDestino,
                    depositosIntermedios,
                    consumoPromedioCombustible,
                    costoBasePromedioPorKm,
                    costoCombustiblePorLitro,
                    cargoGestion);

        logger.info("Finalizada generación. Generadas {} rutas tentativas para solicitud ID: {}", rutasTentativas.size(), solicitudId);
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
        
        logger.debug("Generando ruta Directa: {} -> {}", depositoOrigen.getId(), depositoDestino.getId());
        // Ruta 1: Directa (origen -> destino)
        RutaTentativaDto rutaDirecta = generarRutaDirecta(
            depositoOrigen, depositoDestino,
            consumoPromedioCombustible, costoBasePromedioPorKm,
            costoCombustiblePorLitro, cargoGestion
        );
        if (rutaDirecta != null) {
            rutasTentativas.add(rutaDirecta);
            logger.debug("Ruta Directa generada exitosamente.");
        }

        // Ruta 2: Con 1 depósito intermedio (si hay disponibles)
        if (!depositosIntermedios.isEmpty()) {
            DepositoDto intermedio = depositosIntermedios.get(0);
            logger.debug("Generando ruta con 1 intermedio: {} -> {} -> {}", depositoOrigen.getId(), intermedio.getId(), depositoDestino.getId());
            RutaTentativaDto rutaConUnIntermedio = generarRutaConUnIntermedio(
                depositoOrigen, depositoDestino, intermedio,
                consumoPromedioCombustible, costoBasePromedioPorKm,
                costoCombustiblePorLitro, cargoGestion
            );
            if (rutaConUnIntermedio != null) {
                rutasTentativas.add(rutaConUnIntermedio);
                logger.debug("Ruta con 1 intermedio generada exitosamente.");
            }
        }

        // Ruta 3: Con 2 depósitos intermedios (si hay disponibles)
        if (depositosIntermedios.size() >= 2) {
            DepositoDto intermedio1 = depositosIntermedios.get(0);
            DepositoDto intermedio2 = depositosIntermedios.get(1);
            logger.debug("Generando ruta con 2 intermedios: {} -> {} -> {} -> {}", depositoOrigen.getId(), intermedio1.getId(), intermedio2.getId(), depositoDestino.getId());
            RutaTentativaDto rutaConDosIntermedios = generarRutaConDosIntermedios(
                depositoOrigen, depositoDestino, 
                intermedio1, intermedio2,
                consumoPromedioCombustible, costoBasePromedioPorKm,
                costoCombustiblePorLitro, cargoGestion
            );
            if (rutaConDosIntermedios != null) {
                rutasTentativas.add(rutaConDosIntermedios);
                logger.debug("Ruta con 2 intermedios generada exitosamente.");
            }
        }

        return rutasTentativas;
    }

    private RutaTentativaDto generarRutaDirecta(
                DepositoDto origen, DepositoDto destino,
                BigDecimal consumoPromedio, BigDecimal costoBasePromedio,
                BigDecimal costoCombustible, BigDecimal cargoGestion) {

        try {
            logger.debug("Ruta Directa - Calculando distancia entre Origen ({}) y Destino ({})", origen.getId(), destino.getId());
            DistanciaDto distanciaInfo = googleMapsClient.calcularDistancia(
                origen.getLatitud(), origen.getLongitud(),
                destino.getLatitud(), destino.getLongitud()
            );

            Float distancia = (float) distanciaInfo.getKilometros();
            Long tiempoEstimado = convertirDuracionASegundos(distanciaInfo);
            
            BigDecimal costoTramo = calcularCostoTramo(distancia, consumoPromedio, costoBasePromedio, costoCombustible, cargoGestion);
            logger.debug("Tramo 1 (Directo) - Distancia: {} km, Tiempo: {}s, Costo: {}", distancia, tiempoEstimado, costoTramo);

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
            logger.error("Error generando ruta directa entre {} y {}: {}", origen.getId(), destino.getId(), e.getMessage());
            return null;
        }
    }

    private RutaTentativaDto generarRutaConUnIntermedio(
                DepositoDto origen, DepositoDto destino, DepositoDto intermedio,
                BigDecimal consumoPromedio, BigDecimal costoBasePromedio,
                BigDecimal costoCombustible, BigDecimal cargoGestion) {

        List<TramoTentativoDto> tramos = new ArrayList<>();

        try {
            // Tramo 1: Origen -> Intermedio
            logger.debug("Ruta 1 Intermedio - Calculando Tramo 1: {} -> {}", origen.getId(), intermedio.getId());
            DistanciaDto distancia1 = googleMapsClient.calcularDistancia(
                origen.getLatitud(), origen.getLongitud(),
                intermedio.getLatitud(), intermedio.getLongitud()
            );
            
            tramos.add(crearTramoTentativo(1, origen, intermedio, distancia1, 
                        consumoPromedio, costoBasePromedio, costoCombustible, cargoGestion));

            // Tramo 2: Intermedio -> Destino
            logger.debug("Ruta 1 Intermedio - Calculando Tramo 2: {} -> {}", intermedio.getId(), destino.getId());
            DistanciaDto distancia2 = googleMapsClient.calcularDistancia(
                intermedio.getLatitud(), intermedio.getLongitud(),
                destino.getLatitud(), destino.getLongitud()
            );
            
            tramos.add(crearTramoTentativo(2, intermedio, destino, distancia2, 
                        consumoPromedio, costoBasePromedio, costoCombustible, cargoGestion));

            logger.debug("Ruta con 1 intermedio completa. Costo Total: {}, Tramos: {}", 
                tramos.stream().map(TramoTentativoDto::getCostoEstimado).reduce(BigDecimal.ZERO, BigDecimal::add), 
                tramos.size());
            
            return RutaTentativaDto.builder()
                        .cantidadTramos(2)
                        .cantidadDepositos(3)
                        .tramos(tramos)
                        .build();
        } catch (Exception e) {
            logger.error("Error generando ruta con 1 intermedio ({}): {}", intermedio.getId(), e.getMessage());
            return null;
        }
    }

    private RutaTentativaDto generarRutaConDosIntermedios(
                DepositoDto origen, DepositoDto destino, 
                DepositoDto intermedio1, DepositoDto intermedio2,
                BigDecimal consumoPromedio, BigDecimal costoBasePromedio,
                BigDecimal costoCombustible, BigDecimal cargoGestion) {

        List<TramoTentativoDto> tramos = new ArrayList<>();

        try {
            // Tramo 1: Origen -> Intermedio1
            logger.debug("Ruta 2 Intermedios - Calculando Tramo 1: {} -> {}", origen.getId(), intermedio1.getId());
            DistanciaDto distancia1 = googleMapsClient.calcularDistancia(
                origen.getLatitud(), origen.getLongitud(),
                intermedio1.getLatitud(), intermedio1.getLongitud()
            );
            
            tramos.add(crearTramoTentativo(1, origen, intermedio1, distancia1, 
                        consumoPromedio, costoBasePromedio, costoCombustible, cargoGestion));

            // Tramo 2: Intermedio1 -> Intermedio2
            logger.debug("Ruta 2 Intermedios - Calculando Tramo 2: {} -> {}", intermedio1.getId(), intermedio2.getId());
            DistanciaDto distancia2 = googleMapsClient.calcularDistancia(
                intermedio1.getLatitud(), intermedio1.getLongitud(),
                intermedio2.getLatitud(), intermedio2.getLongitud()
            );
            
            tramos.add(crearTramoTentativo(2, intermedio1, intermedio2, distancia2, 
                        consumoPromedio, costoBasePromedio, costoCombustible, cargoGestion));

            // Tramo 3: Intermedio2 -> Destino
            logger.debug("Ruta 2 Intermedios - Calculando Tramo 3: {} -> {}", intermedio2.getId(), destino.getId());
            DistanciaDto distancia3 = googleMapsClient.calcularDistancia(
                intermedio2.getLatitud(), intermedio2.getLongitud(),
                destino.getLatitud(), destino.getLongitud()
            );
            
            tramos.add(crearTramoTentativo(3, intermedio2, destino, distancia3, 
                        consumoPromedio, costoBasePromedio, costoCombustible, cargoGestion));

            logger.debug("Ruta con 2 intermedios completa. Costo Total: {}, Tramos: {}", 
                tramos.stream().map(TramoTentativoDto::getCostoEstimado).reduce(BigDecimal.ZERO, BigDecimal::add), 
                tramos.size());

            return RutaTentativaDto.builder()
                        .cantidadTramos(3)
                        .cantidadDepositos(4)
                        .tramos(tramos)
                        .build();
        } catch (Exception e) {
            logger.error("Error generando ruta con 2 intermedios ({}, {}): {}", intermedio1.getId(), intermedio2.getId(), e.getMessage());
            return null;
        }
    }

    private TramoTentativoDto crearTramoTentativo(int nroOrden, DepositoDto origen, DepositoDto destino,
            DistanciaDto distanciaInfo, BigDecimal consumoPromedio, BigDecimal costoBasePromedio,
            BigDecimal costoCombustible, BigDecimal cargoGestion) {
        
        BigDecimal costo = calcularCostoTramo((float)distanciaInfo.getKilometros(), consumoPromedio, 
                                 costoBasePromedio, costoCombustible, cargoGestion);
        
        Long tiempo = convertirDuracionASegundos(distanciaInfo);

        logger.debug("Tramo Tentativo {} ({}->{}) - Distancia: {} km, Tiempo: {}s, Costo: {}", 
                      nroOrden, origen.getId(), destino.getId(), distanciaInfo.getKilometros(), tiempo, costo);

        return TramoTentativoDto.builder()
                        .nroOrden(nroOrden)
                        .origen(origen) 
                        .destino(destino)
                        .costoEstimado(costo)
                        .tiempoEstimadoSegundos(tiempo)
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
        BigDecimal costoFinal = costoTotal.setScale(SCALE, RoundingMode.HALF_UP);
        logger.debug("Cálculo Costo Tramo (Distancia: {} km) - Combustible: {}, Base: {}, Gestión: {}, Total: {}", 
                    distancia, costoCombustibleTramo.setScale(SCALE, RoundingMode.HALF_UP), 
                    costoBaseTramo.setScale(SCALE, RoundingMode.HALF_UP), cargoGestion, costoFinal);

        return costoFinal;
    }

    // Método para asignar una ruta a una solicitud (aquí SÍ se persiste)
    public Optional<RutaDto> asignarRutaASolicitud(Integer solicitudId, RutaTentativaDto rutaTentativa) {
        logger.info("Iniciando asignación de ruta a solicitud ID: {}", solicitudId);

        // ✅ VALIDACIÓN: Verificar que la solicitud no tenga ya una ruta asignada
        Optional<Ruta> rutaExistente = rutaRepository.findBySolicitudId(solicitudId);
        if (rutaExistente.isPresent()) {
            logger.error("asignarRutaASolicitud: La solicitud ID {} ya tiene una ruta asignada (ID {})", solicitudId,
                    rutaExistente.get().getId());
            throw new DataConflictException("La solicitud ID " + solicitudId + " ya tiene una ruta asignada");
        }

        try {
            // Verificar que la solicitud existe desde el repository local
            Optional<SolicitudTransporte> solicitudOpt = solicitudTransporteRepository.findById(solicitudId);
            if (solicitudOpt.isEmpty()) {
                logger.error("asignarRutaASolicitud: Solicitud ID {} no encontrada.", solicitudId);
                return Optional.empty();
            }

            SolicitudTransporte solicitud = solicitudOpt.get();
            logger.debug("Solicitud ID {} lista para asignación de ruta.", solicitudId);

            // Calcular costo y tiempo total estimado
            BigDecimal costoTotalEstimado = rutaTentativa.getTramos().stream()
                    .map(TramoTentativoDto::getCostoEstimado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(SCALE, RoundingMode.HALF_UP);

            Long tiempoTotalEstimado = rutaTentativa.getTramos().stream()
                    .map(TramoTentativoDto::getTiempoEstimadoSegundos)
                    .reduce(0L, Long::sum);

            logger.info(
                    "Ruta seleccionada - Tramos: {}, Depósitos: {}, Costo Total Estimado: {}, Tiempo Total Estimado: {} segundos",
                    rutaTentativa.getCantidadTramos(), rutaTentativa.getCantidadDepositos(), costoTotalEstimado,
                    tiempoTotalEstimado);

            // Crear entidad Ruta
            Ruta ruta = new Ruta();
            ruta.setCantidadTramos(rutaTentativa.getCantidadTramos());
            ruta.setCantidadDepositos(rutaTentativa.getCantidadDepositos());
            ruta.setSolicitud(solicitud);

            // Guardar ruta
            Ruta rutaGuardada = rutaRepository.save(ruta);
            logger.info("Ruta persistida con ID: {}", rutaGuardada.getId());

            // Delegar la creación de tramos al TramoService
            tramoService.crearTramosDesdeRutaTentativa(rutaGuardada, rutaTentativa);
            logger.debug("Tramos creados y asociados a la ruta ID: {}", rutaGuardada.getId());

            // 1. Obtener los Tramos como DTOs directamente del TramoService
            List<TramoDto> tramosDto = tramoService.buscarPorRuta(rutaGuardada.getId());

            // 2. Mapear la entidad Ruta a DTO (se mapean los campos básicos)
            RutaDto rutaDto = modelMapper.map(rutaGuardada, RutaDto.class);

            // 3. Asignar la lista de Tramos DTO al Ruta DTO
            rutaDto.setTramos(tramosDto);
            // ==============================================================================

            // Actualizar solicitud a estado "Programada" con costo y tiempo estimado (usando RestClient)
            logger.info("Llamando a SolicitudesClient para actualizar solicitud {} a 'Programada'", solicitudId);
            solicitudesClient.actualizarSolicitudAProgramada(solicitudId, costoTotalEstimado, tiempoTotalEstimado);
            logger.info("Solicitud {} actualizada a estado 'Programada' con éxito.", solicitudId);

            // Convertir a DTO y retornar
            return Optional.of(rutaDto);

        } catch (DataConflictException dce) {
            throw dce; // Re-lanzar la excepción de conflicto para el manejo adecuado
        } catch (RestClientException rce) {
            logger.error("Error de comunicación al actualizar la solicitud a 'Programada' (ID {}): {}", solicitudId,
                    rce.getMessage());
            // Considerar si revertir la persistencia de Ruta y Tramos si es necesario
            throw new RuntimeException("Error al actualizar la solicitud después de asignar la ruta.", rce);
        } catch (Exception e) {
            logger.error("Error inesperado asignando ruta a solicitud {}: {}", solicitudId, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    public Optional<RutaDto> obtenerRutaAsignada(Integer solicitudId) {
        logger.info("Buscando ruta asignada para solicitud ID: {}", solicitudId);
        Optional<Ruta> rutaOp = rutaRepository.findBySolicitudId(solicitudId);
        
        if (rutaOp.isPresent()) {
            Ruta ruta = rutaOp.get();
            logger.debug("Ruta (ID: {}) encontrada para solicitud ID: {}", ruta.getId(), solicitudId);
            
            // --- Cargar DTOs de Tramos desde TramoService ---
            
            // 1. Mapear la entidad Ruta principal a DTO.
            RutaDto rutaDto = modelMapper.map(ruta, RutaDto.class);
            
            // 2. Obtener la lista de Tramos como DTOs directamente del TramoService.
            List<TramoDto> tramosDto = tramoService.buscarPorRuta(ruta.getId()); 
            
            // 3. Asignar la lista de DTOs
            rutaDto.setTramos(tramosDto);
            // ------------------------------------------------

            return Optional.of(rutaDto);
        } else {
            logger.debug("No se encontró ruta asignada para solicitud ID: {}", solicitudId);
        }
        return Optional.empty();
    }

    private Long convertirDuracionASegundos(DistanciaDto distanciaInfo) {
        // Siempre usar duracionSegundos si está disponible
        if (distanciaInfo.getDuracionSegundos() != null && distanciaInfo.getDuracionSegundos() > 0) {
            logger.debug("Usando duración de Google Maps: {} segundos", distanciaInfo.getDuracionSegundos());
            return distanciaInfo.getDuracionSegundos();
        }
        
        // Fallback: calcular basado en distancia (80 km/h)
        long tiempoCalculado = (long) ((distanciaInfo.getKilometros() / 80.0) * 3600);
        logger.warn("Usando fallback de tiempo (80 km/h) para distancia {} km: {} segundos", distanciaInfo.getKilometros(), tiempoCalculado);
        return tiempoCalculado;
    }
}