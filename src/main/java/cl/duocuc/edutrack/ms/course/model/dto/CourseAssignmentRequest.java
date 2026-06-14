package cl.duocuc.edutrack.ms.course.model.dto;

import cl.duocuc.edutrack.ms.course.model.entity.AccessLevel;
import cl.duocuc.edutrack.ms.infrastructure.jackson.Views;
import cl.duocuc.edutrack.ms.infrastructure.validation.Validations;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.UUID;

/**
 * Datos para asignar/actualizar un docente en un curso (BE-CRS-002).
 *
 * <ul>
 *   <li>{@code teacherId} solo viaja en el {@code POST} de creación (en el
 *       {@code PUT} llega por path-param): presencia obligatoria en
 *       {@link Validations.OnCreate}.</li>
 *   <li>{@code accessLevel} es obligatorio en ambos endpoints, por eso su
 *       {@code @NotNull} va en {@code Default}.</li>
 * </ul>
 */
@Schema(description = "Asignacion de un docente a un curso con su nivel de acceso.")
public record CourseAssignmentRequest(

    @JsonView(Views.Create.class)
    @Schema(description = "UUID del docente (userId en Auth)")
    @NotNull(groups = Validations.OnCreate.class) UUID teacherId,

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "Nivel de acceso del docente sobre el curso", examples = "WRITE")
    @NotNull AccessLevel accessLevel
) {}
