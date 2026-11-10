-- =====================================================
-- AppLogistica - Actualización Sesión 13 (Command + Memento) y reportes Jasper
-- Úsese SOLO si ya tiene la base "logistica" del Avance 2 con datos que desea
-- conservar. Si instala desde cero, basta con 01_instalacion_inicial.sql
-- (que ya incluye todo lo de este archivo).
-- Haga un respaldo antes de ejecutarlo. Puede ejecutarse más de una vez.
-- =====================================================

SET NAMES utf8mb4;
USE logistica;
-- MySQL Workbench activa el "safe update mode" por defecto; se desactiva solo para este script
SET SQL_SAFE_UPDATES = 0;

-- 1. Sueldo y área del empleado (estado que guarda el Memento)
DROP PROCEDURE IF EXISTS sp_tmp_agregar_columnas;
DELIMITER $$
CREATE PROCEDURE sp_tmp_agregar_columnas()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = 'logistica' AND TABLE_NAME = 'empleados' AND COLUMN_NAME = 'sueldo') THEN
        ALTER TABLE empleados ADD COLUMN sueldo DECIMAL(10,2) NOT NULL DEFAULT 1500.00 AFTER estado_laboral;
        UPDATE empleados SET sueldo = 6500.00 WHERE idempleado = 'E0001';
        UPDATE empleados SET sueldo = 1800.00 WHERE idempleado = 'E0002';
        UPDATE empleados SET sueldo = 2000.00 WHERE idempleado = 'E0003';
        UPDATE empleados SET sueldo = 3200.00 WHERE idempleado = 'E0004';
        UPDATE empleados SET sueldo = 1600.00 WHERE idempleado IN ('E0010','E0011');
        UPDATE empleados SET sueldo = 1550.00 WHERE idempleado = 'E0012';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = 'logistica' AND TABLE_NAME = 'empleados' AND COLUMN_NAME = 'area') THEN
        ALTER TABLE empleados ADD COLUMN area VARCHAR(40) NOT NULL DEFAULT 'OPERACIONES' AFTER sueldo;
        UPDATE empleados SET area = 'GERENCIA' WHERE idempleado = 'E0001';
        UPDATE empleados SET area = 'ATENCION AL CLIENTE' WHERE idempleado = 'E0002';
        UPDATE empleados SET area = 'ALMACEN' WHERE idempleado = 'E0003';
        UPDATE empleados SET area = 'REPARTO' WHERE idempleado IN ('E0010','E0011','E0012');
    END IF;
END$$
DELIMITER ;
CALL sp_tmp_agregar_columnas();
DROP PROCEDURE sp_tmp_agregar_columnas;

-- Corrige una ejecución anterior que se interrumpió a la mitad (columnas creadas con valores por defecto)
UPDATE empleados SET sueldo = 6500.00 WHERE idempleado = 'E0001' AND sueldo = 1500.00;
UPDATE empleados SET sueldo = 1800.00 WHERE idempleado = 'E0002' AND sueldo = 1500.00;
UPDATE empleados SET sueldo = 2000.00 WHERE idempleado = 'E0003' AND sueldo = 1500.00;
UPDATE empleados SET sueldo = 3200.00 WHERE idempleado = 'E0004' AND sueldo = 1500.00;
UPDATE empleados SET sueldo = 1600.00 WHERE idempleado IN ('E0010','E0011') AND sueldo = 1500.00;
UPDATE empleados SET sueldo = 1550.00 WHERE idempleado = 'E0012' AND sueldo = 1500.00;
UPDATE empleados SET area = 'GERENCIA' WHERE idempleado = 'E0001' AND area = 'OPERACIONES';
UPDATE empleados SET area = 'ATENCION AL CLIENTE' WHERE idempleado = 'E0002' AND area = 'OPERACIONES';
UPDATE empleados SET area = 'ALMACEN' WHERE idempleado = 'E0003' AND area = 'OPERACIONES';
UPDATE empleados SET area = 'REPARTO' WHERE idempleado IN ('E0010','E0011','E0012') AND area = 'OPERACIONES';

-- 2. Historial de remuneraciones (lo escribe EmpleadoNomina)
CREATE TABLE IF NOT EXISTS historial_remuneracion (
    idhistorial BIGINT PRIMARY KEY AUTO_INCREMENT,
    idempleado CHAR(5) NOT NULL,
    operacion VARCHAR(20) NOT NULL,
    sueldo_anterior DECIMAL(10,2) NOT NULL,
    sueldo_nuevo DECIMAL(10,2) NOT NULL,
    area_anterior VARCHAR(40) NOT NULL,
    area_nueva VARCHAR(40) NOT NULL,
    usuario CHAR(5),
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hr_empleado FOREIGN KEY (idempleado) REFERENCES empleados(idempleado)
) ENGINE=InnoDB;

-- 3. Procedimiento usado por EmpleadoDao.actualizarRemuneracion
DROP PROCEDURE IF EXISTS sp_Empleado_ActualizarRemuneracion;
DELIMITER $$
CREATE PROCEDURE sp_Empleado_ActualizarRemuneracion(
    IN p_id CHAR(5), IN p_sueldo DECIMAL(10,2), IN p_area VARCHAR(40),
    IN p_operacion VARCHAR(20), IN p_usuario CHAR(5))
BEGIN
    DECLARE v_sueldo DECIMAL(10,2);
    DECLARE v_area VARCHAR(40);
    SELECT sueldo, area INTO v_sueldo, v_area FROM empleados WHERE idempleado = p_id FOR UPDATE;
    UPDATE empleados SET sueldo = p_sueldo, area = p_area WHERE idempleado = p_id;
    INSERT INTO historial_remuneracion (idempleado, operacion, sueldo_anterior, sueldo_nuevo,
                                        area_anterior, area_nueva, usuario)
    VALUES (p_id, p_operacion, v_sueldo, p_sueldo, v_area, p_area, p_usuario);
END$$
DELIMITER ;

-- 4. Manifiesto de ejemplo para el reporte Jasper (solo si no existe)
INSERT INTO unidades_carga (idunidad, tipo, tara_kg, idpadre, idruta)
SELECT 'MF-2026-0458', 'MANIFIESTO', 0.00, NULL, 1 FROM DUAL
WHERE EXISTS (SELECT 1 FROM rutas WHERE idruta = 1)
  AND NOT EXISTS (SELECT 1 FROM unidades_carga WHERE idunidad = 'MF-2026-0458');
INSERT INTO unidades_carga (idunidad, tipo, tara_kg, idpadre, idruta)
SELECT 'PL-0458-1', 'PALETA', 8.00, 'MF-2026-0458', 1 FROM DUAL
WHERE EXISTS (SELECT 1 FROM unidades_carga WHERE idunidad = 'MF-2026-0458')
  AND NOT EXISTS (SELECT 1 FROM unidades_carga WHERE idunidad = 'PL-0458-1');
INSERT INTO unidades_carga (idunidad, tipo, tara_kg, idpadre, idruta)
SELECT 'SC-0458-1', 'SACO', 0.40, 'MF-2026-0458', 1 FROM DUAL
WHERE EXISTS (SELECT 1 FROM unidades_carga WHERE idunidad = 'MF-2026-0458')
  AND NOT EXISTS (SELECT 1 FROM unidades_carga WHERE idunidad = 'SC-0458-1');
INSERT IGNORE INTO unidad_detalle (idunidad, codigo)
SELECT 'PL-0458-1', 'LR00000004' FROM DUAL
WHERE EXISTS (SELECT 1 FROM unidades_carga WHERE idunidad = 'PL-0458-1')
  AND EXISTS (SELECT 1 FROM envios WHERE codigo = 'LR00000004');
INSERT IGNORE INTO unidad_detalle (idunidad, codigo)
SELECT 'SC-0458-1', 'LR00000008' FROM DUAL
WHERE EXISTS (SELECT 1 FROM unidades_carga WHERE idunidad = 'SC-0458-1')
  AND EXISTS (SELECT 1 FROM envios WHERE codigo = 'LR00000008');

SET SQL_SAFE_UPDATES = 1;

-- 5. Verificación
SELECT idempleado, usuario, rol, estado_laboral, sueldo, area FROM empleados;
SELECT idunidad, tipo, idpadre FROM unidades_carga WHERE idunidad LIKE '%0458%';
