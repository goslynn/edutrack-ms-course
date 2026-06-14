package cl.duocuc.edutrack.ms.course.security;

/**
 * Catálogo de <em>resource keys</em> que el Course Service protege con permisos
 * Unix-style (modelo descentralizado: cada MS define las suyas en código). Una
 * resource key es un identificador estable y legible ({@code "<servicio>.<recurso>"}),
 * opaca para Auth y comparada por igualdad; el mismo string nombra el recurso en
 * ambos lados de un grant (ADR-003).
 *
 * <p>Estas claves son el permiso a nivel de <b>tipo</b> que Auth concede por rol
 * (p. ej. DOCENTE tiene {@code r--} sobre {@code course.courses}; ADMIN tiene
 * {@code rw-}). El permiso granular <b>por instancia</b> (qué docente accede a
 * qué curso, y con qué nivel) lo decide Course con su propia tabla
 * {@code course_assignments} — ver {@code CourseAccessService}. Deben coincidir
 * con los grants sembrados en Auth.</p>
 */
public interface CourseResourceId {

    /** CRUD de cursos. READ: listar/ver; WRITE: crear/editar/borrar (típicamente ADMIN). */
    String COURSES = "course.courses";

    /** Gestión de asignaciones docente-curso. WRITE: asignar/revocar (típicamente ADMIN). */
    String ASSIGNMENTS = "course.assignments";
}
