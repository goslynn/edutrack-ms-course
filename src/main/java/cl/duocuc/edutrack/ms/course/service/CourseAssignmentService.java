package cl.duocuc.edutrack.ms.course.service;

import cl.duocuc.edutrack.ms.course.model.entity.AccessLevel;
import cl.duocuc.edutrack.ms.course.model.entity.Course;
import cl.duocuc.edutrack.ms.course.model.entity.CourseAssignment;
import cl.duocuc.edutrack.ms.course.repository.CourseAssignmentRepository;
import cl.duocuc.edutrack.ms.infrastructure.exception.ConflictException;
import cl.duocuc.edutrack.ms.infrastructure.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Gestión de la relación docente↔curso con nivel de acceso (BE-CRS-002). Es la
 * fuente de verdad del permiso granular por usuario e instancia que complementa
 * el modelo de roles de Auth.
 */
@ApplicationScoped
public class CourseAssignmentService {

    @Inject
    CourseAssignmentRepository assignmentRepository;

    @Inject
    CourseService courseService;

    /** Asigna un docente a un curso. {@code 404} si el curso no existe; {@code 409} si ya estaba asignado. */
    @Transactional
    public CourseAssignment assign(UUID courseId, UUID teacherId, AccessLevel accessLevel) {
        Course course = courseService.findActiveById(courseId);
        if (assignmentRepository.existsByCourseAndTeacher(courseId, teacherId)) {
            throw new ConflictException("COURSE.ASSIGNMENT.ALREADY_EXISTS",
                "Teacher is already assigned to this course")
                .with("courseId", courseId).with("teacherId", teacherId);
        }
        CourseAssignment assignment = new CourseAssignment(course, teacherId, accessLevel);
        assignmentRepository.persist(assignment);
        return assignment;
    }

    /** Actualiza el nivel de acceso de una asignación existente. {@code 404} si no existe. */
    @Transactional
    public CourseAssignment updateAccess(UUID courseId, UUID teacherId, AccessLevel accessLevel) {
        CourseAssignment assignment = requireAssignment(courseId, teacherId);
        assignment.accessLevel = accessLevel;
        return assignment;
    }

    /** Revoca la asignación. {@code 404} si no existe. */
    @Transactional
    public void revoke(UUID courseId, UUID teacherId) {
        CourseAssignment assignment = requireAssignment(courseId, teacherId);
        assignmentRepository.delete(assignment);
    }

    /** Asignaciones de un curso. {@code 404} si el curso no existe. */
    public List<CourseAssignment> listByCourse(UUID courseId) {
        courseService.findActiveById(courseId);
        return assignmentRepository.listByCourse(courseId);
    }

    /** Nivel de acceso del docente sobre el curso, o vacío si no tiene asignación. */
    public Optional<AccessLevel> accessLevelOf(UUID courseId, UUID teacherId) {
        return assignmentRepository.findByCourseAndTeacher(courseId, teacherId)
            .map(a -> a.accessLevel);
    }

    private CourseAssignment requireAssignment(UUID courseId, UUID teacherId) {
        return assignmentRepository.findByCourseAndTeacher(courseId, teacherId)
            .orElseThrow(() -> new NotFoundException("COURSE.ASSIGNMENT.NOT_FOUND", "Assignment not found")
                .with("courseId", courseId).with("teacherId", teacherId));
    }
}
