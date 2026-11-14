-- =====================================================
-- AppLogistica - Actualización Sesiones 14 y 17 (antipatrones y GRASP: boleta de pago)
-- Úsese SOLO si ya tiene la base "logistica" con datos que desea conservar
-- (después de 02_actualizacion_sesion13.sql). Si instala desde cero, basta con
-- 01_instalacion_inicial.sql, que ya incluye todo lo de este archivo.
-- Puede ejecutarse más de una vez.
-- =====================================================

SET NAMES utf8mb4;
USE logistica;
-- MySQL Workbench activa el "safe update mode" por defecto; se desactiva solo para este script
SET SQL_SAFE_UPDATES = 0;

-- 1. Régimen pensionario del empleado (Polimorfismo: DescuentoAFP / DescuentoSNP)
DROP PROCEDURE IF EXISTS sp_tmp_agregar_regimen;
DELIMITER $$
CREATE PROCEDURE sp_tmp_agregar_regimen()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = 'logistica' AND TABLE_NAME = 'empleados'
                     AND COLUMN_NAME = 'regimen_pensionario') THEN
        ALTER TABLE empleados ADD COLUMN regimen_pensionario ENUM('AFP','SNP') NOT NULL DEFAULT 'AFP' AFTER area;
        UPDATE empleados SET regimen_pensionario = 'SNP' WHERE idempleado IN ('E0003','E0011');
    END IF;
END$$
DELIMITER ;
CALL sp_tmp_agregar_regimen();
DROP PROCEDURE sp_tmp_agregar_regimen;

-- 2. Boletas de pago (las emite GeneradorBoleta, Fabricación pura)
CREATE TABLE IF NOT EXISTS boletas_pago (
    idboleta INT PRIMARY KEY AUTO_INCREMENT,
    idempleado CHAR(5) NOT NULL,
    periodo CHAR(7) NOT NULL,                 -- AAAA-MM
    sueldo_basico DECIMAL(10,2) NOT NULL,
    horas_extras INT NOT NULL DEFAULT 0,
    monto_horas_extras DECIMAL(10,2) NOT NULL,
    regimen VARCHAR(5) NOT NULL,
    aporte_pension DECIMAL(10,2) NOT NULL,
    descuento_salud DECIMAL(10,2) NOT NULL,
    total_ingresos DECIMAL(10,2) NOT NULL,
    total_descuentos DECIMAL(10,2) NOT NULL,
    sueldo_neto DECIMAL(10,2) NOT NULL,
    usuario CHAR(5),
    fecha_emision DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_boleta_periodo UNIQUE (idempleado, periodo),
    CONSTRAINT ck_boleta_horas CHECK (horas_extras >= 0),
    CONSTRAINT fk_bol_empleado FOREIGN KEY (idempleado) REFERENCES empleados(idempleado)
) ENGINE=InnoDB;

-- 3. Procedimientos
DROP PROCEDURE IF EXISTS sp_Empleado_CambiarRegimen;
DROP PROCEDURE IF EXISTS sp_Boleta_Registrar;
DELIMITER $$
CREATE PROCEDURE sp_Empleado_CambiarRegimen(IN p_id CHAR(5), IN p_regimen VARCHAR(5))
BEGIN
    UPDATE empleados SET regimen_pensionario = p_regimen WHERE idempleado = p_id;
END$$

CREATE PROCEDURE sp_Boleta_Registrar(
    OUT p_id INT, IN p_empleado CHAR(5), IN p_periodo CHAR(7), IN p_basico DECIMAL(10,2),
    IN p_horas INT, IN p_monto_horas DECIMAL(10,2), IN p_regimen VARCHAR(5),
    IN p_pension DECIMAL(10,2), IN p_salud DECIMAL(10,2), IN p_ingresos DECIMAL(10,2),
    IN p_descuentos DECIMAL(10,2), IN p_neto DECIMAL(10,2), IN p_usuario CHAR(5))
BEGIN
    -- Si la boleta del periodo ya existe se vuelve a emitir con los nuevos montos
    INSERT INTO boletas_pago (idempleado, periodo, sueldo_basico, horas_extras, monto_horas_extras, regimen,
                              aporte_pension, descuento_salud, total_ingresos, total_descuentos, sueldo_neto, usuario)
    VALUES (p_empleado, p_periodo, p_basico, p_horas, p_monto_horas, p_regimen,
            p_pension, p_salud, p_ingresos, p_descuentos, p_neto, p_usuario)
    ON DUPLICATE KEY UPDATE sueldo_basico = p_basico, horas_extras = p_horas, monto_horas_extras = p_monto_horas,
            regimen = p_regimen, aporte_pension = p_pension, descuento_salud = p_salud,
            total_ingresos = p_ingresos, total_descuentos = p_descuentos, sueldo_neto = p_neto,
            usuario = p_usuario, fecha_emision = CURRENT_TIMESTAMP;
    SELECT idboleta INTO p_id FROM boletas_pago WHERE idempleado = p_empleado AND periodo = p_periodo;
END$$
DELIMITER ;

SET SQL_SAFE_UPDATES = 1;

-- 4. Verificación
SELECT idempleado, usuario, rol, sueldo, area, regimen_pensionario FROM empleados;
SELECT COUNT(*) AS boletas FROM boletas_pago;
