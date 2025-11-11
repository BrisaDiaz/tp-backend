# Módulo de Generación de Rutas Tentativas

Este documento describe la lógica implementada en el servicio `RutaService` para generar y seleccionar rutas de transporte preliminares (tentativas) para una solicitud dada.

## 1. Flujo General de Obtención de Rutas

El método principal (`obtenerTentativas`) sigue el siguiente proceso:

1.  **Validación de Solicitud:** Confirma la existencia de la **Solicitud de Transporte** y el **Contenedor** asociado.
2.  **Filtro de Camiones:** Busca camiones disponibles que cumplan con los requisitos de **volumen** y **peso** del contenedor. Si no hay camiones disponibles, se lanza una excepción.
3.  **Obtención de Tarifas:** Consulta el servicio `Recursos` para obtener el **Costo del Combustible por Litro** y el **Cargo por Gestión** por tramo.
4.  **Cálculo de Promedios:** Calcula el **Consumo Promedio de Combustible** y el **Costo Base Promedio por Km** a partir de los camiones filtrados.
5.  **Ordenamiento Heurístico de Depósitos:** Selecciona y ordena los depósitos intermedios.
6.  **Generación de Rutas:** Genera hasta tres rutas basadas en los depósitos ordenados.

***

## 2. Heurística de Selección de Depósitos Intermedios

Para clasificar los depósitos (distintos al origen y destino) que podrían ser usados como puntos intermedios, se utiliza una heurística de proximidad: la **Distancia Euclidiana al Cuadrado**.

Esta métrica (`calculateEuclideanDistanceSquared`) ordena los depósitos por el menor valor de:

$$\text{Score} = (\text{Distancia Euclídea}^2 (\text{Origen} \to \text{Intermedio})) + (\text{Distancia Euclídea}^2 (\text{Intermedio} \to \text{Destino}))$$

Los depósitos con el **Score más bajo** son considerados los "mejores rankeados" porque están geográficamente más cerca de la ruta directa.

***

## 3. Tipos de Rutas Generadas

Se generan un máximo de tres opciones de rutas tentativas, utilizando los depósitos intermedios mejor rankeados:

| Tipo de Ruta | Estructura | Condición | Depósitos Utilizados |
| :--- | :--- | :--- | :--- |
| **Ruta Directa** | Origen → Destino | Siempre generada. | 2 (Origen, Destino) |
| **Ruta 1 Intermedio** | Origen → **Int1** → Destino | Si hay al menos 1 depósito intermedio disponible. | 3 (Origen, **Mejor Rankeado**, Destino) |
| **Ruta 2 Intermedios** | Origen → **Int1** → **Int2** → Destino | Si hay al menos 2 depósitos intermedios disponibles. | 4 (Origen, **1er y 2do Mejor Rankeado**, Destino) |

***

## 4. Cálculo de Costo y Tiempo por Tramo

Para cada tramo de la ruta, se consulta la API de Google Maps (`GoogleMapsClient.calcularDistancia`) para obtener la **distancia real** en kilómetros y la **duración estimada** en segundos.

El costo total estimado para un tramo se calcula mediante la siguiente fórmula (`calcularCostoTramo`):

$$\text{Costo Tramo} = \text{Costo Combustible} + \text{Costo Base} + \text{Cargo Gestión}$$

Donde:

* **Costo Combustible:** $$\text{Distancia (Km)} \times \text{Consumo Promedio (L/Km)} \times \text{Costo Combustible/L}$$
* **Costo Base:** $$\text{Distancia (Km)} \times \text{Costo Base Promedio/Km}$$
* **Cargo Gestión:** Valor fijo por tramo consultado al servicio `Recursos`.

***

## 5. Asignación de Ruta (Persistencia)

El método `asignarRutaASolicitud` toma una de las rutas tentativas seleccionadas y realiza las siguientes acciones:

1.  Persiste la entidad **Ruta** en la base de datos local.
2.  Persiste los **Tramos** asociados a esa ruta.
3.  Actualiza el estado de la **Solicitud de Transporte** a **"Programada"** a través del servicio `SolicitudesClient`, enviando el costo total y el tiempo total estimados de la ruta.