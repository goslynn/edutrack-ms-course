# Course Service — EduTrack

Microservicio de **cursos y asignación de docentes** del libro de clases digital del Colegio Bernardo O'Higgins. Gestiona el CRUD de cursos con borrado lógico y la relación docente↔curso con nivel de acceso granular. Implementa dos capas de autorización: por tipo de recurso (roles de Auth) y por instancia (asignación docente-curso). Implementa `BE-CRS-001..003`.

- **Path raíz (Gateway):** `/course`
- **Schema BD:** `course` (PostgreSQL compartido)
- **App Fly.io:** `edutrack-course`

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Runtime | Java 21 + Quarkus 3.34.6 |
| API | RESTEasy Reactive (JAX-RS) |
| Persistencia | Hibernate ORM Panache — Active Record |
| Base de datos | PostgreSQL 15+ |
| Migraciones | Flyway (corre automáticamente al arrancar) |
| Serialización | Jackson + `@JsonView` |
| Validación | Hibernate Validator (Jakarta Bean Validation) |
| Documentación | SmallRye OpenAPI + Swagger UI |
| Seguridad | `edutrack-ms-commons` — cabeceras internas del Gateway |
| Tests | JUnit 5 + Mockito |
| Build | Maven Wrapper (`./mvnw`) |

---

## Prerrequisitos

- Java 21
- PostgreSQL accesible con schema `course` y usuario `course_user`
- `edutrack-ms-commons:1.0.0` instalado en el repositorio Maven local

### Instalar commons

```bash
# Desde la raíz del monorepo
cd ../commons
./mvnw install -DskipTests
```

---

## Configuración local

Variables de entorno con defaults para desarrollo:

| Variable | Default | Descripción |
|---|---|---|
| `DB_HOST` | `localhost` | Host PostgreSQL |
| `DB_PORT` | `5432` | Puerto PostgreSQL |
| `DB_NAME` | `edutrack` | Base de datos |
| `DB_USER` | `course_user` | Usuario del schema `course` |
| `DB_PASSWORD` | `course_pass` | Contraseña |

### Base de datos

```sql
CREATE DATABASE edutrack;
CREATE USER course_user WITH PASSWORD 'course_pass';
GRANT ALL PRIVILEGES ON DATABASE edutrack TO course_user;
```

Flyway crea el schema `course` y todas las tablas automáticamente al arrancar.

---

## Levantar el servicio

```bash
./mvnw quarkus:dev
```

El servicio queda disponible en `http://localhost:8080/course`.

Swagger UI: `http://localhost:8080/course/q/swagger-ui`  
Health check: `http://localhost:8080/course/q/health`

---

## Endpoints

Base URL: `/course`

### Cursos

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/courses` | `course.courses:READ` | Lista activos. Un docente solo ve los cursos asignados a él; SUPERUSER ve todos. |
| `POST` | `/courses` | `course.courses:WRITE` | Crear curso |
| `GET` | `/courses/{id}` | `course.courses:READ` | Detalle de un curso |
| `PUT` | `/courses/{id}` | `course.courses:WRITE` | Actualizar curso |
| `DELETE` | `/courses/{id}` | `course.courses:WRITE` | Borrado lógico |

### Asignaciones docente-curso

| Método | Ruta | Permiso | Descripción |
|---|---|---|---|
| `GET` | `/courses/{courseId}/teachers` | `course.assignments:READ` | Listar asignaciones del curso |
| `POST` | `/courses/{courseId}/teachers` | `course.assignments:WRITE` | Asignar docente con `accessLevel` |
| `PUT` | `/courses/{courseId}/teachers/{teacherId}` | `course.assignments:WRITE` | Actualizar nivel de acceso |
| `DELETE` | `/courses/{courseId}/teachers/{teacherId}` | `course.assignments:WRITE` | Revocar asignación |

### Verificación de acceso por instancia

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/access?courseId=&permission=` | Verifica si el usuario autenticado tiene el permiso sobre ese curso específico. Consumido por otros MS (ej: Assessment). |
| `GET` | `/meta/resources` | Catálogo de resource keys que este servicio protege |

---

## Autorización dual

Este servicio implementa dos niveles de autorización independientes:

**1. Por tipo de recurso (rol)** — `@RequirePermission` consulta a Auth Service (`GET /auth/access`) para verificar si el rol del usuario le da acceso al recurso en general.

**2. Por instancia (usuario↔curso)** — `CourseAccessService` consulta la BD local para verificar si el docente está asignado a ese curso y con qué nivel. Sin asignación → flags `0` → denegado.

El `accessLevel` en la asignación sigue los mismos flags Unix-style que Auth:

| AccessLevel | Flags | Significado |
|---|---|---|
| `READ` | 4 | El docente puede consultar el curso |
| `WRITE` | 6 | El docente puede consultar y modificar el curso (`WRITE` implica `READ`) |

---

## Esquema de base de datos

Schema: `course`

```
course.courses
├── id             UUID  PK
├── name           VARCHAR  NOT NULL
├── description    VARCHAR
├── level          VARCHAR  (ej: "1° Medio")
├── section        VARCHAR  (ej: "A")
├── academic_year  INT
├── status         VARCHAR  CHECK IN ('ACTIVE', 'DELETED')
├── deleted_at     TIMESTAMPTZ  nullable
├── created_at     TIMESTAMPTZ
├── updated_at     TIMESTAMPTZ
├── creator_user   UUID
└── updater_user   UUID

course.course_assignments
├── course_id     UUID  FK → course.courses  ┐ PK compuesta
├── teacher_id    UUID  (id de usuario en Auth)┘
├── access_level  VARCHAR  CHECK IN ('READ', 'WRITE')
└── assigned_at   TIMESTAMPTZ
```

---

## Estructura del proyecto

```
src/main/java/.../course/
├── model/entity/     ← Course (borrado lógico via status + deleted_at), CourseAssignment, CourseAssignmentId
├── model/dto/        ← CourseRequest, CourseResponse
├── resource/         ← CourseResource, CourseAssignmentResource, CourseAccessResource, ResourceCatalogResource
├── service/          ← CourseService (CRUD + filtrado por docente), CourseAccessService (permiso por instancia)
├── repository/       ← CourseRepository, CourseAssignmentRepository
└── security/         ← CourseResourceId (resource keys: course.courses, course.assignments)
```

---

## Pruebas

```bash
# Unitarios (JUnit 5 + Mockito, sin Quarkus ni DB)
./mvnw test

# Integración
./mvnw verify

# Un solo test
./mvnw test -Dtest=CourseAccessServiceTest
```

---

## Build y empaquetado

```bash
./mvnw clean package -DskipTests
java -jar target/quarkus-app/quarkus-run.jar
```

La imagen Docker JVM está en `src/main/docker/Dockerfile.jvm`.
