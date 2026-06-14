package cl.duocuc.edutrack.ms.course.model.entity;

import cl.duocuc.edutrack.ms.infrastructure.security.Permission;

/**
 * Nivel de acceso de un docente sobre una <b>instancia</b> de curso (BE-CRS-002).
 *
 * <p>Es el permiso granular <em>por usuario e instancia</em> que complementa el
 * modelo de roles de Auth: Auth concede el verbo sobre el <em>tipo</em>
 * ({@code course.courses}); este enum decide qué puede hacer un docente
 * concreto sobre un curso concreto según su asignación.</p>
 *
 * <p>Se modela con los mismos bits Unix-style del resto de la plataforma
 * ({@link Permission}: {@code r=4, w=2, x=1}) para reutilizar exactamente el
 * mismo algoritmo de verificación ({@code (flags & required) == required}) que
 * usan {@code @RequirePermission} y {@code GET /auth/access}. Así
 * {@code GET /course/access} responde con la misma semántica que su análogo en
 * Auth, sin reimplementar la aritmética de bits.</p>
 *
 * <ul>
 *   <li>{@link #READ} ⇒ {@code r--} (4): solo lectura.</li>
 *   <li>{@link #WRITE} ⇒ {@code rw-} (6): la escritura implica lectura.</li>
 * </ul>
 */
public enum AccessLevel {

    /** Solo lectura ({@code r--}, 4). */
    READ((short) 4),

    /** Lectura y escritura ({@code rw-}, 6). La escritura implica lectura. */
    WRITE((short) 6);

    /** Flags Unix-style efectivos de este nivel ({@code r=4, w=2}). */
    public final short flags;

    AccessLevel(short flags) {
        this.flags = flags;
    }

    /**
     * ¿Este nivel de acceso satisface el permiso Unix-style requerido?
     * Aplica el mismo AND bit a bit que el resto del modelo de permisos.
     */
    public boolean satisfies(Permission required) {
        return (flags & required.bit) == required.bit;
    }

    /** Atajo de legibilidad: ¿este nivel permite escritura? */
    public boolean canWrite() {
        return satisfies(Permission.WRITE);
    }
}
