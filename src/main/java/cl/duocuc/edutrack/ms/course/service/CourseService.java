package cl.duocuc.edutrack.ms.course.service;

import cl.duocuc.edutrack.ms.course.model.dto.CourseRequest;
import cl.duocuc.edutrack.ms.course.model.entity.Course;
import cl.duocuc.edutrack.ms.course.repository.CourseRepository;
import cl.duocuc.edutrack.ms.infrastructure.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Lógica de negocio del CRUD de cursos (BE-CRS-001) y del listado filtrado por
 * docente (BE-CRS-003). El borrado es lógico: ningún método expone borrado
 * físico de cursos.
 */
@ApplicationScoped
public class CourseService {

    @Inject
    CourseRepository courseRepository;

    @Transactional
    public Course create(CourseRequest req) {
        Course course = new Course();
        apply(course, req);
        courseRepository.persist(course);
        return course;
    }

    /** Curso activo por id; lanza {@code 404} si no existe o está borrado. */
    public Course findActiveById(UUID id) {
        return courseRepository.findActiveById(id)
            .orElseThrow(() -> new NotFoundException("COURSE.COURSE.NOT_FOUND", "Course not found")
                .with("courseId", id));
    }

    /** Todos los cursos activos (vista de administración). */
    public List<Course> listActive() {
        return courseRepository.listActive();
    }

    /** Cursos activos asignados al docente (BE-CRS-003: no ve cursos ajenos). */
    public List<Course> listForTeacher(UUID teacherId) {
        return courseRepository.listActiveForTeacher(teacherId);
    }

    @Transactional
    public Course update(UUID id, CourseRequest req) {
        Course course = findActiveById(id);
        apply(course, req);
        return course;
    }

    @Transactional
    public void softDelete(UUID id) {
        Course course = findActiveById(id);
        course.softDelete();
    }

    private void apply(Course course, CourseRequest req) {
        course.name = req.name();
        course.description = req.description();
        course.level = req.level();
        course.section = req.section();
        course.academicYear = req.academicYear();
    }
}
