package cl.duocuc.edutrack.ms.course.service;

import cl.duocuc.edutrack.ms.course.model.entity.AccessLevel;
import cl.duocuc.edutrack.ms.course.model.entity.Course;
import cl.duocuc.edutrack.ms.course.model.entity.CourseAssignment;
import cl.duocuc.edutrack.ms.course.repository.CourseAssignmentRepository;
import cl.duocuc.edutrack.ms.infrastructure.exception.ConflictException;
import cl.duocuc.edutrack.ms.infrastructure.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de la gestión de asignaciones docente↔curso (BE-CRS-002). Atómicos: el
 * {@link CourseAssignmentRepository} y el {@link CourseService} colaborador están
 * mockeados.
 */
class CourseAssignmentServiceTest {

    private CourseAssignmentRepository assignmentRepository;
    private CourseService courseService;
    private CourseAssignmentService service;

    private final UUID courseId = UUID.randomUUID();
    private final UUID teacherId = UUID.randomUUID();
    private Course course;

    @BeforeEach
    void setUp() {
        assignmentRepository = mock(CourseAssignmentRepository.class);
        courseService = mock(CourseService.class);
        service = new CourseAssignmentService();
        service.assignmentRepository = assignmentRepository;
        service.courseService = courseService;

        course = new Course();
        course.id = courseId;
        when(courseService.findActiveById(courseId)).thenReturn(course);
    }

    @Test
    @DisplayName("assign crea la asignacion con el nivel de acceso indicado")
    void assign_createsAssignment() {
        when(assignmentRepository.existsByCourseAndTeacher(courseId, teacherId)).thenReturn(false);

        CourseAssignment result = service.assign(courseId, teacherId, AccessLevel.WRITE);

        assertEquals(AccessLevel.WRITE, result.accessLevel);
        assertEquals(courseId, result.id.courseId);
        assertEquals(teacherId, result.id.teacherId);
        verify(assignmentRepository, times(1)).persist(any(CourseAssignment.class));
    }

    @Test
    @DisplayName("assign de un docente ya asignado ⇒ 409 y no persiste")
    void assign_duplicate_conflict() {
        when(assignmentRepository.existsByCourseAndTeacher(courseId, teacherId)).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class,
            () -> service.assign(courseId, teacherId, AccessLevel.READ));
        assertEquals(409, ex.status());
        verify(assignmentRepository, never()).persist(any(CourseAssignment.class));
    }

    @Test
    @DisplayName("updateAccess de una asignacion inexistente ⇒ 404")
    void updateAccess_notFound() {
        when(assignmentRepository.findByCourseAndTeacher(courseId, teacherId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
            () -> service.updateAccess(courseId, teacherId, AccessLevel.WRITE));
        assertEquals(404, ex.status());
    }

    @Test
    @DisplayName("updateAccess cambia el nivel de acceso de una asignacion existente")
    void updateAccess_changesLevel() {
        CourseAssignment existing = new CourseAssignment(course, teacherId, AccessLevel.READ);
        when(assignmentRepository.findByCourseAndTeacher(courseId, teacherId)).thenReturn(Optional.of(existing));

        CourseAssignment updated = service.updateAccess(courseId, teacherId, AccessLevel.WRITE);

        assertEquals(AccessLevel.WRITE, updated.accessLevel);
    }

    @Test
    @DisplayName("revoke de una asignacion inexistente ⇒ 404")
    void revoke_notFound() {
        when(assignmentRepository.findByCourseAndTeacher(courseId, teacherId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
            () -> service.revoke(courseId, teacherId));
        assertEquals(404, ex.status());
    }

    @Test
    @DisplayName("accessLevelOf devuelve el nivel cuando existe asignacion, vacio si no")
    void accessLevelOf_mapsLevel() {
        CourseAssignment existing = new CourseAssignment(course, teacherId, AccessLevel.READ);
        when(assignmentRepository.findByCourseAndTeacher(courseId, teacherId))
            .thenReturn(Optional.of(existing));
        assertEquals(Optional.of(AccessLevel.READ), service.accessLevelOf(courseId, teacherId));

        UUID other = UUID.randomUUID();
        when(assignmentRepository.findByCourseAndTeacher(courseId, other)).thenReturn(Optional.empty());
        assertTrue(service.accessLevelOf(courseId, other).isEmpty());
    }
}
