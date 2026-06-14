package cl.duocuc.edutrack.ms.course.service;

import cl.duocuc.edutrack.ms.course.model.entity.AccessLevel;
import cl.duocuc.edutrack.ms.infrastructure.exception.ForbiddenException;
import cl.duocuc.edutrack.ms.infrastructure.security.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests del permiso granular por instancia (BE-CRS-002). Atómicos: el
 * {@link CourseAssignmentService} colaborador está mockeado, de modo que se
 * prueba exactamente la decisión de acceso (la aritmética de bits y el guard
 * 403) sin tocar persistencia.
 *
 * <p>Escenario clave del spec: "un docente con acceso solo-lectura a un curso
 * intenta registrar notas en él ⇒ 403" — aquí materializado como
 * {@code requireAccess(WRITE)} sobre un docente con {@link AccessLevel#READ}.</p>
 */
class CourseAccessServiceTest {

    private CourseAssignmentService assignmentService;
    private CourseAccessService service;

    private final UUID courseId = UUID.randomUUID();
    private final UUID teacherId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        assignmentService = mock(CourseAssignmentService.class);
        service = new CourseAccessService();
        service.assignmentService = assignmentService;
    }

    @Test
    @DisplayName("BE-CRS-002 — docente solo-lectura: requireAccess(WRITE) ⇒ 403")
    void readOnlyTeacher_writeDenied() {
        when(assignmentService.accessLevelOf(courseId, teacherId)).thenReturn(Optional.of(AccessLevel.READ));

        assertEquals(4, service.effectiveFlags(courseId, teacherId));
        assertTrue(service.hasAccess(courseId, teacherId, Permission.READ));
        assertFalse(service.hasAccess(courseId, teacherId, Permission.WRITE));

        ForbiddenException ex = assertThrows(ForbiddenException.class,
            () -> service.requireAccess(courseId, teacherId, Permission.WRITE));
        assertEquals(403, ex.status());
        // La lectura sigue permitida para el docente solo-lectura.
        assertDoesNotThrow(() -> service.requireAccess(courseId, teacherId, Permission.READ));
    }

    @Test
    @DisplayName("docente con escritura: requireAccess(WRITE) pasa")
    void writeTeacher_writeAllowed() {
        when(assignmentService.accessLevelOf(courseId, teacherId)).thenReturn(Optional.of(AccessLevel.WRITE));

        assertEquals(6, service.effectiveFlags(courseId, teacherId));
        assertTrue(service.hasAccess(courseId, teacherId, Permission.WRITE));
        assertDoesNotThrow(() -> service.requireAccess(courseId, teacherId, Permission.WRITE));
    }

    @Test
    @DisplayName("sin asignacion: flags 0 ⇒ todo denegado")
    void noAssignment_denied() {
        when(assignmentService.accessLevelOf(courseId, teacherId)).thenReturn(Optional.empty());

        assertEquals(0, service.effectiveFlags(courseId, teacherId));
        assertFalse(service.hasAccess(courseId, teacherId, Permission.READ));
        assertThrows(ForbiddenException.class,
            () -> service.requireAccess(courseId, teacherId, Permission.READ));
    }
}
