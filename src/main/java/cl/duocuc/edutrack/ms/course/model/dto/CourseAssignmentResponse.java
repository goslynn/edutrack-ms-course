package cl.duocuc.edutrack.ms.course.model.dto;

import cl.duocuc.edutrack.ms.course.model.entity.CourseAssignment;
import cl.duocuc.edutrack.ms.infrastructure.jackson.Views;
import com.fasterxml.jackson.annotation.JsonView;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Representación de una asignación docente↔curso.
 */
@Schema(description = "Asignacion docente-curso con su nivel de acceso.")
public record CourseAssignmentResponse(

    @Schema(description = "UUID del curso") UUID courseId,
    @Schema(description = "UUID del docente") UUID teacherId,
    @Schema(description = "Nivel de acceso", examples = "WRITE") String accessLevel,

    @JsonView({Views.Detailed.class, Views.Admin.class}) Instant assignedAt
) {

    public static CourseAssignmentResponse fromEntity(CourseAssignment a) {
        return new CourseAssignmentResponse(
            a.id.courseId,
            a.id.teacherId,
            a.accessLevel != null ? a.accessLevel.name() : null,
            a.assignedAt);
    }
}
