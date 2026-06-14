package cl.duocuc.edutrack.ms.course.model.entity;

/**
 * Estado del ciclo de vida de un curso. El borrado es <b>lógico</b>: un curso
 * eliminado pasa a {@link #DELETED} y deja de aparecer en los listados activos,
 * pero su fila (y el historial académico que lo referencia) se preserva
 * (BE-CRS-001).
 */
public enum CourseStatus {

    /** Curso vigente: aparece en listados y admite escritura. */
    ACTIVE,

    /** Curso borrado lógicamente: oculto en listados activos, fila preservada. */
    DELETED
}
