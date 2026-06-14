package cl.duocuc.edutrack.ms.course.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Clave compuesta de {@link CourseAssignment}: el par {@code (courseId, teacherId)}.
 * Mismo patrón que {@code auth.UserRoleId}.
 *
 * <p>{@code teacherId} es el {@code userId} del docente tal como lo emite Auth.
 * No hay FK cross-schema hacia {@code auth.users}: cada MS tiene credenciales
 * exclusivas a su propio schema (ADR-001), por lo que la integridad
 * usuario↔asignación se mantiene a nivel de aplicación.</p>
 */
@Embeddable
public class CourseAssignmentId implements Serializable {

    @Column(name = "course_id", columnDefinition = "uuid")
    public UUID courseId;

    @Column(name = "teacher_id", columnDefinition = "uuid")
    public UUID teacherId;

    public CourseAssignmentId() {}

    public CourseAssignmentId(UUID courseId, UUID teacherId) {
        this.courseId = courseId;
        this.teacherId = teacherId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseAssignmentId that)) return false;
        return Objects.equals(courseId, that.courseId) && Objects.equals(teacherId, that.teacherId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, teacherId);
    }
}
