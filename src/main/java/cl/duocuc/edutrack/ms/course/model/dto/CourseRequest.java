package cl.duocuc.edutrack.ms.course.model.dto;

import cl.duocuc.edutrack.ms.infrastructure.jackson.Views;
import cl.duocuc.edutrack.ms.infrastructure.validation.Validations;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Datos de creación/actualización de un curso. Único Request del recurso; la
 * granularidad de campos por endpoint se modela con {@code @JsonView} y la de
 * validaciones con validation groups (formato en {@code Default}, presencia en
 * {@link Validations.OnCreate}).
 */
@Schema(description = "Datos de creacion/actualizacion de un curso.")
public record CourseRequest(

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "Nombre del curso", examples = "Matematica 1 Medio A")
    @Size(max = 150)
    @NotBlank(groups = Validations.OnCreate.class) String name,

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "Descripcion del curso")
    @Size(max = 500) String description,

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "Nivel/grado", examples = "1 Medio")
    @Size(max = 50) String level,

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "Letra de la seccion", examples = "A")
    @Size(max = 10) String section,

    @JsonView({Views.Create.class, Views.Update.class})
    @Schema(description = "Anio academico", examples = "2026")
    @Min(1900) @Max(2200)
    @NotNull(groups = Validations.OnCreate.class) Integer academicYear
) {}
