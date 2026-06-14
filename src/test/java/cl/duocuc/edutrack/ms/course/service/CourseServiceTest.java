package cl.duocuc.edutrack.ms.course.service;

import cl.duocuc.edutrack.ms.course.model.dto.CourseRequest;
import cl.duocuc.edutrack.ms.course.model.entity.Course;
import cl.duocuc.edutrack.ms.course.model.entity.CourseStatus;
import cl.duocuc.edutrack.ms.course.repository.CourseRepository;
import cl.duocuc.edutrack.ms.infrastructure.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests del CRUD de cursos (BE-CRS-001) y del listado filtrado por docente
 * (BE-CRS-003). Atómicos: el {@link CourseRepository} está mockeado, de modo que
 * se prueba exactamente la lógica del servicio (borrado lógico, 404, delegación
 * del filtro) sin tocar persistencia.
 */
class CourseServiceTest {

    private CourseRepository courseRepository;
    private CourseService service;

    @BeforeEach
    void setUp() {
        courseRepository = mock(CourseRepository.class);
        service = new CourseService();
        service.courseRepository = courseRepository;
    }

    private CourseRequest req() {
        return new CourseRequest("Matematica 1 Medio A", "desc", "1 Medio", "A", 2026);
    }

    @Test
    @DisplayName("create persiste un curso en estado ACTIVE con los datos del request")
    void create_persistsActiveCourse() {
        Course created = service.create(req());

        assertEquals("Matematica 1 Medio A", created.name);
        assertEquals(2026, created.academicYear);
        assertEquals(CourseStatus.ACTIVE, created.status);
        verify(courseRepository, times(1)).persist(any(Course.class));
    }

    @Test
    @DisplayName("findActiveById de un curso inexistente ⇒ 404")
    void findActiveById_notFound() {
        UUID id = UUID.randomUUID();
        when(courseRepository.findActiveById(id)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.findActiveById(id));
        assertEquals(404, ex.status());
    }

    @Test
    @DisplayName("BE-CRS-001 — el borrado es logico: status DELETED + deleted_at, fila preservada")
    void softDelete_marksDeleted() {
        UUID id = UUID.randomUUID();
        Course course = new Course();
        course.id = id;
        course.status = CourseStatus.ACTIVE;
        when(courseRepository.findActiveById(id)).thenReturn(Optional.of(course));

        service.softDelete(id);

        assertEquals(CourseStatus.DELETED, course.status);
        assertNotNull(course.deletedAt, "deleted_at debe registrarse en el borrado logico");
    }

    @Test
    @DisplayName("update aplica los nuevos datos sobre el curso activo")
    void update_appliesNewData() {
        UUID id = UUID.randomUUID();
        Course course = new Course();
        course.id = id;
        course.name = "viejo";
        course.status = CourseStatus.ACTIVE;
        when(courseRepository.findActiveById(id)).thenReturn(Optional.of(course));

        Course updated = service.update(id, new CourseRequest("nuevo", null, null, null, 2027));

        assertEquals("nuevo", updated.name);
        assertEquals(2027, updated.academicYear);
    }

    @Test
    @DisplayName("BE-CRS-003 — listForTeacher delega en la consulta filtrada por docente")
    void listForTeacher_delegatesToFilteredQuery() {
        UUID teacherId = UUID.randomUUID();
        List<Course> expected = List.of(new Course());
        when(courseRepository.listActiveForTeacher(teacherId)).thenReturn(expected);

        assertSame(expected, service.listForTeacher(teacherId));
        verify(courseRepository, times(1)).listActiveForTeacher(teacherId);
    }
}
