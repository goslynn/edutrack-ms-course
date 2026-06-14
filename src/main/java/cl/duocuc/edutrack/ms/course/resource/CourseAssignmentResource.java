package cl.duocuc.edutrack.ms.course.resource;

import cl.duocuc.edutrack.ms.course.model.dto.CourseAssignmentRequest;
import cl.duocuc.edutrack.ms.course.model.dto.CourseAssignmentResponse;
import cl.duocuc.edutrack.ms.course.security.CourseResourceId;
import cl.duocuc.edutrack.ms.course.service.CourseAssignmentService;
import cl.duocuc.edutrack.ms.infrastructure.jackson.Views;
import cl.duocuc.edutrack.ms.infrastructure.security.Permission;
import cl.duocuc.edutrack.ms.infrastructure.security.RequirePermission;
import cl.duocuc.edutrack.ms.infrastructure.validation.Validations;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import jakarta.validation.groups.Default;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

/**
 * Gestión de asignaciones docente↔curso (BE-CRS-002). Anidado bajo el curso:
 * {@code /courses/{courseId}/teachers}.
 */
@Path("/courses/{courseId}/teachers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Course Assignments")
public class CourseAssignmentResource {

    @Inject
    CourseAssignmentService assignmentService;

    @GET
    @JsonView(Views.List.class)
    @RequirePermission(resource = CourseResourceId.ASSIGNMENTS, value = Permission.READ)
    @Operation(summary = "Listar asignaciones del curso", description = "Requiere READ sobre course.assignments.")
    @APIResponse(responseCode = "200", description = "Asignaciones del curso",
        content = @Content(schema = @Schema(implementation = CourseAssignmentResponse.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    @APIResponse(responseCode = "404", description = "Curso no encontrado")
    public List<CourseAssignmentResponse> list(
        @Parameter(description = "UUID del curso") @PathParam("courseId") UUID courseId
    ) {
        return assignmentService.listByCourse(courseId).stream()
            .map(CourseAssignmentResponse::fromEntity).toList();
    }

    @POST
    @JsonView(Views.Detailed.class)
    @RequirePermission(resource = CourseResourceId.ASSIGNMENTS, value = Permission.WRITE)
    @Operation(summary = "Asignar docente a curso", description = "Requiere WRITE sobre course.assignments.")
    @APIResponse(responseCode = "201", description = "Docente asignado",
        content = @Content(schema = @Schema(implementation = CourseAssignmentResponse.class)))
    @APIResponse(responseCode = "400", description = "Body invalido")
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    @APIResponse(responseCode = "404", description = "Curso no encontrado")
    @APIResponse(responseCode = "409", description = "El docente ya esta asignado al curso")
    public Response assign(
        @Parameter(description = "UUID del curso") @PathParam("courseId") UUID courseId,
        @Valid @ConvertGroup(from = Default.class, to = Validations.Create.class)
        @JsonView(Views.Create.class) CourseAssignmentRequest req
    ) {
        return Response.status(Response.Status.CREATED)
            .entity(CourseAssignmentResponse.fromEntity(
                assignmentService.assign(courseId, req.teacherId(), req.accessLevel())))
            .build();
    }

    @PUT
    @Path("/{teacherId}")
    @JsonView(Views.Detailed.class)
    @RequirePermission(resource = CourseResourceId.ASSIGNMENTS, value = Permission.WRITE)
    @Operation(summary = "Actualizar nivel de acceso", description = "Requiere WRITE sobre course.assignments.")
    @APIResponse(responseCode = "200", description = "Asignacion actualizada",
        content = @Content(schema = @Schema(implementation = CourseAssignmentResponse.class)))
    @APIResponse(responseCode = "400", description = "Body invalido")
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    @APIResponse(responseCode = "404", description = "Asignacion no encontrada")
    public CourseAssignmentResponse updateAccess(
        @Parameter(description = "UUID del curso") @PathParam("courseId") UUID courseId,
        @Parameter(description = "UUID del docente") @PathParam("teacherId") UUID teacherId,
        @Valid @JsonView(Views.Update.class) CourseAssignmentRequest req
    ) {
        return CourseAssignmentResponse.fromEntity(
            assignmentService.updateAccess(courseId, teacherId, req.accessLevel()));
    }

    @DELETE
    @Path("/{teacherId}")
    @RequirePermission(resource = CourseResourceId.ASSIGNMENTS, value = Permission.WRITE)
    @Operation(summary = "Revocar asignacion", description = "Requiere WRITE sobre course.assignments.")
    @APIResponse(responseCode = "204", description = "Asignacion revocada")
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    @APIResponse(responseCode = "404", description = "Asignacion no encontrada")
    public Response revoke(
        @Parameter(description = "UUID del curso") @PathParam("courseId") UUID courseId,
        @Parameter(description = "UUID del docente") @PathParam("teacherId") UUID teacherId
    ) {
        assignmentService.revoke(courseId, teacherId);
        return Response.noContent().build();
    }
}
