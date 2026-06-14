-- DDL del schema `course` (Shared DB, schema-per-service — ADR-001).
-- Course Service: CRUD de cursos (borrado lógico) + relación docente-curso con
-- nivel de acceso (permiso granular por usuario e instancia, BE-CRS-001..003).

CREATE SCHEMA IF NOT EXISTS course;

CREATE TABLE course.courses (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(150) NOT NULL,
    description   VARCHAR(500),
    level         VARCHAR(50),
    section       VARCHAR(10),
    academic_year INTEGER      NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    deleted_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    creator_user  UUID         NOT NULL,
    updater_user  UUID         NOT NULL
);

-- teacher_id es el userId del docente en Auth: sin FK cross-schema (cada MS
-- tiene credenciales exclusivas a su propio schema). Integridad a nivel app.
CREATE TABLE course.course_assignments (
    course_id    UUID        NOT NULL REFERENCES course.courses(id) ON DELETE CASCADE,
    teacher_id   UUID        NOT NULL,
    access_level VARCHAR(10) NOT NULL,
    assigned_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (course_id, teacher_id)
);

CREATE INDEX idx_courses_status                 ON course.courses(status);
CREATE INDEX idx_course_assignments_teacher_id  ON course.course_assignments(teacher_id);
