package cl.duocuc.edutrack.ms.course.model.entity;

import cl.duocuc.edutrack.ms.infrastructure.security.Permission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El nivel de acceso por instancia (BE-CRS-002) reutiliza la aritmética de bits
 * Unix-style de la plataforma: {@code READ = r-- (4)}, {@code WRITE = rw- (6)}.
 * Así {@code GET /course/access} responde con la misma semántica que
 * {@code GET /auth/access} sin reimplementar el AND de bits.
 */
class AccessLevelTest {

    @Test
    @DisplayName("Flags Unix-style: READ=4 (r--), WRITE=6 (rw-)")
    void flags_matchUnixContract() {
        assertEquals(4, AccessLevel.READ.flags);
        assertEquals(6, AccessLevel.WRITE.flags);
    }

    @Test
    @DisplayName("READ solo satisface lectura")
    void read_satisfiesOnlyRead() {
        assertTrue(AccessLevel.READ.satisfies(Permission.READ));
        assertFalse(AccessLevel.READ.satisfies(Permission.WRITE));
        assertFalse(AccessLevel.READ.satisfies(Permission.EXECUTE));
        assertFalse(AccessLevel.READ.canWrite());
    }

    @Test
    @DisplayName("WRITE satisface lectura y escritura (la escritura implica lectura)")
    void write_satisfiesReadAndWrite() {
        assertTrue(AccessLevel.WRITE.satisfies(Permission.READ));
        assertTrue(AccessLevel.WRITE.satisfies(Permission.WRITE));
        assertFalse(AccessLevel.WRITE.satisfies(Permission.EXECUTE));
        assertTrue(AccessLevel.WRITE.canWrite());
    }
}
