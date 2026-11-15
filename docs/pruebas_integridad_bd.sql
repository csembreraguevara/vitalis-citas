-- =====================================================================
-- Sistema Web de Gestión de Citas Médicas — Clínica Vitalis
-- Pruebas de integridad de la base de datos vitalis_citas (MySQL 8.x)
--
-- Verifica que las restricciones del diseño físico (sección 3.2.5.2 del
-- informe) se cumplen en el motor de base de datos:
--   * uk_cita_slot impide dos citas activas en el mismo horario (RN / RNF003)
--   * una cita cancelada libera el horario para una nueva reserva
--   * restricciones CHECK, UNIQUE y FOREIGN KEY
--
-- Todas las pruebas se ejecutan dentro de una transacción que se revierte
-- al final: la base de datos queda exactamente igual que antes.
--
-- Uso (desde la carpeta del repositorio):
--   mysql -u root -p -e "source docs/pruebas_integridad_bd.sql"
-- =====================================================================
USE vitalis_citas;

DROP PROCEDURE IF EXISTS sp_pruebas_integridad;

DELIMITER $$
CREATE PROCEDURE sp_pruebas_integridad()
BEGIN
  DECLARE v_errno INT DEFAULT 0;
  DECLARE v_id_cita BIGINT;
  DECLARE v_slot TINYINT;

  -- Captura el código de error de MySQL sin detener el procedimiento
  DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
  BEGIN
    GET DIAGNOSTICS CONDITION 1 v_errno = MYSQL_ERRNO;
  END;

  -- Tabla de resultados en memoria: no se ve afectada por el ROLLBACK
  DROP TEMPORARY TABLE IF EXISTS resultados;
  CREATE TEMPORARY TABLE resultados (
    n          INT AUTO_INCREMENT PRIMARY KEY,
    prueba     VARCHAR(90) NOT NULL,
    esperado   INT         NOT NULL,
    obtenido   INT         NOT NULL
  ) ENGINE = MEMORY;

  START TRANSACTION;

  -- 1. Reserva válida
  SET v_errno = 0;
  INSERT INTO cita (id_paciente, id_medico, fecha, hora, canal)
  VALUES (1, 1, '2026-10-19', '08:00', 'PORTAL');
  SET v_id_cita = LAST_INSERT_ID();
  INSERT INTO resultados (prueba, esperado, obtenido)
  VALUES ('Reservar una cita en un horario libre', 0, v_errno);

  -- 2. Duplicidad: mismo médico, fecha y hora con la cita anterior activa
  SET v_errno = 0;
  INSERT INTO cita (id_paciente, id_medico, fecha, hora, canal)
  VALUES (2, 1, '2026-10-19', '08:00', 'RECEPCION');
  INSERT INTO resultados (prueba, esperado, obtenido)
  VALUES ('Rechazar una segunda cita activa en el mismo horario', 1062, v_errno);

  -- 3. Cancelar la cita: slot_activo debe pasar a NULL
  SET v_errno = 0;
  UPDATE cita SET estado = 'CANCELADA', motivo_cancelacion = 'Prueba'
  WHERE id_cita = v_id_cita;
  SELECT slot_activo INTO v_slot FROM cita WHERE id_cita = v_id_cita;
  INSERT INTO resultados (prueba, esperado, obtenido)
  VALUES ('Cancelar la cita libera el horario (slot_activo = NULL)', 0,
          IF(v_errno = 0 AND v_slot IS NULL, 0, 1));

  -- 4. Volver a reservar el horario liberado
  SET v_errno = 0;
  INSERT INTO cita (id_paciente, id_medico, fecha, hora, canal)
  VALUES (2, 1, '2026-10-19', '08:00', 'RECEPCION');
  INSERT INTO resultados (prueba, esperado, obtenido)
  VALUES ('Reservar de nuevo un horario liberado por cancelación', 0, v_errno);

  -- 5. Cita con un médico inexistente (clave foránea)
  SET v_errno = 0;
  INSERT INTO cita (id_paciente, id_medico, fecha, hora, canal)
  VALUES (1, 999, '2026-10-19', '09:00', 'PORTAL');
  INSERT INTO resultados (prueba, esperado, obtenido)
  VALUES ('Rechazar una cita con un médico inexistente (FK)', 1452, v_errno);

  -- 6. DNI con formato inválido
  SET v_errno = 0;
  INSERT INTO paciente (dni, nombres, apellidos, fecha_nacimiento, sexo, email)
  VALUES ('123', 'Prueba', 'DNI corto', '2000-01-01', 'M', 'p1@correo.com');
  INSERT INTO resultados (prueba, esperado, obtenido)
  VALUES ('Rechazar un DNI que no tiene 8 dígitos (CHECK)', 3819, v_errno);

  -- 7. Sexo fuera del dominio
  SET v_errno = 0;
  INSERT INTO paciente (dni, nombres, apellidos, fecha_nacimiento, sexo, email)
  VALUES ('70000001', 'Prueba', 'Sexo', '2000-01-01', 'X', 'p2@correo.com');
  INSERT INTO resultados (prueba, esperado, obtenido)
  VALUES ('Rechazar un sexo distinto de F o M (CHECK)', 3819, v_errno);

  -- 8. DNI duplicado
  SET v_errno = 0;
  INSERT INTO paciente (dni, nombres, apellidos, fecha_nacimiento, sexo, email)
  VALUES ('45781203', 'Prueba', 'Duplicado', '2000-01-01', 'F', 'p3@correo.com');
  INSERT INTO resultados (prueba, esperado, obtenido)
  VALUES ('Rechazar un paciente con DNI repetido (UNIQUE)', 1062, v_errno);

  -- 9. Nombre de usuario duplicado
  SET v_errno = 0;
  INSERT INTO usuario (username, password_hash, rol)
  VALUES ('admin', '$2a$10$CLK/VG3gY8.fg3L8L966R.cOCjYEOgW/GTcH1kR4hC3JNDYpw28h2', 'ADMIN');
  INSERT INTO resultados (prueba, esperado, obtenido)
  VALUES ('Rechazar un nombre de usuario repetido (UNIQUE)', 1062, v_errno);

  -- 10. Horario con hora de fin anterior a la de inicio
  SET v_errno = 0;
  INSERT INTO horario_medico (id_medico, dia_semana, hora_inicio, hora_fin)
  VALUES (2, 1, '13:00', '09:00');
  INSERT INTO resultados (prueba, esperado, obtenido)
  VALUES ('Rechazar un turno con hora_fin <= hora_inicio (CHECK)', 3819, v_errno);

  -- 11. Día de la semana fuera de rango
  SET v_errno = 0;
  INSERT INTO horario_medico (id_medico, dia_semana, hora_inicio, hora_fin)
  VALUES (2, 9, '08:00', '12:00');
  INSERT INTO resultados (prueba, esperado, obtenido)
  VALUES ('Rechazar un día de la semana fuera de 1..7 (CHECK)', 3819, v_errno);

  -- 12. Duración de cita fuera de rango
  SET v_errno = 0;
  INSERT INTO especialidad (nombre, duracion_cita_min)
  VALUES ('Especialidad de prueba', 5);
  INSERT INTO resultados (prueba, esperado, obtenido)
  VALUES ('Rechazar una duración de cita menor a 10 min (CHECK)', 3819, v_errno);

  ROLLBACK;

  -- Reporte
  SELECT n AS 'N.',
         prueba AS 'Prueba',
         IF(esperado = 0, 'Acepta', CONCAT('Rechaza (', esperado, ')')) AS 'Esperado',
         IF(obtenido = 0, 'Acepta', CONCAT('Rechaza (', obtenido, ')')) AS 'Obtenido',
         IF(esperado = obtenido, 'OK', 'FALLA') AS 'Resultado'
  FROM resultados ORDER BY n;

  SELECT CONCAT(SUM(esperado = obtenido), ' de ', COUNT(*), ' pruebas correctas') AS 'Resumen'
  FROM resultados;

  DROP TEMPORARY TABLE resultados;
END$$
DELIMITER ;

CALL sp_pruebas_integridad();
DROP PROCEDURE sp_pruebas_integridad;
