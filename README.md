# Vitalis Citas — Sistema Web de Gestión de Citas Médicas

Proyecto del curso **Desarrollo Web Integrado** — Universidad Tecnológica del Perú (2026-II).
Docente: MBA Mg. Ing. René Alonso Nieto Valencia.

Sistema web para la **Clínica Vitalis** (escenario ficticio, Piura) que reemplaza un sistema legado de 2014:
los pacientes reservan, reprograman y cancelan sus citas en línea, y el personal gestiona la agenda,
los médicos, los horarios y los reportes.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Backend | Java 17 · Spring Boot 3 · Spring Data JPA (Hibernate) · Spring Security + JWT |
| Frontend | Angular · SCSS (SASS) · Bootstrap 5 |
| Base de datos | MySQL 8.0 |
| Librerías de apoyo | Google Guava · Apache POI · Apache Commons Lang3 · Logback |
| Pruebas | JUnit 5 · Mockito · MockMvc (TDD) |

## Estructura del repositorio

```
vitalis-citas/
├── backend/    API REST con Spring Boot (Maven)
├── frontend/   Aplicación Angular
└── docs/       Informe, script SQL, diagramas y backlog
```

## Equipo

| Integrante | Rol |
|---|---|
| César Sembrera Guevara | Líder de proyecto / Backend · responsable del repositorio |
| Jimmi Leonardo Velásquez Alegre | Frontend |
| Milagros Gutierrez Salas | Analista / QA |
| Hernán Ysidro Poma Quiroz | Documentador |

## Cómo trabajamos con Git

- **`main`**: versión estable de cada entrega (etiquetas `v0.2.0-APF2`, `v0.3.0-APF3`, …).
- **`develop`**: integración del trabajo del equipo.
- **`feature/backend-<módulo>`** y **`feature/frontend-<vista>`**: una rama por tarea (issue).
- Todo cambio entra a `develop` mediante **Pull Request** revisado por otro integrante.

Mensajes de commit (Conventional Commits): `tipo(alcance): descripción`

| Tipo | Uso | Ejemplo |
|---|---|---|
| `feat` | Nueva funcionalidad | `feat(auth): agregar login con JWT` |
| `fix` | Corrección | `fix(citas): validar horario pasado` |
| `test` | Pruebas | `test(disponibilidad): caso de turnos ocupados` |
| `docs` | Documentación | `docs: actualizar README` |
| `refactor` | Cambio interno | `refactor(service): extraer CitaMapper` |
| `chore` | Configuración | `chore(pom): agregar Apache POI` |
| `style` | Estilos | `style(reserva): ajustar vista móvil` |

## Ejecución local

> Se completará al terminar la configuración del backend y del frontend.

1. Crear la base de datos con `docs/vitalis_citas_schema.sql` (MySQL 8.0).
2. Backend: `cd backend` → `./mvnw spring-boot:run` (puerto 8080).
3. Frontend: `cd frontend` → `npm install` → `ng serve` (puerto 4200).

**Nunca subas contraseñas al repositorio.** La clave de MySQL se pasa con la variable de entorno `DB_PASSWORD`.
