# CLAUDE.md

Guía para Claude Code al trabajar en el **Course Service**. Hereda todas las
convenciones transversales del monorepo (`../CLAUDE.md`) y de la librería
compartida (`../commons/CLAUDE.md`); aquí solo lo específico del dominio.

## Responsabilidad del servicio

Course Service gestiona el **CRUD de cursos** (borrado lógico) y la **relación
docente↔curso con nivel de acceso** (lectura/escritura). Implementa `BE-CRS-001..003`.

Es el dueño del **permiso granular por usuario e instancia** que complementa el
modelo de roles de Auth: Auth concede el verbo sobre el *tipo* de recurso
(`course.courses`); Course decide la pertenencia sobre la *instancia* con su
propia regla (`WHERE teacher_id = :userId`) y el nivel de acceso de la
asignación.

- Path raíz: `/course` (= `ServiceIds.COURSE`, contrato de discovery del Gateway).
- Schema BD: `course` (Shared DB, credenciales exclusivas — ADR-001).

## Modelo de datos (`model/entity`)

| Entidad | Tabla | Superclase | Notas |
|---|---|---|---|
| `Course` | `course.courses` | `AuditableEntity` | Borrado lógico: `status` (`ACTIVE`/`DELETED`) + `deleted_at` vía `softDelete()` |
| `CourseAssignment` | `course.course_assignments` | `PanacheEntityBase` | PK compuesta `@EmbeddedId CourseAssignmentId(courseId, teacherId)`; `assigned_at` propio (patrón `auth.UserRole`) |
| `CourseAssignmentId` | — | — | `@Embeddable`; `teacherId` = `userId` de Auth, **sin FK cross-schema** |

Enums: `CourseStatus` (ACTIVE/DELETED) y `AccessLevel` (READ=`r--` flags 4, WRITE=`rw-` flags 6).

### `AccessLevel` reutiliza los bits Unix-style

`AccessLevel` mapea a los mismos flags que `Permission` (`r=4, w=2, x=1`) para
que `GET /course/access` aplique exactamente el mismo AND de bits que
`GET /auth/access` (`(flags & required) == required`). No se reimplementa la
aritmética: `WRITE` implica `READ`.

## Autorización: dos capas

1. **Tipo (rol)** — `@RequirePermission(resource = CourseResourceId.X, value = …)`.
   Lo evalúa el `RemotePermissionEvaluator` de `commons` (HTTP a `/auth/access`).
   Resource keys en `security/CourseResourceId`:
   - `course.courses` — CRUD de cursos (READ docente/admin; WRITE admin).
   - `course.assignments` — gestión de asignaciones (WRITE admin).
2. **Instancia (usuario↔curso)** — `CourseAccessService`. Fuente de verdad única
   del permiso granular: alimenta tanto el guard interno (`requireAccess` ⇒ `403`)
   como el endpoint público `GET /course/access` que otros MS (p. ej. Assessment,
   para "registrar notas") consumen. Sin asignación ⇒ flags `0` ⇒ denegado, nunca
   error (fail-closed lo decide el consumidor). Materializa BE-CRS-002
   ("solo lectura → 403 en escritura").

> **Nota de resource key:** la spec ilustra el tipo como `course.asignatura`; se
> usa `course.courses` por coherencia con la convención `<servicio>.<recurso>`
> (`auth.users`, `auth.roles`). El string es un contrato interno opaco; debe
> coincidir con los grants sembrados en Auth.

## Endpoints

- `GET/POST /course/courses`, `GET/PUT/DELETE /course/courses/{id}` — CRUD (borrado lógico).
  El `GET` de colección filtra por el docente autenticado (BE-CRS-003); un
  superusuario (`RequestContext.isSuper()`) ve todos los cursos activos.
- `GET/POST /course/courses/{courseId}/teachers`,
  `PUT/DELETE /course/courses/{courseId}/teachers/{teacherId}` — asignaciones.
- `GET /course/access?courseId&permission` — verificación por instancia (público
  tras el Gateway; `text/plain` `"1"/"0"` o `application/json`).

## Tests

Unitarios planos (JUnit5 + Mockito, sin arrancar Quarkus ni DB), igual que en
Auth: los repositorios y colaboradores se mockean e inyectan en los campos
package-private. Cubren los escenarios de `BE-CRS-001..003`.

```bash
./mvnw test                       # unitarios
./mvnw clean package              # build + tests
./mvnw test -Dtest=CourseAccessServiceTest
```

## Pendientes conocidos (fuera del alcance de la implementación inicial)

- Sembrar los grants de `course.courses`/`course.assignments` en la seed de Auth
  (`V2__seed.sql` del MS Auth) para los roles DOCENTE/ADMIN.
- Sumar el servicio a `docker-compose.yml` (contenedor `course`, usuario/credenciales
  del schema `course`) y `infra/` del Gateway.
- `CourseClient` en `commons/clients` cuando Assessment necesite consumir `/course/access`.
