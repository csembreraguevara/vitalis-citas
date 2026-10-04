# Backlog del proyecto — issues para GitHub

Cada fila es un **issue**. Créalos en GitHub con la plantilla «Tarea / Requisito», asígnalos al responsable
y ponles la etiqueta y el milestone indicados. Así cada integrante tiene trabajo propio y visible.

**Etiquetas a crear:** `backend` · `frontend` · `bd` · `requisito` · `bug` · `mejora` · `documentación` · `pruebas`
**Milestones a crear:** `APF2` (vence 13/10/2026) · `APF3` (vence 17/11/2026) · `Final` (vence 07/12/2026)

## Milestone APF2 (semana 10)

| # | Título del issue | Responsable | Etiquetas | Requisito |
|---|---|---|---|---|
| 1 | Configurar proyecto Spring Boot y conexión a MySQL | César | backend, bd | — |
| 2 | Crear entidades JPA y enums del dominio | César | backend | — |
| 3 | CRUD de especialidades (API REST) | Hernán | backend, requisito | RF005 |
| 4 | CRUD de médicos y horarios (API REST) | Hernán | backend, requisito | RF006, RF007 |
| 5 | CRUD y búsqueda de pacientes (API REST) | Milagros | backend, requisito | RF004 |
| 6 | Pruebas TDD de PacienteService y MedicoService | Milagros | pruebas | RNF007 |
| 7 | Consulta de disponibilidad con JPQL | César | backend, requisito | RF008 |
| 8 | Reserva, confirmación y cancelación de citas (transacciones) | César | backend, requisito | RF009, RF012 |
| 9 | Pruebas TDD de DisponibilidadService y CitaService | Milagros | pruebas | RN01–RN05 |
| 10 | Spring Security, roles y login con JWT | César | backend, requisito | RF002, RF003 |
| 11 | Manejo global de errores y validación de DTO | Hernán | backend | RNF010 |
| 12 | Logback y auditoría de operaciones | Hernán | backend, requisito | RF018, RNF009 |
| 13 | Crear proyecto Angular con SCSS, rutas y layout | Jimmi | frontend | — |
| 14 | Pantalla de login con interceptor JWT | Jimmi | frontend, requisito | RF002 |
| 15 | Pantalla «Reservar cita» consumiendo la API | Jimmi | frontend, requisito | RF008, RF009 |
| 16 | Pantalla «Agenda del día» (recepción) | Jimmi | frontend, requisito | RF013 |
| 17 | Colección Postman de la API | Milagros | pruebas | — |
| 18 | README, Javadoc y capturas para el informe APF2 | Hernán | documentación | — |

## Milestone APF3 (semana 15)

| # | Título del issue | Responsable | Etiquetas | Requisito |
|---|---|---|---|---|
| 19 | Registro de cuenta de paciente (backend + pantalla) | Milagros | backend, frontend, requisito | RF001 |
| 20 | Reprogramación de citas | César | backend, requisito | RF011 |
| 21 | Registro de cita por recepción | Jimmi | frontend, requisito | RF010 |
| 22 | Pantalla «Mis citas» | Jimmi | frontend, requisito | RF011, RF012 |
| 23 | Panel de administración de médicos y horarios | Hernán | frontend, requisito | RF006, RF007 |
| 24 | Notificaciones por correo y recordatorio programado | César | backend, requisito | RF014, RF015 |
| 25 | Reportes R01 y R02 con Apache POI | Hernán | backend, requisito | RF016, RF017 |
| 26 | Registro de atención por el médico | Milagros | backend, frontend, requisito | RF013 |

## Milestone Final (semana 18)

| # | Título del issue | Responsable | Etiquetas |
|---|---|---|---|
| 27 | Pruebas de integración frontend–backend | Milagros | pruebas |
| 28 | Despliegue en la nube (backend, frontend y MySQL) | César | backend, frontend |
| 29 | Manual de usuario y manual técnico | Hernán | documentación |
| 30 | Ajustes de diseño responsive y accesibilidad | Jimmi | frontend, mejora |
