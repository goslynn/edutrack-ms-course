package cl.duocuc.edutrack.ms.course.resource;

import cl.duocuc.edutrack.ms.course.model.dto.CourseAccessResponse;
import cl.duocuc.edutrack.ms.course.service.CourseAccessService;
import cl.duocuc.edutrack.ms.infrastructure.context.RequestContext;
import cl.duocuc.edutrack.ms.infrastructure.security.Permission;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirements;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

/**
 * Verificación de acceso <b>por instancia</b> docente↔curso para consumo de
 * otros microservicios (BE-CRS-002). Es el análogo de {@code GET /auth/access}:
 * Auth decide el verbo sobre el <em>tipo</em>; Course decide la pertenencia
 * sobre la <em>instancia</em>. Lo consume, p. ej., Assessment antes de permitir
 * que un docente registre notas en un curso.
 *
 * <p>Endpoint público tras el Gateway: <strong>no</strong> lleva
 * {@code @RequirePermission}. Sin identidad propagada (o sin asignación) los
 * flags efectivos son {@code 0} ⇒ {@code "0"} / {@code allowed=false}, no un
 * {@code 403} — el consumidor traduce eso a su propia política (fail-closed).</p>
 */
@Path("/access")
@Tag(name = "Access")
@SecurityRequirements
public class CourseAccessResource {

    @Inject
    RequestContext requestContext;

    @Inject
    CourseAccessService accessService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Verificar acceso a curso (texto plano)",
        description = "Variante ligera: \"1\" si el docente propagado tiene el permiso sobre el curso, \"0\" si no.")
    @APIResponse(responseCode = "200", description = "\"1\" (permitido) o \"0\" (denegado)",
        content = @Content(schema = @Schema(type = SchemaType.STRING, examples = "1")))
    @APIResponse(responseCode = "400", description = "courseId ausente")
    public String checkPlain(
        @Parameter(description = "UUID del curso")
        @NotNull @QueryParam("courseId") UUID courseId,
        @Parameter(description = "Permiso requerido")
        @DefaultValue("READ") @QueryParam("permission") Permission permission
    ) {
        return accessService.hasAccess(courseId, teacherId(), permission) ? "1" : "0";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Verificar acceso a curso (JSON)",
        description = "Variante detallada: allowed + flags efectivos del docente propagado sobre el curso.")
    @APIResponse(responseCode = "200", description = "Resultado de la verificacion",
        content = @Content(schema = @Schema(implementation = CourseAccessResponse.class)))
    @APIResponse(responseCode = "400", description = "courseId ausente")
    public CourseAccessResponse checkJson(
        @Parameter(description = "UUID del curso")
        @NotNull @QueryParam("courseId") UUID courseId,
        @Parameter(description = "Permiso requerido")
        @DefaultValue("READ") @QueryParam("permission") Permission permission
    ) {
        short effective = accessService.effectiveFlags(courseId, teacherId());
        return CourseAccessResponse.of(
            (effective & permission.bit) == permission.bit, courseId, permission, effective);
    }

    /** El docente es la identidad propagada por el Gateway; sin identidad ⇒ sentinela nulo ⇒ sin asignación. */
    private UUID teacherId() {
        return requestContext.headers().userId().orElse(null);
    }
}
