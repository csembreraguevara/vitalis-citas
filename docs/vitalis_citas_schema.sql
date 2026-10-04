-- =====================================================================
-- Sistema Web de Gestión de Citas Médicas — Clínica Vitalis
-- Script de base de datos MySQL 8.0  (versión 1.0 — APF2)
-- Motor InnoDB · juego de caracteres utf8mb4
-- =====================================================================
DROP DATABASE IF EXISTS vitalis_citas;
CREATE DATABASE vitalis_citas CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vitalis_citas;

-- ---------------------------------------------------------------------
-- 1. USUARIO: credenciales y rol de acceso (Spring Security + JWT)
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
  id_usuario      BIGINT       NOT NULL AUTO_INCREMENT,
  username        VARCHAR(100) NOT NULL,
  password_hash   VARCHAR(60)  NOT NULL COMMENT 'Hash BCrypt, nunca texto plano',
  rol             ENUM('PACIENTE','RECEPCIONISTA','MEDICO','ADMIN') NOT NULL,
  activo          TINYINT(1)   NOT NULL DEFAULT 1,
  intentos_fallidos TINYINT    NOT NULL DEFAULT 0,
  bloqueado_hasta DATETIME     NULL,
  fecha_creacion  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ultimo_acceso   DATETIME     NULL,
  CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
  CONSTRAINT uk_usuario_username UNIQUE (username)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2. PACIENTE
-- ---------------------------------------------------------------------
CREATE TABLE paciente (
  id_paciente      BIGINT       NOT NULL AUTO_INCREMENT,
  id_usuario       BIGINT       NULL COMMENT 'NULL si fue registrado por recepción sin cuenta',
  dni              CHAR(8)      NOT NULL,
  nombres          VARCHAR(80)  NOT NULL,
  apellidos        VARCHAR(80)  NOT NULL,
  fecha_nacimiento DATE         NOT NULL,
  sexo             CHAR(1)      NOT NULL,
  telefono         VARCHAR(15)  NULL,
  email            VARCHAR(120) NOT NULL,
  direccion        VARCHAR(200) NULL,
  fecha_registro   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_paciente PRIMARY KEY (id_paciente),
  CONSTRAINT uk_paciente_dni UNIQUE (dni),
  CONSTRAINT uk_paciente_usuario UNIQUE (id_usuario),
  CONSTRAINT fk_paciente_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
  CONSTRAINT ck_paciente_dni CHECK (dni REGEXP '^[0-9]{8}$'),
  CONSTRAINT ck_paciente_sexo CHECK (sexo IN ('F','M'))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. ESPECIALIDAD
-- ---------------------------------------------------------------------
CREATE TABLE especialidad (
  id_especialidad   BIGINT       NOT NULL AUTO_INCREMENT,
  nombre            VARCHAR(60)  NOT NULL,
  descripcion       VARCHAR(250) NULL,
  duracion_cita_min SMALLINT     NOT NULL DEFAULT 30,
  activo            TINYINT(1)   NOT NULL DEFAULT 1,
  CONSTRAINT pk_especialidad PRIMARY KEY (id_especialidad),
  CONSTRAINT uk_especialidad_nombre UNIQUE (nombre),
  CONSTRAINT ck_especialidad_duracion CHECK (duracion_cita_min BETWEEN 10 AND 120)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 4. MEDICO
-- ---------------------------------------------------------------------
CREATE TABLE medico (
  id_medico       BIGINT       NOT NULL AUTO_INCREMENT,
  id_usuario      BIGINT       NULL,
  id_especialidad BIGINT       NOT NULL,
  cmp             VARCHAR(10)  NOT NULL COMMENT 'Colegio Médico del Perú',
  nombres         VARCHAR(80)  NOT NULL,
  apellidos       VARCHAR(80)  NOT NULL,
  telefono        VARCHAR(15)  NULL,
  email           VARCHAR(120) NULL,
  consultorio     VARCHAR(30)  NOT NULL,
  activo          TINYINT(1)   NOT NULL DEFAULT 1,
  CONSTRAINT pk_medico PRIMARY KEY (id_medico),
  CONSTRAINT uk_medico_cmp UNIQUE (cmp),
  CONSTRAINT uk_medico_usuario UNIQUE (id_usuario),
  CONSTRAINT fk_medico_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
  CONSTRAINT fk_medico_especialidad FOREIGN KEY (id_especialidad) REFERENCES especialidad (id_especialidad)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 5. HORARIO_MEDICO: turnos semanales (1 = lunes ... 7 = domingo)
-- ---------------------------------------------------------------------
CREATE TABLE horario_medico (
  id_horario  BIGINT     NOT NULL AUTO_INCREMENT,
  id_medico   BIGINT     NOT NULL,
  dia_semana  TINYINT    NOT NULL,
  hora_inicio TIME       NOT NULL,
  hora_fin    TIME       NOT NULL,
  activo      TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT pk_horario_medico PRIMARY KEY (id_horario),
  CONSTRAINT uk_horario_medico UNIQUE (id_medico, dia_semana, hora_inicio),
  CONSTRAINT fk_horario_medico FOREIGN KEY (id_medico) REFERENCES medico (id_medico) ON DELETE CASCADE,
  CONSTRAINT ck_horario_dia CHECK (dia_semana BETWEEN 1 AND 7),
  CONSTRAINT ck_horario_rango CHECK (hora_fin > hora_inicio)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 6. CITA
--    slot_activo es una columna generada: vale 1 mientras la cita ocupa
--    el horario y NULL cuando fue cancelada o expiró. Como MySQL admite
--    varios NULL en un índice UNIQUE, la restricción uk_cita_slot impide
--    que dos citas activas ocupen el mismo médico-fecha-hora (elimina la
--    duplicidad) y a la vez permite volver a reservar un horario liberado.
-- ---------------------------------------------------------------------
CREATE TABLE cita (
  id_cita             BIGINT       NOT NULL AUTO_INCREMENT,
  id_paciente         BIGINT       NOT NULL,
  id_medico           BIGINT       NOT NULL,
  fecha               DATE         NOT NULL,
  hora                TIME         NOT NULL,
  estado              ENUM('PENDIENTE','CONFIRMADA','ATENDIDA','CANCELADA','NO_ASISTIO','EXPIRADA') NOT NULL DEFAULT 'PENDIENTE',
  canal               ENUM('PORTAL','RECEPCION') NOT NULL,
  motivo              VARCHAR(250) NULL,
  motivo_cancelacion  VARCHAR(250) NULL,
  expira_en           DATETIME     NULL COMMENT 'Fin de la reserva temporal (5 min)',
  slot_activo         TINYINT GENERATED ALWAYS AS
                        (IF(estado IN ('CANCELADA','EXPIRADA'), NULL, 1)) STORED,
  registrado_por      BIGINT       NULL,
  fecha_registro      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,
  version             INT          NOT NULL DEFAULT 0 COMMENT 'Bloqueo optimista JPA (@Version)',
  CONSTRAINT pk_cita PRIMARY KEY (id_cita),
  CONSTRAINT uk_cita_slot UNIQUE (id_medico, fecha, hora, slot_activo),
  CONSTRAINT fk_cita_paciente FOREIGN KEY (id_paciente) REFERENCES paciente (id_paciente),
  CONSTRAINT fk_cita_medico FOREIGN KEY (id_medico) REFERENCES medico (id_medico),
  CONSTRAINT fk_cita_usuario FOREIGN KEY (registrado_por) REFERENCES usuario (id_usuario)
) ENGINE=InnoDB;

CREATE INDEX ix_cita_paciente ON cita (id_paciente, fecha);
CREATE INDEX ix_cita_fecha_estado ON cita (fecha, estado);

-- ---------------------------------------------------------------------
-- 7. NOTIFICACION: cola de correos (confirmación, recordatorio, etc.)
-- ---------------------------------------------------------------------
CREATE TABLE notificacion (
  id_notificacion  BIGINT       NOT NULL AUTO_INCREMENT,
  id_cita          BIGINT       NOT NULL,
  tipo             ENUM('CONFIRMACION','RECORDATORIO','REPROGRAMACION','CANCELACION') NOT NULL,
  destinatario     VARCHAR(120) NOT NULL,
  asunto           VARCHAR(150) NOT NULL,
  estado           ENUM('PENDIENTE','ENVIADO','FALLIDO') NOT NULL DEFAULT 'PENDIENTE',
  fecha_programada DATETIME     NOT NULL,
  fecha_envio      DATETIME     NULL,
  intentos         TINYINT      NOT NULL DEFAULT 0,
  CONSTRAINT pk_notificacion PRIMARY KEY (id_notificacion),
  CONSTRAINT fk_notificacion_cita FOREIGN KEY (id_cita) REFERENCES cita (id_cita) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX ix_notificacion_pendiente ON notificacion (estado, fecha_programada);

-- ---------------------------------------------------------------------
-- 8. AUDITORIA: trazabilidad de accesos y operaciones (Ley 29733)
-- ---------------------------------------------------------------------
CREATE TABLE auditoria (
  id_auditoria BIGINT       NOT NULL AUTO_INCREMENT,
  id_usuario   BIGINT       NULL,
  accion       VARCHAR(40)  NOT NULL COMMENT 'LOGIN, CREAR_CITA, CANCELAR_CITA, EXPORTAR_REPORTE...',
  entidad      VARCHAR(40)  NULL,
  id_entidad   BIGINT       NULL,
  detalle      VARCHAR(500) NULL,
  ip           VARCHAR(45)  NULL,
  fecha        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_auditoria PRIMARY KEY (id_auditoria),
  CONSTRAINT fk_auditoria_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
) ENGINE=InnoDB;

CREATE INDEX ix_auditoria_fecha ON auditoria (fecha);

-- =====================================================================
-- DATOS DE PRUEBA (escenario ficticio)
-- Contraseña de todos los usuarios de prueba: Vitalis2026  (hash BCrypt)
-- =====================================================================
INSERT INTO usuario (username, password_hash, rol) VALUES
 ('admin',                 '$2a$10$CLK/VG3gY8.fg3L8L966R.cOCjYEOgW/GTcH1kR4hC3JNDYpw28h2', 'ADMIN'),
 ('recepcion01',           '$2a$10$CLK/VG3gY8.fg3L8L966R.cOCjYEOgW/GTcH1kR4hC3JNDYpw28h2', 'RECEPCIONISTA'),
 ('jramirez',              '$2a$10$CLK/VG3gY8.fg3L8L966R.cOCjYEOgW/GTcH1kR4hC3JNDYpw28h2', 'MEDICO'),
 ('ana.torres@correo.com', '$2a$10$CLK/VG3gY8.fg3L8L966R.cOCjYEOgW/GTcH1kR4hC3JNDYpw28h2', 'PACIENTE');

INSERT INTO especialidad (nombre, descripcion, duracion_cita_min) VALUES
 ('Medicina general', 'Atención primaria y evaluación general', 30),
 ('Pediatría',        'Atención de niños y adolescentes',       20),
 ('Ginecología',      'Salud de la mujer',                      30),
 ('Odontología',      'Salud bucal',                            30);

INSERT INTO medico (id_usuario, id_especialidad, cmp, nombres, apellidos, consultorio) VALUES
 (3,    1, '045218', 'Jorge',  'Ramírez Chávez', 'C-104'),
 (NULL, 4, '051733', 'Carmen', 'Salazar Ruiz',   'C-105'),
 (NULL, 2, '038904', 'Luis',   'Núñez Paredes',  'C-201'),
 (NULL, 3, '060127', 'Rocío',  'Castillo Mena',  'C-202');

INSERT INTO horario_medico (id_medico, dia_semana, hora_inicio, hora_fin) VALUES
 (1, 1, '08:00', '13:00'), (1, 2, '15:00', '19:00'), (1, 3, '08:00', '13:00'), (1, 5, '08:00', '12:00'),
 (2, 2, '09:00', '13:00'), (2, 4, '09:00', '13:00'),
 (3, 1, '15:00', '19:00'), (3, 4, '08:00', '12:00'),
 (4, 3, '15:00', '19:00'), (4, 5, '15:00', '19:00');

INSERT INTO paciente (id_usuario, dni, nombres, apellidos, fecha_nacimiento, sexo, telefono, email) VALUES
 (4,    '45781203', 'Ana',  'Torres Vega',  '1990-04-12', 'F', '969123456', 'ana.torres@correo.com'),
 (NULL, '45120876', 'Luis', 'García Pérez', '1985-11-03', 'M', '978456123', 'luis.garcia@correo.com');
