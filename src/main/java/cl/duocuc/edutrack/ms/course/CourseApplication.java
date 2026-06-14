package cl.duocuc.edutrack.ms.course;

import cl.duocuc.edutrack.ms.infrastructure.discovery.ServiceIds;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Punto de entrada JAX-RS y metadata global de OpenAPI del Course Service.
 *
 * <p>Las rutas viven bajo {@code /course} (primer segmento = nombre lógico del
 * servicio, contrato de discovery del Gateway, ADR-005). El Gateway valida el
 * JWT RS256 y propaga la identidad como cabeceras internas; los endpoints
 * protegidos aplican permisos sobre esos roles vía {@code @RequirePermission}.
 * El endpoint {@code /course/access} es público tras el Gateway.</p>
 */
@ApplicationPath("/" + ServiceIds.COURSE)
@OpenAPIDefinition(
    info = @Info(
        title = "EduTrack — Course Service API",
        version = "1.0.0",
        description = """
            Servicio de cursos de EduTrack (Colegio Bernardo O'Higgins). Gestiona \
            el CRUD de cursos (borrado logico) y la relacion docente-curso con \
            nivel de acceso (lectura/escritura): el permiso granular por usuario e \
            instancia que complementa el modelo de roles de Auth. El listado se \
            filtra por el docente autenticado.

            El API Gateway valida el JWT y propaga la identidad (`X-User-Id`, \
            `X-User-Roles`) como cabeceras internas. Las rutas viven bajo `/course`.""",
        contact = @Contact(name = "EduTrack Backend", email = "vct.gonzaleza@gmail.com")
    ),
    servers = {
        @Server(url = "/", description = "Acceso directo al microservicio (paths bajo /course)")
    },
    tags = {
        @Tag(name = "Courses", description = "CRUD de cursos (borrado logico)"),
        @Tag(name = "Course Assignments", description = "Asignacion docente-curso con nivel de acceso"),
        @Tag(name = "Access", description = "Verificacion de acceso por instancia para otros microservicios")
    },
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    securitySchemeName = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT RS256 emitido por Auth. El Gateway lo valida y propaga la identidad."
)
public class CourseApplication extends Application {
}
