USE vitalis_citas;
INSERT INTO paciente
    (id_usuario, dni, nombres, apellidos, fecha_nacimiento, sexo, telefono, email, direccion)
VALUES
    (NULL, '48372916', 'Carla', 'Valdivia Rojas', '1994-02-18', 'F', '999000003', 'carla.valdivia94@gmail.com', 'Av. Los Olivos 125'),
    (NULL, '72841503', 'Mateo', 'Salazar Ponce', '1987-07-05', 'M', '999000004', 'mateo.salazar87@outlook.com', 'Jr. Las Flores 240'),
    (NULL, '35690274', 'Lucía', 'Mendoza Chávez', '2001-11-22', 'F', '999000005', 'lucia.mendoza01@gmail.com', 'Calle Central 318'),
    (NULL, '69418320', 'Diego', 'Cárdenas Ruiz', '1979-03-14', 'M', '999000006', 'diego.cardenas79@outlook.com', 'Av. Primavera 460'),
    (NULL, '51284739', 'Valeria', 'Espinoza León', '1998-09-30', 'F', '999000007', 'valeria.espinoza98@gmail.com', 'Jr. Los Pinos 512'),
    (NULL, '83726041', 'Andrés', 'Paredes Silva', '1991-06-11', 'M', '999000008', 'andres.paredes91@outlook.com', 'Calle Las Gardenias 175'),
    (NULL, '26953184', 'Camila', 'Núñez Vargas', '2003-01-27', 'F', '999000009', 'camila.nunez03@gmail.com', 'Av. Los Próceres 630'),
    (NULL, '74190632', 'Joaquín', 'Herrera Flores', '1983-12-08', 'M', '999000010', 'joaquin.herrera83@outlook.com', 'Jr. San Martín 208'),
    (NULL, '40571829', 'Daniela', 'Castro Medina', '1996-05-19', 'F', '999000011', 'daniela.castro96@gmail.com', 'Calle Las Dalias 341'),
    (NULL, '91836425', 'Sebastián', 'Ríos Cabrera', '1975-08-02', 'M', '999000012', 'sebastian.rios75@outlook.com', 'Av. El Sol 520'),
    (NULL, '58302716', 'Mariana', 'Vega Campos', '2000-04-16', 'F', '999000013', 'mariana.vega00@gmail.com', 'Jr. Los Cedros 114'),
    (NULL, '67429508', 'Gabriel', 'Flores Acosta', '1989-10-25', 'M', '999000014', 'gabriel.flores89@outlook.com', 'Calle Los Álamos 286'),
    (NULL, '32086147', 'Paola', 'Reátegui Molina', '1993-07-09', 'F', '999000015', 'paola.reategui93@gmail.com', 'Av. La Marina 475'),
    (NULL, '85641392', 'Nicolás', 'Vargas Soto', '1981-01-31', 'M', '999000016', 'nicolas.vargas81@outlook.com', 'Jr. Las Palmeras 390'),
    (NULL, '19754268', 'Andrea', 'Quispe Torres', '1999-03-06', 'F', '999000017', 'andrea.quispe99@gmail.com', 'Calle Los Jazmines 158'),
    (NULL, '46283905', 'Renato', 'Aguilar Peña', '1977-11-13', 'M', '999000018', 'renato.aguilar77@outlook.com', 'Av. Universitaria 710'),
    (NULL, '73158426', 'Fernanda', 'Delgado Cruz', '2002-06-28', 'F', '999000019', 'fernanda.delgado02@gmail.com', 'Jr. Los Laureles 224'),
    (NULL, '29864713', 'Emilio', 'Gutiérrez Ramos', '1985-02-04', 'M', '999000020', 'emilio.gutierrez85@outlook.com', 'Calle Santa Rosa 430'),
    (NULL, '64507291', 'Rosa', 'Navarro Paredes', '1972-09-17', 'F', '999000021', 'rosa.navarro72@gmail.com', 'Av. Arequipa 890'),
    (NULL, '50931864', 'Alonso', 'Márquez Salinas', '1997-12-21', 'M', '999000022', 'alonso.marquez97@outlook.com', 'Jr. Las Magnolias 302'),
    (NULL, '87420653', 'Gabriela', 'Campos Rivas', '1990-05-03', 'F', '999000023', 'gabriela.campos90@gmail.com', 'Calle Los Tulipanes 215'),
    (NULL, '23679518', 'Tomás', 'León Cabrera', '1986-08-15', 'M', '999000024', 'tomas.leon86@outlook.com', 'Av. Los Héroes 640'),
    (NULL, '68194327', 'Elena', 'Sánchez Poma', '1995-10-07', 'F', '999000025', 'elena.sanchez95@gmail.com', 'Jr. Santa Ana 180');

-- Las citas relacionan al paciente por DNI para no depender de id_paciente fijos.
-- Los IDs de médico corresponden al orden de carga del esquema inicial.
INSERT INTO cita
    (id_paciente, id_medico, fecha, hora, estado, canal, motivo)
SELECT p.id_paciente, r.id_medico, r.fecha, r.hora,
       'CONFIRMADA', 'PORTAL', r.motivo
FROM paciente AS p
JOIN (
    SELECT '48372916' AS dni, 1 AS id_medico, '2026-12-07' AS fecha, '08:00:00' AS hora, 'Consulta general' AS motivo
    UNION ALL SELECT '72841503', 3, '2026-12-07', '15:00:00', 'Control de salud'
    UNION ALL SELECT '35690274', 2, '2026-12-08', '09:00:00', 'Evaluación odontológica'
    UNION ALL SELECT '69418320', 1, '2026-12-08', '15:00:00', 'Consulta general'
    UNION ALL SELECT '51284739', 1, '2026-12-09', '08:00:00', 'Evaluación médica'
    UNION ALL SELECT '83726041', 4, '2026-12-09', '15:00:00', 'Consulta ginecológica'
    UNION ALL SELECT '26953184', 2, '2026-12-10', '09:00:00', 'Control odontológico'
    UNION ALL SELECT '74190632', 3, '2026-12-10', '08:00:00', 'Consulta pediátrica'
    UNION ALL SELECT '40571829', 1, '2026-12-11', '08:00:00', 'Consulta general'
    UNION ALL SELECT '91836425', 4, '2026-12-11', '15:00:00', 'Control ginecológico'
    UNION ALL SELECT '58302716', 1, '2026-12-14', '08:30:00', 'Evaluación médica'
    UNION ALL SELECT '67429508', 3, '2026-12-14', '15:20:00', 'Consulta pediátrica'
    UNION ALL SELECT '32086147', 2, '2026-12-15', '09:30:00', 'Evaluación odontológica'
    UNION ALL SELECT '85641392', 1, '2026-12-15', '15:30:00', 'Consulta general'
    UNION ALL SELECT '19754268', 1, '2026-12-16', '08:30:00', 'Control médico'
    UNION ALL SELECT '46283905', 4, '2026-12-16', '15:30:00', 'Consulta ginecológica'
    UNION ALL SELECT '73158426', 2, '2026-12-17', '09:30:00', 'Control odontológico'
    UNION ALL SELECT '29864713', 3, '2026-12-17', '08:20:00', 'Consulta pediátrica'
    UNION ALL SELECT '64507291', 1, '2026-12-18', '08:30:00', 'Consulta general'
    UNION ALL SELECT '50931864', 4, '2026-12-18', '15:30:00', 'Control ginecológico'
    UNION ALL SELECT '87420653', 1, '2026-12-21', '09:00:00', 'Evaluación médica'
    UNION ALL SELECT '23679518', 3, '2026-12-24', '08:40:00', 'Consulta pediátrica'
    UNION ALL SELECT '68194327', 2, '2026-12-29', '10:00:00', 'Evaluación odontológica'
) AS r ON r.dni = p.dni;
