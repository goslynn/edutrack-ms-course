package cl.duocuc.edutrack.ms.course.repository;

import cl.duocuc.edutrack.ms.course.model.entity.Course;
import cl.duocuc.edutrack.ms.course.model.entity.CourseStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CourseRepository implements PanacheRepositoryBase<Course, UUID> {

    /** Todos los cursos activos (BE-CRS-001: los borrados no aparecen). */
    public List<Course> listActive() {
        return list("status", CourseStatus.ACTIVE);
    }

    /** Curso activo por id; vacío si no existe o está borrado lógicamente. */
    public Optional<Course> findActiveById(UUID id) {
        return find("id = ?1 and status = ?2", id, CourseStatus.ACTIVE).firstResultOptional();
    }

    /**
     * Cursos activos a los que el docente tiene una asignación explícita
     * (BE-CRS-003): el listado del docente no incluye cursos ajenos. Join contra
     * {@code CourseAssignment} por la regla de pertenencia {@code teacher_id = :userId}.
     */
    public List<Course> listActiveForTeacher(UUID teacherId) {
        return find("select c from Course c, CourseAssignment a "
                + "where a.course = c and a.id.teacherId = ?1 and c.status = ?2",
                teacherId, CourseStatus.ACTIVE).list();
    }
}
