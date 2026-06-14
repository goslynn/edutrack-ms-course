package cl.duocuc.edutrack.ms.course.model.entity;

import cl.duocuc.edutrack.ms.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/**
 * Curso del libro de clases. Entidad mutable con auditoría completa: hereda
 * {@code id}, {@code createdAt}/{@code creatorUser} y
 * {@code updatedAt}/{@code updaterUser} de {@link AuditableEntity}.
 *
 * <p>El borrado es lógico (BE-CRS-001): {@link #softDelete()} fija
 * {@link #status} en {@link CourseStatus#DELETED} y registra {@link #deletedAt},
 * preservando la fila y el historial académico que la referencia.</p>
 */
@Entity
@Table(name = "courses", schema = "course")
public class Course extends AuditableEntity {

    /** Nombre del curso, p. ej. "Matemática 1° Medio A". */
    @Column(nullable = false, length = 150)
    public String name;

    @Column(length = 500)
    public String description;

    /** Nivel/grado, p. ej. "1° Medio". */
    @Column(length = 50)
    public String level;

    /** Letra de la sección, p. ej. "A". */
    @Column(length = 10)
    public String section;

    /** Año académico, p. ej. 2026. */
    @Column(name = "academic_year", nullable = false)
    public int academicYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public CourseStatus status = CourseStatus.ACTIVE;

    @Column(name = "deleted_at")
    public Instant deletedAt;

    public boolean isDeleted() {
        return status == CourseStatus.DELETED;
    }

    /**
     * Borrado lógico: marca el curso como {@link CourseStatus#DELETED} y registra
     * el instante. Idempotente en la práctica (vuelve a fijar el mismo estado).
     */
    public void softDelete() {
        this.status = CourseStatus.DELETED;
        this.deletedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(id, course.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
