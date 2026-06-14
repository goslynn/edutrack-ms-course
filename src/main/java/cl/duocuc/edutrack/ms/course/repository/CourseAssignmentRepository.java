package cl.duocuc.edutrack.ms.course.repository;

import cl.duocuc.edutrack.ms.course.model.entity.CourseAssignment;
import cl.duocuc.edutrack.ms.course.model.entity.CourseAssignmentId;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CourseAssignmentRepository implements PanacheRepositoryBase<CourseAssignment, CourseAssignmentId> {

    public Optional<CourseAssignment> findByCourseAndTeacher(UUID courseId, UUID teacherId) {
        return find("id.courseId = ?1 and id.teacherId = ?2", courseId, teacherId).firstResultOptional();
    }

    public List<CourseAssignment> listByCourse(UUID courseId) {
        return list("id.courseId", courseId);
    }

    public boolean existsByCourseAndTeacher(UUID courseId, UUID teacherId) {
        return count("id.courseId = ?1 and id.teacherId = ?2", courseId, teacherId) > 0;
    }
}
