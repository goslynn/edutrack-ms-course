package cl.duocuc.edutrack.ms.course.model.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Asignación docente↔curso con nivel de acceso (BE-CRS-002). Es la fuente de
 * verdad del permiso granular <em>por usuario e instancia</em>: la regla de
 * pertenencia que Course aporta sobre el modelo de roles de Auth
 * ({@code WHERE teacher_id = :userId}).
 *
 * <p>Mismo patrón que {@code auth.UserRole}: PK compuesta
 * {@link CourseAssignmentId} y {@code assigned_at} con semántica propia (no
 * hereda de las superclases de auditoría — la fila no se "audita", se asigna).</p>
 */
@Entity
@Table(name = "course_assignments", schema = "course")
public class CourseAssignment extends PanacheEntityBase {

    @EmbeddedId
    public CourseAssignmentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    public Course course;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false, length = 10)
    public AccessLevel accessLevel;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    public Instant assignedAt;

    public CourseAssignment() {}

    public CourseAssignment(Course course, UUID teacherId, AccessLevel accessLevel) {
        this.course = course;
        this.accessLevel = accessLevel;
        this.id = new CourseAssignmentId(course.id, teacherId);
    }

    @PrePersist
    void prePersist() {
        assignedAt = Instant.now();
    }
}
