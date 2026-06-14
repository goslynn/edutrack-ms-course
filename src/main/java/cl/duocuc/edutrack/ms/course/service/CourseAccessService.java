package cl.duocuc.edutrack.ms.course.service;

import cl.duocuc.edutrack.ms.course.model.entity.AccessLevel;
import cl.duocuc.edutrack.ms.infrastructure.exception.ForbiddenException;
import cl.duocuc.edutrack.ms.infrastructure.security.Permission;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

/**
 * Decisión de acceso <b>por instancia</b> docente↔curso (BE-CRS-002): aplica la
 * regla de pertenencia de Course ({@code WHERE teacher_id = :userId}) y la
 * traduce a la misma aritmética de bits Unix-style del resto de la plataforma.
 *
 * <p>Es la fuente de verdad única que alimenta tanto el guard interno
 * ({@link #requireAccess}) como el endpoint público {@code GET /course/access}
 * que otros MS (p. ej. Assessment, para "registrar notas") consumen: la lógica
 * de bits no se duplica. Sin asignación ⇒ flags {@code 0} ⇒ denegado, nunca un
 * error: el consumidor decide si eso es {@code 403}.</p>
 */
@ApplicationScoped
public class CourseAccessService {

    @Inject
    CourseAssignmentService assignmentService;

    /** Flags efectivos del docente sobre el curso ({@code 0} si no tiene asignación). */
    public short effectiveFlags(UUID courseId, UUID teacherId) {
        return assignmentService.accessLevelOf(courseId, teacherId)
            .map(level -> level.flags)
            .orElse((short) 0);
    }

    /** ¿La asignación del docente satisface el permiso requerido sobre el curso? */
    public boolean hasAccess(UUID courseId, UUID teacherId, Permission required) {
        return (effectiveFlags(courseId, teacherId) & required.bit) == required.bit;
    }

    /**
     * Guard de instancia: lanza {@code 403} si el docente no tiene el permiso
     * requerido sobre el curso. Materializa el escenario "solo lectura → 403 en
     * escritura" (BE-CRS-002): un docente con {@link AccessLevel#READ} que
     * intente una operación de escritura no lo satisface.
     */
    public void requireAccess(UUID courseId, UUID teacherId, Permission required) {
        if (!hasAccess(courseId, teacherId, required)) {
            throw new ForbiddenException("COURSE.ASSIGNMENT.ACCESS_DENIED",
                "Teacher lacks " + required.name() + " access on this course")
                .with("courseId", courseId)
                .with("teacherId", teacherId)
                .with("required", required.name());
        }
    }
}
