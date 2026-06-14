package cl.duocuc.edutrack.ms.course.model.dto;

import cl.duocuc.edutrack.ms.course.model.entity.Course;
import cl.duocuc.edutrack.ms.infrastructure.jackson.Views;
import com.fasterxml.jackson.annotation.JsonView;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Representación de un curso. Único Response del recurso; los campos se muestran
 * según la vista activa del endpoint (lista vs detalle vs admin).
 */
@Schema(description = "Representacion de un curso.")
public record CourseResponse(

    @Schema(description = "UUID del curso") UUID id,
    @Schema(description = "Nombre del curso") String name,
    @Schema(description = "Anio academico", examples = "2026") Integer academicYear,

    @JsonView({Views.Base.class, Views.Extra.class})
    @Schema(description = "Descripcion") String description,

    @JsonView({Views.Base.class, Views.Extra.class})
    @Schema(description = "Nivel/grado") String level,

    @JsonView({Views.Base.class, Views.Extra.class})
    @Schema(description = "Letra de la seccion") String section,

    @JsonView({Views.List.class, Views.Detailed.class, Views.Admin.class})
    @Schema(description = "Estado del curso", examples = "ACTIVE") String status,

    @JsonView({Views.Detailed.class, Views.Admin.class}) Instant createdAt,
    @JsonView({Views.Detailed.class, Views.Admin.class}) Instant updatedAt
) {

    /** Factory canónico del contrato "*Response sabe construirse desde su entidad". */
    public static CourseResponse fromEntity(Course c) {
        return new CourseResponse(
            c.id, c.name, c.academicYear,
            c.description, c.level, c.section,
            c.status != null ? c.status.name() : null,
            c.createdAt, c.updatedAt);
    }
}
