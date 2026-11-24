-- =====================================================
-- AppLogistica - Actualización final: seguridad del login
-- Bloqueo de la cuenta tras 3 contraseñas incorrectas seguidas
-- (5 minutos, o hasta que el ADMINISTRADOR la desbloquee).
-- Úsese SOLO si ya tiene la base "logistica" con datos que desea conservar
-- (después de 03_actualizacion_semana17.sql). Si instala desde cero, basta con
-- 01_instalacion_inicial.sql, que ya incluye todo lo de este archivo.
-- Puede ejecutarse más de una vez.
-- Los gráficos circulares de los reportes NO necesitan cambios en la base.
-- =====================================================

SET NAMES utf8mb4;
USE logistica;
SET SQL_SAFE_UPDATES = 0;

-- 1. Contador de intentos y fecha de fin del bloqueo por usuario
CREATE TABLE IF NOT EXISTS bloqueos_login (
    usuario VARCHAR(50) PRIMARY KEY,
    intentos_fallidos TINYINT NOT NULL DEFAULT 0,
    bloqueado_hasta DATETIME NULL,
    ultimo_fallo DATETIME NULL,
    CONSTRAINT ck_bloqueo_intentos CHECK (intentos_fallidos >= 0)
) ENGINE=InnoDB;

-- 2. Bitácora de intentos de ingreso (exitosos, fallidos, bloqueos y desbloqueos)
CREATE TABLE IF NOT EXISTS intentos_login (
    idintento BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario VARCHAR(50) NOT NULL,
    exitoso TINYINT(1) NOT NULL,
    detalle VARCHAR(80),
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Desbloqueo manual (lo usa el ADMINISTRADOR desde Utilitarios > Desbloquear usuarios)
DROP PROCEDURE IF EXISTS sp_Login_Desbloquear;
DELIMITER $$
CREATE PROCEDURE sp_Login_Desbloquear(IN p_usuario VARCHAR(50))
BEGIN
    UPDATE bloqueos_login SET intentos_fallidos = 0, bloqueado_hasta = NULL WHERE usuario = p_usuario;
    INSERT INTO intentos_login (usuario, exitoso, detalle) VALUES (p_usuario, 0, 'DESBLOQUEADO POR ADMINISTRADOR');
END$$
DELIMITER ;

SET SQL_SAFE_UPDATES = 1;

-- Verificación
SHOW TABLES LIKE '%login%';
SELECT * FROM bloqueos_login;
