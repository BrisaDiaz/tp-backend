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
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, longitud, id_ciudad) VALUES
('Depósito Central Cba.', 'Av. Circunvalación Oeste 5000', 1850.00, -31.4300, -64.2000, (SELECT id FROM ciudad WHERE nombre = 'Córdoba')),
('Terminal Sur Cba.', 'Calle 100 y Ruta 36', 1600.00, -31.5000, -64.1200, (SELECT id FROM ciudad WHERE nombre = 'Córdoba'));

-- RÍO CUARTO
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, longitud, id_ciudad) VALUES
('Logística Río Cuarto', 'Ruta Nacional 36, Km 600', 1200.00, -33.1500, -64.3800, (SELECT id FROM ciudad WHERE nombre = 'Río Cuarto'));

-- VILLA MARÍA
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, longitud, id_ciudad) VALUES
('Almacén VM', 'Av. Libertador 2500', 1150.00, -32.4000, -63.2500, (SELECT id FROM ciudad WHERE nombre = 'Villa María'));

-- SAN FRANCISCO
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, longitud, id_ciudad) VALUES
('Punto San Francisco', 'Ruta Nacional 158 y 19', 1050.00, -31.4100, -62.0500, (SELECT id FROM ciudad WHERE nombre = 'San Francisco'));

-- JESÚS MARÍA
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, longitud, id_ciudad) VALUES
('Depósito Regional JM', 'Av. 28 de Julio 1200', 950.00, -30.9850, -64.1000, (SELECT id FROM ciudad WHERE nombre = 'Jesús María'));

-- ROSARIO
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, longitud, id_ciudad) VALUES
('Puerto Rosario Logística', 'Acceso Sur y Av. San Martín', 2100.00, -33.0000, -60.7000, (SELECT id FROM ciudad WHERE nombre = 'Rosario')),
('Zárate Logística', 'Ruta Nacional A012, Km 15', 1950.00, -32.9500, -60.6500, (SELECT id FROM ciudad WHERE nombre = 'Rosario'));

-- SANTA FE
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, longitud, id_ciudad) VALUES
('Centro Distribución SF', 'Av. Alem 4500', 1700.00, -31.6500, -60.7100, (SELECT id FROM ciudad WHERE nombre = 'Santa Fe'));

-- RAFAELA
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, longitud, id_ciudad) VALUES
('Plataforma Rafaela', 'Bv. Roca 900', 1000.00, -30.8600, -61.5000, (SELECT id FROM ciudad WHERE nombre = 'Rafaela'));

-- SAN LUIS
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, longitud, id_ciudad) VALUES
('Zona Franca San Luis', 'Ruta Nacional 7, Km 800', 1100.00, -33.3500, -66.3000, (SELECT id FROM ciudad WHERE nombre = 'San Luis'));

-- VILLA MERCEDES
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, longitud, id_ciudad) VALUES
('Almacén VM San Luis', 'Av. Velez Sarsfield 3000', 980.00, -33.6800, -65.4500, (SELECT id FROM ciudad WHERE nombre = 'Villa Mercedes'));

-- SANTIAGO DEL ESTERO
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, longitud, id_ciudad) VALUES
('Norte Logística', 'Ruta Nacional 9 y 64', 1300.00, -27.7500, -64.2800, (SELECT id FROM ciudad WHERE nombre = 'Santiago del Estero'));

-- LA BANDA
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, longitud, id_ciudad) VALUES
('Depósito La Banda', 'Av. Belgrano 500', 1050.00, -27.7000, -64.2500, (SELECT id FROM ciudad WHERE nombre = 'La Banda'));

-- LA RIOJA
INSERT INTO deposito (nombre, direccion, precio_por_dia, latitud, longitud, id_ciudad) VALUES
('Plataforma La Rioja', 'Av. Ortiz de Ocampo 100', 1000.00, -29.4000, -66.8600, (SELECT id FROM ciudad WHERE nombre = 'La Rioja'));


-- ==========================================================================
-- 4. INSERCIÓN DE CAMIONES
-- ==========================================================================

INSERT INTO camion (
    dominio,
    capacidad_volumen, 
    capacidad_peso, 
    costo_por_km, 
    consumo_combustible_promedio, 
    nombre_transportista, 
    telefono_transportista, 
    disponibilidad
) VALUES 
-- 1. Semirremolque Pesado (Larga Distancia)
('AAT-123', 90.00, 45000.00, 15.50, 0.40, 'Transportes El Rápido S.A.', '+5493511112222', TRUE),

-- 2. Camión Rígido (Mediana Distancia)
('BBK-456', 40.00, 15000.00, 9.20, 0.25, 'Logística Central', '+5493513334444', TRUE),

-- 3. Furgón Liviano (Distribución Urbana)
('CCM-789', 15.00, 5000.00, 5.80, 0.15, 'Transportadora Express', '+5493515556666', TRUE),

-- 4. Camión con Acoplado (Gran Volumen)
('DDL-012', 110.00, 30000.00, 12.00, 0.35, 'Logística Central', '+5493513334444', TRUE),

-- 5. Semirremolque Frigorífico (Alta Capacidad/Costo)
('EEP-345', 75.00, 42000.00, 18.90, 0.45, 'FríoMax S.R.L.', '+5491177788899', TRUE),

-- 6. Camión Rígido con Grúa (Capacidad Media)
('FFQ-678', 35.00, 18000.00, 10.50, 0.28, 'Transportes El Rápido S.A.', '+5493511112222', TRUE),

-- 7. Furgón Mediano (Distribución Regional)
('GGR-901', 25.00, 8000.00, 7.00, 0.20, 'Logística Central', '+5493513334444', TRUE),

-- 8. Camión Cisterna (Especializado)
('HHU-234', 50.00, 20000.00, 11.50, 0.30, 'FríoMax S.R.L.', '+5491177788899', TRUE);


-- ==========================================================================
-- 1. INSERCIÓN DE CLIENTES
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
-- 2. FINALIZAR TRANSACCIÓN Y PERSISTIR DATOS
-- ==========================================================================
COMMIT;
