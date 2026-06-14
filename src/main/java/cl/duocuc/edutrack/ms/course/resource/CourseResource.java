package cl.duocuc.edutrack.ms.course.resource;

import cl.duocuc.edutrack.ms.course.model.dto.CourseRequest;
import cl.duocuc.edutrack.ms.course.model.dto.CourseResponse;
import cl.duocuc.edutrack.ms.course.model.entity.Course;
import cl.duocuc.edutrack.ms.course.security.CourseResourceId;
import cl.duocuc.edutrack.ms.course.service.CourseService;
import cl.duocuc.edutrack.ms.infrastructure.context.RequestContext;
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

@Path("/courses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Courses")
public class CourseResource {

    @Inject
    CourseService courseService;

    @Inject
    RequestContext requestContext;

    @GET
    @JsonView(Views.List.class)
    @RequirePermission(resource = CourseResourceId.COURSES, value = Permission.READ)
    @Operation(summary = "Listar cursos",
        description = "Requiere READ sobre course.courses. El docente solo ve los cursos a los que "
            + "tiene asignacion (BE-CRS-003); un superusuario ve todos los cursos activos.")
    @APIResponse(responseCode = "200", description = "Listado de cursos",
        content = @Content(schema = @Schema(implementation = CourseResponse.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    public List<CourseResponse> list() {
        List<Course> courses = requestContext.isSuper()
            ? courseService.listActive()
            : courseService.listForTeacher(requestContext.headers().requireUserId());
        return courses.stream().map(CourseResponse::fromEntity).toList();
    }

    @POST
    @JsonView(Views.Detailed.class)
    @RequirePermission(resource = CourseResourceId.COURSES, value = Permission.WRITE)
    @Operation(summary = "Crear curso", description = "Requiere WRITE sobre course.courses.")
    @APIResponse(responseCode = "201", description = "Curso creado",
        content = @Content(schema = @Schema(implementation = CourseResponse.class)))
    @APIResponse(responseCode = "400", description = "Body invalido")
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    public Response create(
        @Valid @ConvertGroup(from = Default.class, to = Validations.Create.class)
        @JsonView(Views.Create.class) CourseRequest req
    ) {
        return Response.status(Response.Status.CREATED)
            .entity(CourseResponse.fromEntity(courseService.create(req)))
            .build();
    }

    @GET
    @Path("/{id}")
    @JsonView(Views.Detailed.class)
    @RequirePermission(resource = CourseResourceId.COURSES, value = Permission.READ)
    @Operation(summary = "Obtener curso", description = "Requiere READ sobre course.courses.")
    @APIResponse(responseCode = "200", description = "Curso encontrado",
        content = @Content(schema = @Schema(implementation = CourseResponse.class)))
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    @APIResponse(responseCode = "404", description = "Curso no encontrado")
    public CourseResponse get(@Parameter(description = "UUID del curso") @PathParam("id") UUID id) {
        return CourseResponse.fromEntity(courseService.findActiveById(id));
    }

    @PUT
    @Path("/{id}")
    @JsonView(Views.Detailed.class)
    @RequirePermission(resource = CourseResourceId.COURSES, value = Permission.WRITE)
    @Operation(summary = "Actualizar curso", description = "Requiere WRITE sobre course.courses.")
    @APIResponse(responseCode = "200", description = "Curso actualizado",
        content = @Content(schema = @Schema(implementation = CourseResponse.class)))
    @APIResponse(responseCode = "400", description = "Body invalido")
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    @APIResponse(responseCode = "404", description = "Curso no encontrado")
    public CourseResponse update(
        @Parameter(description = "UUID del curso") @PathParam("id") UUID id,
        @Valid @ConvertGroup(from = Default.class, to = Validations.Create.class)
        @JsonView(Views.Update.class) CourseRequest req
    ) {
        return CourseResponse.fromEntity(courseService.update(id, req));
    }

    @DELETE
    @Path("/{id}")
    @RequirePermission(resource = CourseResourceId.COURSES, value = Permission.WRITE)
    @Operation(summary = "Eliminar curso (logico)",
        description = "Requiere WRITE sobre course.courses. Borrado logico: el curso deja de aparecer "
            + "en el listado activo pero su historial se preserva (BE-CRS-001).")
    @APIResponse(responseCode = "204", description = "Curso eliminado")
    @APIResponse(responseCode = "403", description = "Permisos insuficientes")
    @APIResponse(responseCode = "404", description = "Curso no encontrado")
    public Response delete(@Parameter(description = "UUID del curso") @PathParam("id") UUID id) {
        courseService.softDelete(id);
        return Response.noContent().build();
    }
}
