package cl.duocuc.edutrack.ms.course.model.dto;

import cl.duocuc.edutrack.ms.infrastructure.security.Permission;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.UUID;

/**
 * Resultado de verificar el acceso <b>por instancia</b> del usuario propagado
 * por el Gateway sobre un curso (BE-CRS-002). Es el análogo de {@code AccessResponse}
 * de Auth, pero la decisión la toma Course con su tabla de asignaciones en vez
 * del modelo de roles. Lo emite {@code CourseAccessResource} cuando el cliente
 * pide {@code application/json}; con {@code text/plain} la respuesta es {@code "1"}/{@code "0"}.
 *
 * @param allowed        ¿la asignación del docente satisface el permiso pedido?
 * @param courseId       curso consultado
 * @param required       permiso solicitado (READ / WRITE)
 * @param effectiveFlags flags Unix-style del nivel de acceso del docente (0 si no asignado)
 * @param effectiveLabel representación {@code rwx} de {@code effectiveFlags}
 */
@Schema(description = "Resultado de la verificacion de acceso por instancia (docente-curso).")
public record CourseAccessResponse(
    @Schema(description = "Si la asignacion satisface el permiso pedido") boolean allowed,
    @Schema(description = "Curso consultado") UUID courseId,
    @Schema(description = "Permiso solicitado", examples = "WRITE") String required,
    @Schema(description = "Flags efectivos del docente sobre el curso", examples = "6") short effectiveFlags,
    @Schema(description = "Etiqueta rwx de los flags efectivos", examples = "rw-") String effectiveLabel
) {

    public static CourseAccessResponse of(boolean allowed, UUID courseId,
                                          Permission required, short effectiveFlags) {
        return new CourseAccessResponse(
            allowed, courseId, required.name(), effectiveFlags, toLabel(effectiveFlags));
    }

    /** Etiqueta Unix {@code rwx} de un conjunto de flags. */
    public static String toLabel(short flags) {
        return new StringBuilder(3)
            .append((flags & 4) != 0 ? 'r' : '-')
            .append((flags & 2) != 0 ? 'w' : '-')
            .append((flags & 1) != 0 ? 'x' : '-')
            .toString();
    }
}
