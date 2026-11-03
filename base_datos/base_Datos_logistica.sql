-- =====================================================
-- BASE DE DATOS: LOGISTICA
-- MOTOR: MYSQL 8.0+
-- PROYECTO: AppLogistica (molde: AppEmpresa)
-- Plataforma de logística y seguimiento de envíos - LogiRuta Perú S.A.C.
-- Versión: Avance 2 (Capítulos I al VI)
--   + estado_laboral e historial_laboral (State/Observer del empleado, Sesión 12)
--   + columnas intentos / paso_flujo (patrón State)
--   + tabla alertas (patrón Observer)
--   + tabla comandos_log (patrón Command / Memento)
-- Ejecutar completo en MySQL Workbench (Ctrl+Shift+Enter) o con:
--   mysql -u root -p < base_Datos_logistica.sql
-- =====================================================

SET NAMES utf8mb4;

-- Se recrea la base completa (tablas, procedimientos, triggers y vistas)
-- para que el script pueda ejecutarse cuantas veces sea necesario.
DROP DATABASE IF EXISTS logistica;

CREATE DATABASE IF NOT EXISTS logistica
CHARACTER SET utf8mb4
COLLATE utf8mb4_spanish_ci;

USE logistica;

-- Usuario de conexión de la aplicación (uni.database.AccesoDB)
CREATE USER IF NOT EXISTS 'logistica'@'localhost' IDENTIFIED BY 'logistica';
GRANT ALL PRIVILEGES ON logistica.* TO 'logistica'@'localhost';
FLUSH PRIVILEGES;

-- =====================================================
-- 1. ELIMINAR TABLAS PARA RECREAR LA ESTRUCTURA
-- =====================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS historial_laboral;
DROP TABLE IF EXISTS comandos_log;
DROP TABLE IF EXISTS alertas;
DROP TABLE IF EXISTS auditoria;
DROP TABLE IF EXISTS notificaciones;
DROP TABLE IF EXISTS reclamos;
DROP TABLE IF EXISTS incidencias;
DROP TABLE IF EXISTS seguimientos;
DROP TABLE IF EXISTS asignaciones;
DROP TABLE IF EXISTS unidad_detalle;
DROP TABLE IF EXISTS unidades_carga;
DROP TABLE IF EXISTS envio_adicionales;
DROP TABLE IF EXISTS envios;
DROP TABLE IF EXISTS direcciones;
DROP TABLE IF EXISTS servicios_adicionales;
DROP TABLE IF EXISTS tipos_servicio;
DROP TABLE IF EXISTS rutas;
DROP TABLE IF EXISTS vehiculos;
DROP TABLE IF EXISTS clientes;
DROP TABLE IF EXISTS empleados;
DROP TABLE IF EXISTS zonas;
DROP TABLE IF EXISTS sucursales;
DROP TABLE IF EXISTS proveedores_integracion;
DROP TABLE IF EXISTS control;

DROP VIEW IF EXISTS vw_incidencias_zona;
DROP VIEW IF EXISTS vw_cumplimiento_tiempos;
DROP VIEW IF EXISTS vw_productividad_reparto;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 2. TABLA CONTROL DE CORRELATIVOS (igual que AppEmpresa)
-- =====================================================

CREATE TABLE control (
    parametro VARCHAR(30) PRIMARY KEY,
    valor INT NOT NULL
) ENGINE=InnoDB;

-- =====================================================
-- 3. SUCURSALES (agencias, almacenes y tiendas de recojo)
-- =====================================================

CREATE TABLE sucursales (
    idsucursal CHAR(4) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo ENUM('AGENCIA','ALMACEN','HUB','TIENDA_RECOJO') NOT NULL,
    direccion VARCHAR(180) NOT NULL,
    departamento VARCHAR(60) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- =====================================================
-- 4. ZONAS DE REPARTO (para reportes de incidencias)
-- =====================================================

CREATE TABLE zonas (
    idzona INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(80) NOT NULL,
    idsucursal CHAR(4) NOT NULL,
    CONSTRAINT fk_zonas_sucursal FOREIGN KEY (idsucursal) REFERENCES sucursales(idsucursal)
) ENGINE=InnoDB;

-- =====================================================
-- 5. EMPLEADOS / USUARIOS (con rol)
-- La clave se guarda como hash SHA-256 (nunca texto plano).
-- =====================================================

CREATE TABLE empleados (
    idempleado CHAR(5) PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(120),
    usuario VARCHAR(50) NOT NULL UNIQUE,
    clave CHAR(64) NOT NULL,
    rol ENUM('AGENTE_VENTANILLA','OPERADOR_ALMACEN','SUPERVISOR_RUTA',
             'REPARTIDOR','ADMINISTRADOR') NOT NULL,
    idsucursal CHAR(4) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    estado_laboral ENUM('ACTIVO','VACACIONES','INACTIVO') NOT NULL DEFAULT 'ACTIVO',  -- patrón State
    CONSTRAINT fk_empleados_sucursal FOREIGN KEY (idsucursal) REFERENCES sucursales(idsucursal)
) ENGINE=InnoDB;

-- =====================================================
-- 6. CLIENTES (naturales y corporativos)
-- =====================================================

CREATE TABLE clientes (
    idcliente CHAR(5) PRIMARY KEY,
    razonsocial VARCHAR(150) NOT NULL,
    tipocliente ENUM('NATURAL','CORPORATIVO') NOT NULL,
    nrodocumento VARCHAR(11) NOT NULL UNIQUE,
    email VARCHAR(120),
    telefono VARCHAR(20),
    usuario_portal VARCHAR(50) UNIQUE,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =====================================================
-- 7. VEHICULOS Y RUTAS
-- =====================================================

CREATE TABLE vehiculos (
    placa CHAR(7) PRIMARY KEY,
    tipo ENUM('MOTO','FURGONETA','CAMION') NOT NULL,
    capacidad_kg DECIMAL(10,2) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE rutas (
    idruta INT PRIMARY KEY AUTO_INCREMENT,
    fecha DATE NOT NULL,
    idzona INT NOT NULL,
    placa CHAR(7) NOT NULL,
    idsupervisor CHAR(5) NOT NULL,
    CONSTRAINT fk_rutas_zona FOREIGN KEY (idzona) REFERENCES zonas(idzona),
    CONSTRAINT fk_rutas_vehiculo FOREIGN KEY (placa) REFERENCES vehiculos(placa),
    CONSTRAINT fk_rutas_supervisor FOREIGN KEY (idsupervisor) REFERENCES empleados(idempleado)
) ENGINE=InnoDB;

-- =====================================================
-- 8. CATALOGO DE SERVICIOS (Factory) Y ADICIONALES (Decorator)
-- =====================================================

CREATE TABLE tipos_servicio (
    idservicio VARCHAR(20) PRIMARY KEY,
    descripcion VARCHAR(100) NOT NULL,
    cargo_fijo DECIMAL(10,2) NOT NULL,
    costo_kg DECIMAL(10,2) NOT NULL,
    dias_entrega INT NOT NULL,
    prioridad INT NOT NULL,
    peso_maximo DECIMAL(10,2)
) ENGINE=InnoDB;

CREATE TABLE servicios_adicionales (
    idadicional VARCHAR(20) PRIMARY KEY,
    descripcion VARCHAR(100) NOT NULL,
    tipo_calculo ENUM('FIJO','PORCENTAJE_VALOR','PORCENTAJE_TARIFA') NOT NULL,
    valor DECIMAL(10,4) NOT NULL
) ENGINE=InnoDB;

-- =====================================================
-- 9. DIRECCIONES (geolocalizadas por el Adapter)
-- =====================================================

CREATE TABLE direcciones (
    iddireccion INT PRIMARY KEY AUTO_INCREMENT,
    calle VARCHAR(180) NOT NULL,
    distrito VARCHAR(80) NOT NULL,
    provincia VARCHAR(80) NOT NULL,
    departamento VARCHAR(80) NOT NULL,
    latitud DECIMAL(9,6),
    longitud DECIMAL(9,6)
) ENGINE=InnoDB;

-- =====================================================
-- 10. ENVIOS
-- intentos y paso_flujo son la parte persistente del patrón State.
-- =====================================================

CREATE TABLE envios (
    codigo CHAR(10) PRIMARY KEY,
    idcliente CHAR(5) NOT NULL,
    idservicio VARCHAR(20) NOT NULL,
    iddir_origen INT,
    iddir_destino INT,
    idzona_destino INT,
    nombre_dest VARCHAR(120) NOT NULL,
    telefono_dest VARCHAR(20),
    email_dest VARCHAR(120),
    peso_kg DECIMAL(10,2) NOT NULL,
    valor_declarado DECIMAL(12,2) NOT NULL DEFAULT 0,
    fragil TINYINT(1) NOT NULL DEFAULT 0,
    con_seguro TINYINT(1) NOT NULL DEFAULT 0,
    requiere_firma TINYINT(1) NOT NULL DEFAULT 0,
    fecha_programada DATE,
    idsucursal_recojo CHAR(4),
    estado ENUM('REGISTRADO','RECOGIDO','EN_TRANSITO','CLASIFICADO','EN_RUTA',
                'ENTREGADO','OBSERVADO','REPROGRAMADO','DEVUELTO')
           NOT NULL DEFAULT 'REGISTRADO',
    paso_flujo TINYINT NOT NULL DEFAULT 0,
    intentos TINYINT NOT NULL DEFAULT 0,
    costo DECIMAL(12,2) NOT NULL DEFAULT 0,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_estimada DATE,
    fecha_entrega DATETIME,
    codigo_firma VARCHAR(40),

    CONSTRAINT fk_envios_cliente FOREIGN KEY (idcliente) REFERENCES clientes(idcliente),
    CONSTRAINT fk_envios_servicio FOREIGN KEY (idservicio) REFERENCES tipos_servicio(idservicio),
    CONSTRAINT fk_envios_origen FOREIGN KEY (iddir_origen) REFERENCES direcciones(iddireccion),
    CONSTRAINT fk_envios_destino FOREIGN KEY (iddir_destino) REFERENCES direcciones(iddireccion),
    CONSTRAINT fk_envios_zona FOREIGN KEY (idzona_destino) REFERENCES zonas(idzona),
    CONSTRAINT fk_envios_tienda FOREIGN KEY (idsucursal_recojo) REFERENCES sucursales(idsucursal),
    CONSTRAINT ck_envios_peso CHECK (peso_kg > 0),
    CONSTRAINT ck_envios_intentos CHECK (intentos BETWEEN 0 AND 3)
) ENGINE=InnoDB;

CREATE INDEX ix_envios_estado ON envios(estado);
CREATE INDEX ix_envios_cliente_fecha ON envios(idcliente, fecha_registro);

CREATE TABLE envio_adicionales (
    codigo CHAR(10) NOT NULL,
    idadicional VARCHAR(20) NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (codigo, idadicional),
    CONSTRAINT fk_ea_envio FOREIGN KEY (codigo) REFERENCES envios(codigo),
    CONSTRAINT fk_ea_adicional FOREIGN KEY (idadicional) REFERENCES servicios_adicionales(idadicional)
) ENGINE=InnoDB;

-- =====================================================
-- 11. UNIDADES DE CARGA (Composite: saco/paleta/manifiesto)
-- =====================================================

CREATE TABLE unidades_carga (
    idunidad VARCHAR(15) PRIMARY KEY,
    tipo ENUM('SACO','PALETA','MANIFIESTO') NOT NULL,
    tara_kg DECIMAL(10,2) NOT NULL DEFAULT 0,
    idpadre VARCHAR(15),
    idruta INT,
    CONSTRAINT fk_uc_padre FOREIGN KEY (idpadre) REFERENCES unidades_carga(idunidad),
    CONSTRAINT fk_uc_ruta FOREIGN KEY (idruta) REFERENCES rutas(idruta)
) ENGINE=InnoDB;

CREATE TABLE unidad_detalle (
    idunidad VARCHAR(15) NOT NULL,
    codigo CHAR(10) NOT NULL,
    PRIMARY KEY (idunidad, codigo),
    CONSTRAINT fk_ud_unidad FOREIGN KEY (idunidad) REFERENCES unidades_carga(idunidad),
    CONSTRAINT fk_ud_envio FOREIGN KEY (codigo) REFERENCES envios(codigo)
) ENGINE=InnoDB;

-- =====================================================
-- 12. ASIGNACIONES ENVIO-REPARTIDOR (usadas por el Proxy)
-- =====================================================

CREATE TABLE asignaciones (
    codigo CHAR(10) NOT NULL,
    idrepartidor CHAR(5) NOT NULL,
    idruta INT NOT NULL,
    fecha_asignacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (codigo, idruta),
    CONSTRAINT fk_as_envio FOREIGN KEY (codigo) REFERENCES envios(codigo),
    CONSTRAINT fk_as_repartidor FOREIGN KEY (idrepartidor) REFERENCES empleados(idempleado),
    CONSTRAINT fk_as_ruta FOREIGN KEY (idruta) REFERENCES rutas(idruta)
) ENGINE=InnoDB;

-- =====================================================
-- 13. SEGUIMIENTOS (historial inmutable de estados)
-- Lo escribe el observador HistorialSeguimientoObserver.
-- =====================================================

CREATE TABLE seguimientos (
    idseguimiento BIGINT PRIMARY KEY AUTO_INCREMENT,
    codigo CHAR(10) NOT NULL,
    estado VARCHAR(15) NOT NULL,
    ubicacion VARCHAR(120),
    observacion VARCHAR(250),
    idempleado CHAR(5),
    fechahora DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_seg_envio FOREIGN KEY (codigo) REFERENCES envios(codigo),
    CONSTRAINT fk_seg_empleado FOREIGN KEY (idempleado) REFERENCES empleados(idempleado)
) ENGINE=InnoDB;

CREATE INDEX ix_seg_codigo ON seguimientos(codigo, fechahora);

-- =====================================================
-- 14. INCIDENCIAS Y RECLAMOS
-- =====================================================

CREATE TABLE incidencias (
    idincidencia INT PRIMARY KEY AUTO_INCREMENT,
    codigo CHAR(10) NOT NULL,
    motivo ENUM('DIRECCION_INCORRECTA','AUSENCIA_DESTINATARIO','CLIMA',
                'INCIDENTE_OPERATIVO','OTRO') NOT NULL,
    descripcion VARCHAR(250),
    idzona INT,
    idempleado CHAR(5) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inc_envio FOREIGN KEY (codigo) REFERENCES envios(codigo),
    CONSTRAINT fk_inc_zona FOREIGN KEY (idzona) REFERENCES zonas(idzona),
    CONSTRAINT fk_inc_empleado FOREIGN KEY (idempleado) REFERENCES empleados(idempleado)
) ENGINE=InnoDB;

CREATE TABLE reclamos (
    idreclamo INT PRIMARY KEY,
    codigo CHAR(10) NOT NULL,
    idcliente CHAR(5) NOT NULL,
    detalle VARCHAR(500) NOT NULL,
    estado ENUM('ABIERTO','EN_ATENCION','RESUELTO') NOT NULL DEFAULT 'ABIERTO',
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre DATETIME,
    CONSTRAINT fk_rec_envio FOREIGN KEY (codigo) REFERENCES envios(codigo),
    CONSTRAINT fk_rec_cliente FOREIGN KEY (idcliente) REFERENCES clientes(idcliente)
) ENGINE=InnoDB;

-- =====================================================
-- 15. INTEGRACIONES, NOTIFICACIONES, AUDITORIA
-- =====================================================

CREATE TABLE proveedores_integracion (
    idproveedor VARCHAR(20) PRIMARY KEY,
    tipo ENUM('GEOLOCALIZACION','NOTIFICACION','FIRMA_DIGITAL','ECOMMERCE') NOT NULL,
    familia ENUM('PRODUCCION','CONTINGENCIA') NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- Lo escribe el observador NotificacionDestinatarioObserver
CREATE TABLE notificaciones (
    idnotificacion BIGINT PRIMARY KEY AUTO_INCREMENT,
    codigo CHAR(10) NOT NULL,
    canal VARCHAR(15) NOT NULL,
    destinatario VARCHAR(120) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    enviado TINYINT(1) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_not_envio FOREIGN KEY (codigo) REFERENCES envios(codigo)
) ENGINE=InnoDB;

-- Lo escribe el Proxy de seguimiento
CREATE TABLE auditoria (
    idauditoria BIGINT PRIMARY KEY AUTO_INCREMENT,
    idusuario CHAR(5) NOT NULL,
    rol VARCHAR(25) NOT NULL,
    accion VARCHAR(40) NOT NULL,
    codigo CHAR(10),
    permitido TINYINT(1) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =====================================================
-- 16. ALERTAS A SUPERVISORES (patrón Observer)
-- Lo escribe AlertaSupervisorObserver: envíos express observados,
-- tercer intento fallido, devoluciones.
-- =====================================================

CREATE TABLE alertas (
    idalerta BIGINT PRIMARY KEY AUTO_INCREMENT,
    codigo CHAR(10) NOT NULL,
    nivel ENUM('INFO','ALTA','CRITICA') NOT NULL,
    mensaje VARCHAR(250) NOT NULL,
    atendida TINYINT(1) NOT NULL DEFAULT 0,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alerta_envio FOREIGN KEY (codigo) REFERENCES envios(codigo)
) ENGINE=InnoDB;

-- =====================================================
-- 17. BITACORA DE COMANDOS (patrones Command y Memento)
-- Cada acción del personal operativo es un comando que puede
-- deshacerse; el campo estado_previo guarda lo que el Memento restauró.
-- =====================================================

CREATE TABLE comandos_log (
    idcomando BIGINT PRIMARY KEY AUTO_INCREMENT,
    codigo CHAR(10) NOT NULL,
    comando VARCHAR(40) NOT NULL,
    descripcion VARCHAR(250) NOT NULL,
    estado_previo VARCHAR(15),
    estado_nuevo VARCHAR(15),
    idempleado CHAR(5) NOT NULL,
    deshecho TINYINT(1) NOT NULL DEFAULT 0,
    fecha DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    fecha_deshecho DATETIME,
    CONSTRAINT fk_cmd_envio FOREIGN KEY (codigo) REFERENCES envios(codigo),
    CONSTRAINT fk_cmd_empleado FOREIGN KEY (idempleado) REFERENCES empleados(idempleado)
) ENGINE=InnoDB;

-- =====================================================
-- 17b. HISTORIAL LABORAL DEL EMPLEADO (State + Observer, Sesión 12)
-- Lo escribe RecursosHumanosObserver cada vez que cambia el estado laboral.
-- =====================================================

CREATE TABLE historial_laboral (
    idhistorial BIGINT PRIMARY KEY AUTO_INCREMENT,
    idempleado CHAR(5) NOT NULL,
    estado_anterior VARCHAR(12) NOT NULL,
    estado_nuevo VARCHAR(12) NOT NULL,
    motivo VARCHAR(200),
    usuario CHAR(5),
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hl_empleado FOREIGN KEY (idempleado) REFERENCES empleados(idempleado)
) ENGINE=InnoDB;

-- =====================================================
-- 18. DATOS DE PRUEBA
-- =====================================================

INSERT INTO control (parametro, valor) VALUES
('Empleados', 13),
('Clientes', 7),
('Envios', 15),
('Manifiestos', 459),
('Reclamos', 1);

INSERT INTO sucursales VALUES
('S001','Agencia Central Lima','AGENCIA','Av. Javier Prado Este 1450, San Isidro','Lima',1),
('S002','Almacén Callao','ALMACEN','Av. Argentina 4521, Callao','Callao',1),
('S003','Tienda Recojo Miraflores','TIENDA_RECOJO','Av. Larco 345, Miraflores','Lima',1),
('S004','Hub Huancayo','HUB','Av. Ferrocarril 980, Huancayo','Junín',1);

INSERT INTO zonas (nombre, idsucursal) VALUES
('Lima Norte','S002'), ('Lima Centro','S001'), ('Lima Sur','S001'), ('Sierra Central','S004');

-- Claves de prueba: admin -> admin | demás usuarios -> 123456
INSERT INTO empleados VALUES
('E0001','Administrador','Sistema','admin@logiruta.pe','admin',SHA2('admin',256),'ADMINISTRADOR','S001',1,'ACTIVO'),
('E0002','Rosa','Quispe','rquispe@logiruta.pe','rquispe',SHA2('123456',256),'AGENTE_VENTANILLA','S001',1,'ACTIVO'),
('E0003','Luis','Mendoza','lmendoza@logiruta.pe','lmendoza',SHA2('123456',256),'OPERADOR_ALMACEN','S002',1,'ACTIVO'),
('E0004','Julia','Torres','jtorres@logiruta.pe','jtorres',SHA2('123456',256),'SUPERVISOR_RUTA','S002',1,'ACTIVO'),
('E0010','Iván','Cárdenas','icardenas@logiruta.pe','icardenas',SHA2('123456',256),'REPARTIDOR','S002',1,'ACTIVO'),
('E0011','Mario','Huamán','mhuaman@logiruta.pe','mhuaman',SHA2('123456',256),'REPARTIDOR','S002',1,'ACTIVO'),
('E0012','Carlos','Salazar','csalazar@logiruta.pe','csalazar',SHA2('123456',256),'REPARTIDOR','S004',1,'VACACIONES');

INSERT INTO clientes (idcliente, razonsocial, tipocliente, nrodocumento, email, telefono, usuario_portal) VALUES
('C0001','Cliente de prueba','NATURAL','45879632','cliente@correo.pe','987654321',NULL),
('C0002','Carla Romero Díaz','NATURAL','70123456','carla.romero@correo.pe','912345678',NULL),
('C0003','Estudio Contable Ríos & Asociados','CORPORATIVO','20548796321','mesa@riosasociados.pe','014217788','riosasoc'),
('C0004','Agroexport Mantaro S.A.','CORPORATIVO','20487654123','despachos@agromantaro.pe','064231100','agromantaro'),
('C0005','Tienda Virtual Andina S.A.C.','CORPORATIVO','20601234567','logistica@andina.pe','014445566','andina'),
('C0006','Diego Salas Vega','NATURAL','46321587','dsalas@correo.pe','976543210',NULL);

INSERT INTO tipos_servicio VALUES
('EXPRESS','Entrega express (24 h)',18.00,3.50,1,1,30.00),
('ESTANDAR','Envío estándar',10.00,1.80,3,3,NULL),
('PROGRAMADO','Mensajería programada',12.00,2.20,2,2,NULL),
('RECOJO_TIENDA','Recojo en tienda',6.00,1.20,2,4,NULL),
('INTERPROVINCIAL','Transporte interprovincial',25.00,2.50,5,3,500.00);

INSERT INTO servicios_adicionales VALUES
('SEGURO','Seguro contra pérdida (mín. S/ 5)','PORCENTAJE_VALOR',0.0200),
('FRAGIL','Embalaje y manipulación frágil','FIJO',8.0000),
('FIRMA','Confirmación con firma digital','FIJO',4.5000),
('GPS','Rastreo GPS en tiempo real','PORCENTAJE_TARIFA',0.1000);

INSERT INTO vehiculos VALUES
('ABC-123','FURGONETA',1200,1), ('MTO-456','MOTO',40,1), ('CMN-789','CAMION',8000,1);

INSERT INTO proveedores_integracion VALUES
('GOOGLE_MAPS','GEOLOCALIZACION','PRODUCCION',1),
('WHATSAPP','NOTIFICACION','PRODUCCION',1),
('FIRMA_CLOUD','FIRMA_DIGITAL','PRODUCCION',1),
('OSM','GEOLOCALIZACION','CONTINGENCIA',1),
('SMS_GATEWAY','NOTIFICACION','CONTINGENCIA',1),
('FIRMA_LOCAL','FIRMA_DIGITAL','CONTINGENCIA',1);

INSERT INTO direcciones (calle, distrito, provincia, departamento, latitud, longitud) VALUES
('Av. Argentina 4521','Callao','Callao','Callao',-12.048100,-77.105900),          -- 1 origen almacén
('Av. Brasil 1200','Jesús María','Lima','Lima',-12.071200,-77.048300),            -- 2
('Av. Larco 880','Miraflores','Lima','Lima',-12.121500,-77.029700),               -- 3
('Av. Universitaria 1801','San Martín de Porres','Lima','Lima',-12.018300,-77.083900), -- 4
('Jr. Cuzco 455','Breña','Lima','Lima',-12.055000,-77.050100),                    -- 5
('Av. Benavides 3020','Surco','Lima','Lima',-12.128000,-76.998000),               -- 6
('Av. Túpac Amaru 2150','Comas','Lima','Lima',-11.938000,-77.060200),             -- 7
('Calle Las Begonias 415','San Isidro','Lima','Lima',-12.091700,-77.022600),      -- 8
('Av. Naranjal 1240','Los Olivos','Lima','Lima',-11.973400,-77.071000),           -- 9
('Jr. Ancash 520','Huancayo','Huancayo','Junín',-12.068000,-75.210000),           -- 10
('Av. Arequipa 2450','Lince','Lima','Lima',-12.085300,-77.034600),                -- 11
('Av. Grau 150','Barranco','Lima','Lima',-12.149900,-77.021800);                  -- 12

-- Envíos en distintos estados (paso_flujo = posición en el flujo del servicio)
INSERT INTO envios (codigo, idcliente, idservicio, iddir_origen, iddir_destino, idzona_destino,
    nombre_dest, telefono_dest, email_dest, peso_kg, valor_declarado, fragil, con_seguro,
    requiere_firma, fecha_programada, idsucursal_recojo, estado, paso_flujo, intentos, costo,
    fecha_registro, fecha_estimada, fecha_entrega) VALUES
('LR00000001','C0005','ESTANDAR',1,2,2,'Luis Paredes','987111222',NULL,2.00,150,0,1,0,NULL,NULL,'ENTREGADO',5,0,18.60,
  DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('LR00000002','C0005','EXPRESS',1,3,3,'Ana Torres','934111222',NULL,1.00,0,0,0,1,NULL,NULL,'ENTREGADO',3,0,26.00,
  DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('LR00000003','C0001','ESTANDAR',1,4,1,'Jorge Lima','934333444',NULL,3.50,0,0,0,0,NULL,NULL,'ENTREGADO',5,1,16.30,
  DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(CURDATE(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('LR00000004','C0005','ESTANDAR',1,7,1,'Rita Flores','945678123',NULL,3.00,0,0,0,0,NULL,NULL,'EN_RUTA',4,0,15.40,
  DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 1 DAY), NULL),
('LR00000005','C0002','EXPRESS',1,6,3,'Elena Vega','934555666','elena.vega@correo.pe',0.80,0,0,0,1,NULL,NULL,'EN_RUTA',2,0,25.30,
  DATE_SUB(NOW(), INTERVAL 1 DAY), CURDATE(), NULL),
('LR00000006','C0005','ESTANDAR',1,9,1,'Pedro Ruiz','999888777',NULL,5.00,300,1,1,0,NULL,NULL,'CLASIFICADO',3,0,33.00,
  DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), NULL),
('LR00000007','C0003','PROGRAMADO',1,4,1,'Mesa de partes','955112233',NULL,0.50,0,0,0,1,DATE_ADD(CURDATE(), INTERVAL 1 DAY),NULL,'CLASIFICADO',2,0,17.60,
  DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 1 DAY), NULL),
('LR00000008','C0005','ESTANDAR',1,5,1,'Sofía Cáceres','922334455',NULL,1.50,0,0,0,0,NULL,NULL,'OBSERVADO',4,1,12.70,
  DATE_SUB(NOW(), INTERVAL 3 DAY), CURDATE(), NULL),
('LR00000009','C0004','INTERPROVINCIAL',1,10,4,'Almacén Agromantaro','964778899',NULL,120.00,5000,0,1,0,NULL,NULL,'EN_TRANSITO',3,0,425.00,
  DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 3 DAY), NULL),
('LR00000010','C0001','ESTANDAR',1,11,2,'Marco Ibáñez','911223344',NULL,1.20,0,0,0,0,NULL,NULL,'REGISTRADO',0,0,12.16,
  NOW(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), NULL),
('LR00000011','C0005','RECOJO_TIENDA',1,NULL,NULL,'Valeria Núñez','933221100',NULL,2.00,0,0,0,0,NULL,'S003','CLASIFICADO',3,0,8.40,
  DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 1 DAY), NULL),
('LR00000012','C0006','EXPRESS',1,12,3,'Diego Salas','976543210','dsalas@correo.pe',2.50,400,0,1,1,NULL,NULL,'RECOGIDO',1,0,39.25,
  NOW(), DATE_ADD(CURDATE(), INTERVAL 1 DAY), NULL),
('LR00000013','C0005','ESTANDAR',1,7,1,'Gabriela Soto','988776655',NULL,4.20,0,0,0,0,NULL,NULL,'CLASIFICADO',3,0,17.56,
  DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), NULL),
('LR00000014','C0003','ESTANDAR',1,9,1,'Notaría Paredes','944556677',NULL,0.80,0,0,0,1,NULL,NULL,'CLASIFICADO',3,0,15.94,
  DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), NULL);

INSERT INTO rutas (fecha, idzona, placa, idsupervisor) VALUES
(CURDATE(), 1, 'ABC-123', 'E0004'),
(CURDATE(), 3, 'MTO-456', 'E0004');

INSERT INTO asignaciones (codigo, idrepartidor, idruta) VALUES
('LR00000004','E0010',1), ('LR00000008','E0011',1), ('LR00000005','E0010',2);

INSERT INTO seguimientos (codigo, estado, ubicacion, observacion, idempleado, fechahora) VALUES
('LR00000001','REGISTRADO','Agencia Central Lima','Envío registrado','E0002',DATE_SUB(NOW(), INTERVAL 6 DAY)),
('LR00000001','RECOGIDO','Almacén Callao','Paquete recogido','E0003',DATE_SUB(NOW(), INTERVAL 6 DAY)),
('LR00000001','EN_TRANSITO','Almacén Callao','Traslado a almacén','E0003',DATE_SUB(NOW(), INTERVAL 5 DAY)),
('LR00000001','CLASIFICADO','Almacén Callao','Clasificado zona Lima Centro','E0003',DATE_SUB(NOW(), INTERVAL 5 DAY)),
('LR00000001','EN_RUTA','Jesús María','Salida a reparto','E0010',DATE_SUB(NOW(), INTERVAL 4 DAY)),
('LR00000001','ENTREGADO','Jesús María','Entregado al destinatario','E0010',DATE_SUB(NOW(), INTERVAL 4 DAY)),
('LR00000002','REGISTRADO','Agencia Central Lima','Envío registrado','E0002',DATE_SUB(NOW(), INTERVAL 5 DAY)),
('LR00000002','RECOGIDO','Agencia Central Lima','Paquete recogido','E0003',DATE_SUB(NOW(), INTERVAL 5 DAY)),
('LR00000002','EN_RUTA','Miraflores','Salida a reparto express','E0010',DATE_SUB(NOW(), INTERVAL 4 DAY)),
('LR00000002','ENTREGADO','Miraflores','Entregado con firma digital','E0010',DATE_SUB(NOW(), INTERVAL 4 DAY)),
('LR00000003','REGISTRADO','Agencia Central Lima','Envío registrado','E0002',DATE_SUB(NOW(), INTERVAL 9 DAY)),
('LR00000003','RECOGIDO','Almacén Callao','Paquete recogido','E0003',DATE_SUB(NOW(), INTERVAL 9 DAY)),
('LR00000003','EN_TRANSITO','Almacén Callao','Traslado a almacén','E0003',DATE_SUB(NOW(), INTERVAL 8 DAY)),
('LR00000003','CLASIFICADO','Almacén Callao','Clasificado zona Lima Norte','E0003',DATE_SUB(NOW(), INTERVAL 8 DAY)),
('LR00000003','EN_RUTA','San Martín de Porres','Salida a reparto','E0011',DATE_SUB(NOW(), INTERVAL 7 DAY)),
('LR00000003','OBSERVADO','San Martín de Porres','Ausencia del destinatario','E0011',DATE_SUB(NOW(), INTERVAL 7 DAY)),
('LR00000003','REPROGRAMADO','Almacén Callao','Nueva visita coordinada','E0004',DATE_SUB(NOW(), INTERVAL 6 DAY)),
('LR00000003','EN_RUTA','San Martín de Porres','Segundo intento','E0011',DATE_SUB(NOW(), INTERVAL 4 DAY)),
('LR00000003','ENTREGADO','San Martín de Porres','Entregado al destinatario','E0011',DATE_SUB(NOW(), INTERVAL 4 DAY)),
('LR00000004','REGISTRADO','Agencia Central Lima','Envío registrado','E0002',DATE_SUB(NOW(), INTERVAL 2 DAY)),
('LR00000004','RECOGIDO','Almacén Callao','Paquete recogido','E0003',DATE_SUB(NOW(), INTERVAL 2 DAY)),
('LR00000004','EN_TRANSITO','Almacén Callao','Traslado a almacén','E0003',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000004','CLASIFICADO','Almacén Callao','Clasificado zona Lima Norte','E0003',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000004','EN_RUTA','Comas','Salida a reparto (manifiesto MF-2026-0457)','E0004',NOW()),
('LR00000005','REGISTRADO','Agencia Central Lima','Envío registrado','E0002',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000005','RECOGIDO','Agencia Central Lima','Paquete recogido','E0003',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000005','EN_RUTA','Surco','Salida a reparto express','E0004',NOW()),
('LR00000006','REGISTRADO','Agencia Central Lima','Envío registrado','E0002',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000006','RECOGIDO','Almacén Callao','Paquete recogido','E0003',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000006','EN_TRANSITO','Almacén Callao','Traslado a almacén','E0003',NOW()),
('LR00000006','CLASIFICADO','Almacén Callao','Clasificado zona Lima Norte','E0003',NOW()),
('LR00000007','REGISTRADO','Agencia Central Lima','Envío registrado','E0002',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000007','RECOGIDO','Agencia Central Lima','Paquete recogido','E0003',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000007','CLASIFICADO','Almacén Callao','Clasificado zona Lima Norte','E0003',NOW()),
('LR00000008','REGISTRADO','Agencia Central Lima','Envío registrado','E0002',DATE_SUB(NOW(), INTERVAL 3 DAY)),
('LR00000008','RECOGIDO','Almacén Callao','Paquete recogido','E0003',DATE_SUB(NOW(), INTERVAL 3 DAY)),
('LR00000008','EN_TRANSITO','Almacén Callao','Traslado a almacén','E0003',DATE_SUB(NOW(), INTERVAL 2 DAY)),
('LR00000008','CLASIFICADO','Almacén Callao','Clasificado zona Lima Norte','E0003',DATE_SUB(NOW(), INTERVAL 2 DAY)),
('LR00000008','EN_RUTA','Breña','Salida a reparto','E0011',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000008','OBSERVADO','Breña','Dirección incorrecta: numeración no existe','E0011',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000009','REGISTRADO','Agencia Central Lima','Envío registrado','E0002',DATE_SUB(NOW(), INTERVAL 2 DAY)),
('LR00000009','RECOGIDO','Almacén Callao','Carga recogida','E0003',DATE_SUB(NOW(), INTERVAL 2 DAY)),
('LR00000009','CLASIFICADO','Almacén Callao','Consolidado para Huancayo','E0003',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000009','EN_TRANSITO','Carretera Central km 120','En tránsito interprovincial','E0003',NOW()),
('LR00000010','REGISTRADO','Agencia Central Lima','Envío registrado','E0002',NOW()),
('LR00000011','REGISTRADO','Agencia Central Lima','Envío registrado','E0002',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000011','RECOGIDO','Almacén Callao','Paquete recogido','E0003',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000011','EN_TRANSITO','Almacén Callao','Traslado a tienda','E0003',NOW()),
('LR00000011','CLASIFICADO','Tienda Recojo Miraflores','Disponible para recojo','E0003',NOW()),
('LR00000012','REGISTRADO','Agencia Central Lima','Envío registrado','E0002',NOW()),
('LR00000012','RECOGIDO','Agencia Central Lima','Paquete recogido','E0003',NOW()),
('LR00000013','REGISTRADO','Agencia Central Lima','Envío registrado','E0002',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000013','RECOGIDO','Almacén Callao','Paquete recogido','E0003',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000013','EN_TRANSITO','Almacén Callao','Traslado a almacén','E0003',NOW()),
('LR00000013','CLASIFICADO','Almacén Callao','Clasificado zona Lima Norte','E0003',NOW()),
('LR00000014','REGISTRADO','Agencia Central Lima','Envío registrado','E0002',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000014','RECOGIDO','Almacén Callao','Paquete recogido','E0003',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000014','EN_TRANSITO','Almacén Callao','Traslado a almacén','E0003',NOW()),
('LR00000014','CLASIFICADO','Almacén Callao','Clasificado zona Lima Norte','E0003',NOW());

INSERT INTO incidencias (codigo, motivo, descripcion, idzona, idempleado, fecha) VALUES
('LR00000003','AUSENCIA_DESTINATARIO','Nadie en domicilio',1,'E0011',DATE_SUB(NOW(), INTERVAL 7 DAY)),
('LR00000008','DIRECCION_INCORRECTA','Numeración no existe',1,'E0011',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000001','INCIDENTE_OPERATIVO','Retraso por falla de vehículo',2,'E0010',DATE_SUB(NOW(), INTERVAL 5 DAY)),
('LR00000009','CLIMA','Huaico en Carretera Central',4,'E0003',DATE_SUB(NOW(), INTERVAL 1 DAY)),
('LR00000004','AUSENCIA_DESTINATARIO','Primer intento sin respuesta (histórico)',1,'E0010',DATE_SUB(NOW(), INTERVAL 10 DAY)),
('LR00000002','OTRO','Portería no permitió ingreso',3,'E0010',DATE_SUB(NOW(), INTERVAL 4 DAY));

INSERT INTO historial_laboral (idempleado, estado_anterior, estado_nuevo, motivo, usuario) VALUES
('E0012','ACTIVO','VACACIONES','Vacaciones programadas (15 días)','E0001');

INSERT INTO alertas (codigo, nivel, mensaje) VALUES
('LR00000008','ALTA','Envío observado (intento 1/3): Dirección incorrecta');

-- =====================================================
-- 19. PROCEDIMIENTOS ALMACENADOS
-- =====================================================

DELIMITER $$

-- ---------- EMPLEADOS (mismo estilo que AppEmpresa) ----------
CREATE PROCEDURE sp_Empleado_Adicionar(
    IN p_id CHAR(5), IN p_nombre VARCHAR(80), IN p_apellidos VARCHAR(100),
    IN p_email VARCHAR(120), IN p_usuario VARCHAR(50), IN p_clave VARCHAR(100),
    IN p_rol VARCHAR(25), IN p_sucursal CHAR(4))
BEGIN
    INSERT INTO empleados (idempleado, nombre, apellidos, email, usuario, clave, rol, idsucursal)
    VALUES (p_id, p_nombre, p_apellidos, p_email, p_usuario, SHA2(p_clave,256), p_rol, p_sucursal);
END$$

CREATE PROCEDURE sp_Empleado_Actualizar(
    IN p_id CHAR(5), IN p_nombre VARCHAR(80), IN p_apellidos VARCHAR(100),
    IN p_email VARCHAR(120), IN p_usuario VARCHAR(50), IN p_clave VARCHAR(100),
    IN p_rol VARCHAR(25), IN p_sucursal CHAR(4))
BEGIN
    UPDATE empleados
    SET nombre = p_nombre, apellidos = p_apellidos, email = p_email, usuario = p_usuario,
        clave = IF(p_clave IS NULL OR p_clave = '', clave, SHA2(p_clave,256)),
        rol = p_rol, idsucursal = p_sucursal
    WHERE idempleado = p_id;
END$$

CREATE PROCEDURE sp_Empleado_Eliminar(IN p_id CHAR(5))
BEGIN
    -- Baja lógica: el empleado tiene historial de seguimiento asociado
    UPDATE empleados SET activo = 0, estado_laboral = 'INACTIVO' WHERE idempleado = p_id;
END$$

-- Persiste el estado laboral calculado por el patrón State
CREATE PROCEDURE sp_Empleado_CambiarEstadoLaboral(IN p_id CHAR(5), IN p_estado VARCHAR(12))
BEGIN
    UPDATE empleados
    SET estado_laboral = p_estado,
        activo = IF(p_estado = 'INACTIVO', 0, 1)
    WHERE idempleado = p_id;
END$$

CREATE PROCEDURE sp_HistorialLaboral_Registrar(
    IN p_id CHAR(5), IN p_anterior VARCHAR(12), IN p_nuevo VARCHAR(12),
    IN p_motivo VARCHAR(200), IN p_usuario CHAR(5))
BEGIN
    INSERT INTO historial_laboral (idempleado, estado_anterior, estado_nuevo, motivo, usuario)
    VALUES (p_id, p_anterior, p_nuevo, p_motivo, p_usuario);
END$$

-- ---------- CLIENTES ----------
CREATE PROCEDURE sp_Cliente_Adicionar(
    IN p_id CHAR(5), IN p_razon VARCHAR(150), IN p_tipo VARCHAR(12),
    IN p_doc VARCHAR(11), IN p_email VARCHAR(120), IN p_tel VARCHAR(20))
BEGIN
    INSERT INTO clientes (idcliente, razonsocial, tipocliente, nrodocumento, email, telefono)
    VALUES (p_id, p_razon, p_tipo, p_doc, p_email, p_tel);
END$$

CREATE PROCEDURE sp_Cliente_Actualizar(
    IN p_id CHAR(5), IN p_razon VARCHAR(150), IN p_tipo VARCHAR(12),
    IN p_doc VARCHAR(11), IN p_email VARCHAR(120), IN p_tel VARCHAR(20))
BEGIN
    UPDATE clientes SET razonsocial = p_razon, tipocliente = p_tipo, nrodocumento = p_doc,
           email = p_email, telefono = p_tel
    WHERE idcliente = p_id;
END$$

CREATE PROCEDURE sp_Cliente_Eliminar(IN p_id CHAR(5))
BEGIN
    IF EXISTS (SELECT 1 FROM envios WHERE idcliente = p_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El cliente tiene envíos registrados';
    END IF;
    DELETE FROM clientes WHERE idcliente = p_id;
END$$

-- ---------- DIRECCIONES (parámetro de salida, como USP_PROVEEDOR_INSERTA) ----------
CREATE PROCEDURE sp_Direccion_Registrar(
    OUT p_id INT, IN p_calle VARCHAR(180), IN p_distrito VARCHAR(80),
    IN p_provincia VARCHAR(80), IN p_departamento VARCHAR(80),
    IN p_lat DECIMAL(9,6), IN p_lng DECIMAL(9,6))
BEGIN
    INSERT INTO direcciones (calle, distrito, provincia, departamento, latitud, longitud)
    VALUES (p_calle, p_distrito, p_provincia, p_departamento, p_lat, p_lng);
    SET p_id = LAST_INSERT_ID();
END$$

-- ---------- ENVIOS ----------
CREATE PROCEDURE sp_Envio_Registrar(
    IN p_codigo CHAR(10), IN p_idcliente CHAR(5), IN p_idservicio VARCHAR(20),
    IN p_dir_origen INT, IN p_dir_destino INT, IN p_zona INT,
    IN p_nombre VARCHAR(120), IN p_telefono VARCHAR(20), IN p_email VARCHAR(120),
    IN p_peso DECIMAL(10,2), IN p_valor DECIMAL(12,2),
    IN p_fragil TINYINT, IN p_seguro TINYINT, IN p_firma TINYINT,
    IN p_fecha_prog DATE, IN p_tienda CHAR(4), IN p_costo DECIMAL(12,2))
BEGIN
    DECLARE v_dias INT;
    SELECT dias_entrega INTO v_dias FROM tipos_servicio WHERE idservicio = p_idservicio;

    INSERT INTO envios (codigo, idcliente, idservicio, iddir_origen, iddir_destino, idzona_destino,
                        nombre_dest, telefono_dest, email_dest, peso_kg, valor_declarado,
                        fragil, con_seguro, requiere_firma, fecha_programada,
                        idsucursal_recojo, costo, fecha_estimada)
    VALUES (p_codigo, p_idcliente, p_idservicio, p_dir_origen, p_dir_destino, p_zona,
            p_nombre, p_telefono, p_email, p_peso, p_valor,
            p_fragil, p_seguro, p_firma, p_fecha_prog, p_tienda, p_costo,
            COALESCE(p_fecha_prog, DATE_ADD(CURDATE(), INTERVAL v_dias DAY)));
END$$

-- Persiste el estado que calculó el patrón State (lo invoca el observador
-- PersistenciaEnvioObserver). Si se deshace una entrega, se limpia la fecha.
CREATE PROCEDURE sp_Envio_ActualizarEstado(
    IN p_codigo CHAR(10), IN p_estado VARCHAR(15), IN p_paso TINYINT,
    IN p_intentos TINYINT, IN p_fecha_prog DATE, IN p_firma VARCHAR(40))
BEGIN
    UPDATE envios
    SET estado = p_estado,
        paso_flujo = p_paso,
        intentos = p_intentos,
        fecha_programada = p_fecha_prog,
        codigo_firma = p_firma,
        fecha_entrega = IF(p_estado = 'ENTREGADO', COALESCE(fecha_entrega, NOW()), NULL)
    WHERE codigo = p_codigo;
END$$

-- ---------- SEGUIMIENTO / INCIDENCIAS / NOTIFICACIONES / ALERTAS ----------
CREATE PROCEDURE sp_Seguimiento_Registrar(
    IN p_codigo CHAR(10), IN p_estado VARCHAR(15), IN p_ubicacion VARCHAR(120),
    IN p_observacion VARCHAR(250), IN p_idempleado CHAR(5))
BEGIN
    INSERT INTO seguimientos (codigo, estado, ubicacion, observacion, idempleado)
    VALUES (p_codigo, p_estado, p_ubicacion, p_observacion, p_idempleado);
END$$

-- Solo registra la incidencia: el cambio de estado lo hace el patrón State
CREATE PROCEDURE sp_Incidencia_Registrar(
    IN p_codigo CHAR(10), IN p_motivo VARCHAR(25),
    IN p_descripcion VARCHAR(250), IN p_idempleado CHAR(5))
BEGIN
    INSERT INTO incidencias (codigo, motivo, descripcion, idzona, idempleado)
    SELECT p_codigo, p_motivo, p_descripcion, e.idzona_destino, p_idempleado
    FROM envios e WHERE e.codigo = p_codigo;
END$$

CREATE PROCEDURE sp_Notificacion_Registrar(
    IN p_codigo CHAR(10), IN p_canal VARCHAR(15), IN p_dest VARCHAR(120),
    IN p_mensaje VARCHAR(500), IN p_enviado TINYINT)
BEGIN
    INSERT INTO notificaciones (codigo, canal, destinatario, mensaje, enviado)
    VALUES (p_codigo, p_canal, p_dest, p_mensaje, p_enviado);
END$$

CREATE PROCEDURE sp_Alerta_Registrar(
    IN p_codigo CHAR(10), IN p_nivel VARCHAR(10), IN p_mensaje VARCHAR(250))
BEGIN
    INSERT INTO historial_laboral (idempleado, estado_anterior, estado_nuevo, motivo, usuario) VALUES
('E0012','ACTIVO','VACACIONES','Vacaciones programadas (15 días)','E0001');

INSERT INTO alertas (codigo, nivel, mensaje) VALUES (p_codigo, p_nivel, p_mensaje);
END$$

CREATE PROCEDURE sp_Auditoria_Registrar(
    IN p_usuario CHAR(5), IN p_rol VARCHAR(25), IN p_accion VARCHAR(40),
    IN p_codigo CHAR(10), IN p_permitido TINYINT)
BEGIN
    INSERT INTO auditoria (idusuario, rol, accion, codigo, permitido)
    VALUES (p_usuario, p_rol, p_accion, p_codigo, p_permitido);
END$$

-- ---------- COMANDOS (Command) ----------
CREATE PROCEDURE sp_Comando_Registrar(
    OUT p_id BIGINT, IN p_codigo CHAR(10), IN p_comando VARCHAR(40),
    IN p_descripcion VARCHAR(250), IN p_previo VARCHAR(15),
    IN p_nuevo VARCHAR(15), IN p_idempleado CHAR(5))
BEGIN
    INSERT INTO comandos_log (codigo, comando, descripcion, estado_previo, estado_nuevo, idempleado)
    VALUES (p_codigo, p_comando, p_descripcion, p_previo, p_nuevo, p_idempleado);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE sp_Comando_MarcarDeshecho(IN p_id BIGINT, IN p_deshecho TINYINT)
BEGIN
    UPDATE comandos_log
    SET deshecho = p_deshecho,
        fecha_deshecho = IF(p_deshecho = 1, NOW(), NULL)
    WHERE idcomando = p_id;
END$$

-- ---------- MANIFIESTOS (Composite) ----------
CREATE PROCEDURE sp_Ruta_Crear(
    OUT p_id INT, IN p_idzona INT, IN p_placa CHAR(7), IN p_supervisor CHAR(5))
BEGIN
    INSERT INTO rutas (fecha, idzona, placa, idsupervisor)
    VALUES (CURDATE(), p_idzona, p_placa, p_supervisor);
    SET p_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE sp_Unidad_Registrar(
    IN p_id VARCHAR(15), IN p_tipo VARCHAR(12), IN p_tara DECIMAL(10,2),
    IN p_padre VARCHAR(15), IN p_ruta INT)
BEGIN
    INSERT INTO unidades_carga (idunidad, tipo, tara_kg, idpadre, idruta)
    VALUES (p_id, p_tipo, p_tara, p_padre, p_ruta);
END$$

CREATE PROCEDURE sp_UnidadDetalle_Registrar(IN p_unidad VARCHAR(15), IN p_codigo CHAR(10))
BEGIN
    INSERT INTO unidad_detalle (idunidad, codigo) VALUES (p_unidad, p_codigo);
END$$

CREATE PROCEDURE sp_Asignar_Repartidor(
    IN p_codigo CHAR(10), IN p_idrepartidor CHAR(5), IN p_idruta INT)
BEGIN
    INSERT INTO asignaciones (codigo, idrepartidor, idruta)
    VALUES (p_codigo, p_idrepartidor, p_idruta);
END$$

-- =====================================================
-- 20. TRIGGERS: el historial de seguimiento es inmutable
-- (por eso "deshacer" agrega un registro de reversión, no borra)
-- =====================================================

CREATE TRIGGER tg_seguimientos_no_update
BEFORE UPDATE ON seguimientos FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El historial de seguimiento no puede modificarse';
END$$

CREATE TRIGGER tg_seguimientos_no_delete
BEFORE DELETE ON seguimientos FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El historial de seguimiento no puede eliminarse';
END$$

DELIMITER ;

-- =====================================================
-- 21. VISTAS PARA REPORTES ESTRATEGICOS (Bridge)
-- =====================================================

CREATE OR REPLACE VIEW vw_incidencias_zona AS
SELECT z.nombre AS zona, COUNT(*) AS incidencias
FROM incidencias i JOIN zonas z ON z.idzona = i.idzona
GROUP BY z.nombre;

CREATE OR REPLACE VIEW vw_cumplimiento_tiempos AS
SELECT idservicio AS servicio,
       COUNT(*) AS entregados,
       SUM(CASE WHEN DATE(fecha_entrega) <= fecha_estimada THEN 1 ELSE 0 END) AS a_tiempo
FROM envios
WHERE estado = 'ENTREGADO'
GROUP BY idservicio;

CREATE OR REPLACE VIEW vw_productividad_reparto AS
SELECT CONCAT(e.nombre, ' ', e.apellidos) AS repartidor,
       SUM(CASE WHEN s.estado = 'ENTREGADO' THEN 1 ELSE 0 END) AS entregas,
       SUM(CASE WHEN s.estado = 'OBSERVADO' THEN 1 ELSE 0 END) AS observados
FROM seguimientos s JOIN empleados e ON e.idempleado = s.idempleado
WHERE e.rol = 'REPARTIDOR'
GROUP BY e.idempleado, e.nombre, e.apellidos;

-- =====================================================
-- 22. VERIFICAR LA BASE DE DATOS
-- =====================================================

SHOW TABLES;
SELECT codigo, idservicio, estado, paso_flujo, intentos FROM envios;
SELECT * FROM vw_incidencias_zona;
SELECT * FROM vw_cumplimiento_tiempos;
SELECT idempleado, usuario, rol, estado_laboral FROM empleados;
