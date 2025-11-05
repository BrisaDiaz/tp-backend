-- ==========================================================================
-- 1. INSERCIÓN DE CIUDADES
-- (Necesario para que la tabla 'deposito' pueda hacer referencia a ellas)
-- ==========================================================================

-- Ciudades de CÓRDOBA (X)
INSERT INTO ciudad (nombre, codigo_postal) VALUES ('Córdoba', 'X5000');
INSERT INTO ciudad (nombre, codigo_postal) VALUES ('Río Cuarto', 'X5800');
INSERT INTO ciudad (nombre, codigo_postal) VALUES ('Villa María', 'X5900');
INSERT INTO ciudad (nombre, codigo_postal) VALUES ('San Francisco', 'X2400');
INSERT INTO ciudad (nombre, codigo_postal) VALUES ('Jesús María', 'X5223');

-- Ciudades de SANTA FE (S)
INSERT INTO ciudad (nombre, codigo_postal) VALUES ('Rosario', 'S2000');
INSERT INTO ciudad (nombre, codigo_postal) VALUES ('Santa Fe', 'S3000');
INSERT INTO ciudad (nombre, codigo_postal) VALUES ('Rafaela', 'S2300');

-- Ciudades de SAN LUIS (D)
INSERT INTO ciudad (nombre, codigo_postal) VALUES ('San Luis', 'D5700');
INSERT INTO ciudad (nombre, codigo_postal) VALUES ('Villa Mercedes', 'D5730');

-- Ciudades de SANTIAGO DEL ESTERO (G)
INSERT INTO ciudad (nombre, codigo_postal) VALUES ('Santiago del Estero', 'G4000');
INSERT INTO ciudad (nombre, codigo_postal) VALUES ('La Banda', 'G4300');

-- Ciudades de LA RIOJA (F)
INSERT INTO ciudad (nombre, codigo_postal) VALUES ('La Rioja', 'F5300');


-- ==========================================================================
-- 2. INSERCIÓN DE HISTÓRICOS DE COSTOS
-- (Se insertan 2 históricos y 1 vigente por tabla)
-- ==========================================================================

-- PRECIO_COMBUSTIBLE
INSERT INTO precio_combustible (precio_por_litro, fecha_hora_desde, fecha_hora_hasta) VALUES
(750.50, '2024-12-01 00:00:00', '2024-12-31 23:59:59'), -- Histórico 1
(880.00, '2025-01-01 00:00:00', '2025-01-31 23:59:59'), -- Histórico 2
(955.75, '2025-02-01 00:00:00', NULL); -- Vigente (fecha_hora_hasta es NULL)

-- CARGO_GESTION
INSERT INTO cargo_gestion (costo_por_tramo, fecha_hora_desde, fecha_hora_hasta) VALUES
(2500.00, '2024-12-01 00:00:00', '2024-12-31 23:59:59'), -- Histórico 1
(3200.50, '2025-01-01 00:00:00', '2025-01-31 23:59:59'), -- Histórico 2
(4000.00, '2025-02-01 00:00:00', NULL); -- Vigente (fecha_hora_hasta es NULL)

-- ==========================================================================
-- 3. INSERCIÓN DE DEPÓSITOS
-- (Se utiliza SELECT ID FROM ciudad WHERE nombre = '...')
-- ==========================================================================

-- CÓRDOBA
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, Longitud, id_ciudad) VALUES
('Depósito Central Cba.', 'Av. Circunvalación Oeste 5000', 1850.00, -31.4300, -64.2000, (SELECT id FROM ciudad WHERE nombre = 'Córdoba')),
('Terminal Sur Cba.', 'Calle 100 y Ruta 36', 1600.00, -31.5000, -64.1200, (SELECT id FROM ciudad WHERE nombre = 'Córdoba'));

-- RÍO CUARTO
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, Longitud, id_ciudad) VALUES
('Logística Río Cuarto', 'Ruta Nacional 36, Km 600', 1200.00, -33.1500, -64.3800, (SELECT id FROM ciudad WHERE nombre = 'Río Cuarto'));

-- VILLA MARÍA
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, Longitud, id_ciudad) VALUES
('Almacén VM', 'Av. Libertador 2500', 1150.00, -32.4000, -63.2500, (SELECT id FROM ciudad WHERE nombre = 'Villa María'));

-- SAN FRANCISCO
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, Longitud, id_ciudad) VALUES
('Punto San Francisco', 'Ruta Nacional 158 y 19', 1050.00, -31.4100, -62.0500, (SELECT id FROM ciudad WHERE nombre = 'San Francisco'));

-- JESÚS MARÍA
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, Longitud, id_ciudad) VALUES
('Depósito Regional JM', 'Av. 28 de Julio 1200', 950.00, -30.9850, -64.1000, (SELECT id FROM ciudad WHERE nombre = 'Jesús María'));

-- ROSARIO
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, Longitud, id_ciudad) VALUES
('Puerto Rosario Logística', 'Acceso Sur y Av. San Martín', 2100.00, -33.0000, -60.7000, (SELECT id FROM ciudad WHERE nombre = 'Rosario')),
('Zárate Logística', 'Ruta Nacional A012, Km 15', 1950.00, -32.9500, -60.6500, (SELECT id FROM ciudad WHERE nombre = 'Rosario'));

-- SANTA FE
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, Longitud, id_ciudad) VALUES
('Centro Distribución SF', 'Av. Alem 4500', 1700.00, -31.6500, -60.7100, (SELECT id FROM ciudad WHERE nombre = 'Santa Fe'));

-- RAFAELA
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, Longitud, id_ciudad) VALUES
('Plataforma Rafaela', 'Bv. Roca 900', 1000.00, -30.8600, -61.5000, (SELECT id FROM ciudad WHERE nombre = 'Rafaela'));

-- SAN LUIS
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, Longitud, id_ciudad) VALUES
('Zona Franca San Luis', 'Ruta Nacional 7, Km 800', 1100.00, -33.3500, -66.3000, (SELECT id FROM ciudad WHERE nombre = 'San Luis'));

-- VILLA MERCEDES
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, Longitud, id_ciudad) VALUES
('Almacén VM San Luis', 'Av. Velez Sarsfield 3000', 980.00, -33.6800, -65.4500, (SELECT id FROM ciudad WHERE nombre = 'Villa Mercedes'));

-- SANTIAGO DEL ESTERO
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, Longitud, id_ciudad) VALUES
('Norte Logística', 'Ruta Nacional 9 y 64', 1300.00, -27.7500, -64.2800, (SELECT id FROM ciudad WHERE nombre = 'Santiago del Estero'));

-- LA BANDA
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, Longitud, id_ciudad) VALUES
('Depósito La Banda', 'Av. Belgrano 500', 1050.00, -27.7000, -64.2500, (SELECT id FROM ciudad WHERE nombre = 'La Banda'));

-- LA RIOJA
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, Longitud, id_ciudad) VALUES
('Plataforma La Rioja', 'Av. Ortiz de Ocampo 100', 1000.00, -29.4000, -66.8600, (SELECT id FROM ciudad WHERE nombre = 'La Rioja'));


-- ==========================================================================
-- 4. INSERCIÓN DE CAMIONES (CON ASIGNACIÓN DE AUTH_ID)
--
-- El nombre de la columna para el ID de Keycloak es 'auth_id'.
-- Se utilizan 8 IDs únicos para evitar la violación de la restricción UNIQUE.
--
-- IDs de transportista usados:
-- - T01 (e3f4a5b6-c7d8-40e9-f1g2-h3i4j5k6l7m8) -> Luis Rodríguez
-- - T02 (f9g0h1i2-j3k4-4l5m-n6o7-p8q9r0s1t2u3) -> Ana Martínez
-- - T03 (t3333333-2222-3333-4444-555555555553) -> Javier Gutiérrez
-- - T04 (t4444444-2222-3333-4444-555555555554) -> Silvia Díaz
-- - T05 (t5555555-2222-3333-4444-555555555555) -> Julieta Paz 
-- - T06 (t6666666-2222-3333-4444-555555555556) -> Carlos Luna 
-- - T07 (t7777777-2222-3333-4444-555555555557) -> Elena Ríos 
-- - T08 (t8888888-2222-3333-4444-555555555558) -> Martín Sosa 
-- ==========================================================================

INSERT INTO camion (
    dominio,
    capacidad_volumen, 
    capacidad_peso, 
    costo_por_km, 
    consumo_combustible_promedio, 
    auth_id, -- COLUMNA CORREGIDA
    nombre_transportista, 
    telefono_transportista, 
    disponibilidad
) VALUES 
-- 1. Semirremolque Pesado (Larga Distancia) -> T01 (Luis Rodríguez)
('AAT-123', 90.00, 45000.00, 15.50, 0.40, 'e3f4a5b6-c7d8-40e9-f1g2-h3i4j5k6l7m8', 'Luis Rodríguez', '+5493511112222', TRUE),

-- 2. Camión Rígido (Mediana Distancia) -> T02 (Ana Martínez)
('BBK-456', 40.00, 15000.00, 9.20, 0.25, 'f9g0h1i2-j3k4-4l5m-n6o7-p8q9r0s1t2u3', 'Ana Martínez', '+5493513334444', TRUE),

-- 3. Furgón Liviano (Distribución Urbana) -> T03 (Javier Gutiérrez)
('CCM-789', 15.00, 5000.00, 5.80, 0.15, 't3333333-2222-3333-4444-555555555553', 'Javier Gutiérrez', '+5493515556666', TRUE),

-- 4. Camión con Acoplado (Gran Volumen) -> T04 (Silvia Díaz)
('DDL-012', 110.00, 30000.00, 12.00, 0.35, 't4444444-2222-3333-4444-555555555554', 'Silvia Díaz', '+5493513334444', TRUE),

-- 5. Semirremolque Frigorífico (Alta Capacidad/Costo) -> T05 (Julieta Paz)
('EEP-345', 75.00, 42000.00, 18.90, 0.45, 't5555555-2222-3333-4444-555555555555', 'Julieta Paz', '+5491177788899', TRUE),

-- 6. Camión Rígido con Grúa (Capacidad Media) -> T06 (Carlos Luna)
('FFQ-678', 35.00, 18000.00, 10.50, 0.28, 't6666666-2222-3333-4444-555555555556', 'Carlos Luna', '+5493511112222', TRUE),

-- 7. Furgón Mediano (Distribución Regional) -> T07 (Elena Ríos)
('GGR-901', 25.00, 8000.00, 7.00, 0.20, 't7777777-2222-3333-4444-555555555557', 'Elena Ríos', '+5493513334444', TRUE),

-- 8. Camión Cisterna (Especializado) -> T08 (Martín Sosa)
('HHU-234', 50.00, 20000.00, 11.50, 0.30, 't8888888-2222-3333-4444-555555555558', 'Martín Sosa', '+5491177788899', TRUE);


-- ==========================================================================
-- 5. INSERCIÓN DE CLIENTES
-- ==========================================================================

INSERT INTO cliente (auth_id, nombre, apellido, email, telefono, dni) VALUES
(
    'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6', -- ID de Keycloak para 'cliente01'
    'Carla',
    'Gómez',
    'cliente01@example.com',
    '+5493512223333',
    '28123456'
),
(
    'c7d8e9f0-f1a2-48b9-c0d1-e2f3a4b5c6d7', -- ID de Keycloak para 'cliente02'
    'Juan',
    'Pérez',
    'cliente02@example.com',
    '+5493514445555',
    '30654321'
);

-- ==========================================================================
-- INSERCIÓN DE TODOS LOS ESTADOS REQUERIDOS POR EL BACKEND (Solicitud, Contenedor y Tramo)
-- ==========================================================================

INSERT INTO estado (nombre, descripcion) VALUES 
-- 1. ESTADOS DE SOLICITUD DE TRANSPORTE
('Borrador', 'Solicitud creada por el cliente, pendiente de cotización y programación por Logística.'),
('Programada', 'Solicitud aceptada y planificada, con costo y tiempo de entrega estimados.'),
('En Tránsito', 'La solicitud ha sido iniciada y el contenedor se encuentra viajando.'),
('Entregada', 'El servicio de transporte ha finalizado.'),
('Cancelada', 'Solicitud anulada por el cliente o rechazada por Logística.'),

-- 2. ESTADOS DE CONTENEDOR (Para el seguimiento a través del GET /seguimiento)
('Pendiente de Entrega', 'El contenedor está en el depósito de origen y aún no ha sido retirado por el transportista.'),
('En Depósito', 'El contenedor ha llegado y se encuentra almacenado en un depósito intermedio o el de destino.'),
('En Viaje', 'El contenedor salió de un depósito y está en ruta hacia el próximo punto.'),
('Entregado', 'El contenedor ha llegado al depósito final y la entrega está completa.'),

-- 3. ESTADOS DE TRAMO (Para la gestión interna de los segmentos de la ruta)
('Estimado', 'El tramo ha sido calculado y forma parte de la ruta planificada.'),
('Asignado', 'El tramo tiene un camión/transportista asignado y está listo para su inicio.'),
('Iniciado', 'El camión ha comenzado el tramo actual.'),
('Finalizado', 'El camión ha completado el tramo y llegó al destino (depósito o destino final).');

-- ==========================================================================
-- 7. INSERCIÓN DE SOLICITUDES DE TRANSPORTE PARA CLIENTES 1 Y 2
-- ==========================================================================

-- Primero insertamos los contenedores
INSERT INTO contenedor (volumen, peso, estado_actual_id) VALUES
-- Contenedor para Cliente 1 (Carla Gómez)
(
    25.50,  -- volumen en m³
    8500.00, -- peso en kg
    (SELECT id FROM estado WHERE nombre = 'Pendiente de Entrega')
),
-- Contenedor para Cliente 2 (Juan Pérez)
(
    18.75,  -- volumen en m³
    6200.00, -- peso en kg
    (SELECT id FROM estado WHERE nombre = 'Pendiente de Entrega')
);

-- Ahora insertamos las solicitudes de transporte
INSERT INTO solicitud_transporte (
    fecha_solicitud, 
    costo_estimado, 
    tiempo_estimado, 
    costo_real, 
    tiempo_real, 
    id_estado, 
    id_cliente, 
    id_contenedor, 
    id_deposito_origen, 
    id_deposito_destino
) VALUES
-- Solicitud 1: Cliente 1 (Carla Gómez) - Córdoba a Rosario
(
    '2025-02-15 10:30:00', -- fecha_solicitud
    NULL, -- costo_estimado (se calculará luego)
    NULL, -- tiempo_estimado (se calculará luego)
    NULL, -- costo_real (se completará al finalizar)
    NULL, -- tiempo_real (se completará al finalizar)
    (SELECT id FROM estado WHERE nombre = 'Borrador'), -- estado Borrador
    (SELECT id FROM cliente WHERE auth_id = 'a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6'), -- Cliente 1
    (SELECT id FROM contenedor WHERE volumen = 25.50 AND peso = 8500.00), -- Contenedor 1
    (SELECT id FROM deposito WHERE nombre = 'Depósito Central Cba.'), -- Origen: Córdoba
    (SELECT id FROM deposito WHERE nombre = 'Puerto Rosario Logística') -- Destino: Rosario
),
-- Solicitud 2: Cliente 2 (Juan Pérez) - Río Cuarto a La Rioja
(
    '2025-02-16 14:45:00', -- fecha_solicitud
    NULL, -- costo_estimado (se calculará luego)
    NULL, -- tiempo_estimado (se calculará luego)
    NULL, -- costo_real (se completará al finalizar)
    NULL, -- tiempo_real (se completará al finalizar)
    (SELECT id FROM estado WHERE nombre = 'Borrador'), -- estado Borrador
    (SELECT id FROM cliente WHERE auth_id = 'c7d8e9f0-f1a2-48b9-c0d1-e2f3a4b5c6d7'), -- Cliente 2
    (SELECT id FROM contenedor WHERE volumen = 18.75 AND peso = 6200.00), -- Contenedor 2
    (SELECT id FROM deposito WHERE nombre = 'Logística Río Cuarto'), -- Origen: Río Cuarto
    (SELECT id FROM deposito WHERE nombre = 'Plataforma La Rioja') -- Destino: La Rioja
);

-- ==========================================================================
-- 8. INSERCIÓN DE HISTÓRICOS DE ESTADO PARA LOS CONTENEDORES
-- ==========================================================================

-- Histórico para Contenedor 1 (Cliente 1)
INSERT INTO historico_estado_contenedor (
    fecha_hora_desde,
    fecha_hora_hasta,
    descripcion,
    estado_id,
    contenedor_id
) VALUES
(
    '2025-02-15 10:30:00', -- fecha_hora_desde (misma que la solicitud)
    NULL, -- fecha_hora_hasta (NULL porque es el estado actual)
    'Tu contenedor esta en espera para ser retirado.',
    (SELECT id FROM estado WHERE nombre = 'Pendiente de Entrega'),
    (SELECT id FROM contenedor WHERE volumen = 25.50 AND peso = 8500.00)
);

-- Histórico para Contenedor 2 (Cliente 2)
INSERT INTO historico_estado_contenedor (
    fecha_hora_desde,
    fecha_hora_hasta,
    descripcion,
    estado_id,
    contenedor_id
) VALUES
(
    '2025-02-16 14:45:00', -- fecha_hora_desde (misma que la solicitud)
    NULL, -- fecha_hora_hasta (NULL porque es el estado actual)
    'Tu contenedor esta en espera para ser retirado.',
    (SELECT id FROM estado WHERE nombre = 'Pendiente de Entrega'),
    (SELECT id FROM contenedor WHERE volumen = 18.75 AND peso = 6200.00)
);

-- ==========================================================================
-- 6. FINALIZAR TRANSACCIÓN Y PERSISTIR DATOS
-- ==========================================================================
COMMIT;