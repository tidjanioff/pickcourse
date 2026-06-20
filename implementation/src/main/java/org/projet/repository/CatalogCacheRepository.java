package org.projet.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.Jdbi;
import org.projet.config.DatabaseConfig;
import org.projet.model.Cours;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Accès SQL au cache local du catalogue Planifium.
 */
public class CatalogCacheRepository {
    private final Jdbi jdbi;
    private final ObjectMapper mapper = new ObjectMapper();

    public CatalogCacheRepository() {
        this(DatabaseConfig.createJdbi());
    }

    public CatalogCacheRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public boolean isEmpty() {
        Integer count = jdbi.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM courses")
                        .mapTo(Integer.class)
                        .one()
        );
        return count == null || count == 0;
    }

    public void upsertCourse(Cours cours) {
        try {
            String rawData = mapper.writeValueAsString(cours);
            jdbi.useHandle(handle -> handle.createUpdate("""
                            INSERT INTO courses (
                                id,
                                name,
                                description,
                                credits,
                                requirement_text,
                                udem_website,
                                raw_data
                            )
                            VALUES (
                                :id,
                                :name,
                                :description,
                                :credits,
                                :requirementText,
                                :udemWebsite,
                                CAST(:rawData AS jsonb)
                            )
                            ON CONFLICT (id) DO UPDATE SET
                                name = EXCLUDED.name,
                                description = EXCLUDED.description,
                                credits = EXCLUDED.credits,
                                requirement_text = EXCLUDED.requirement_text,
                                udem_website = EXCLUDED.udem_website,
                                raw_data = EXCLUDED.raw_data
                            """)
                    .bind("id", cours.getId())
                    .bind("name", cours.getName())
                    .bind("description", cours.getDescription())
                    .bind("credits", cours.getCredits())
                    .bind("requirementText", cours.getRequirement_text())
                    .bind("udemWebsite", cours.getUdemWebsite())
                    .bind("rawData", rawData)
                    .execute());
        } catch (Exception e) {
            throw new RuntimeException("Erreur upsert course " + cours.getId(), e);
        }
    }

    public void upsertProgram(JsonNode program) {
        String id = program.path("id").asText(null);
        if (id == null || id.isBlank()) {
            return;
        }

        try {
            String rawData = mapper.writeValueAsString(program);
            jdbi.useHandle(handle -> handle.createUpdate("""
                            INSERT INTO programs (id, name, raw_data)
                            VALUES (:id, :name, CAST(:rawData AS jsonb))
                            ON CONFLICT (id) DO UPDATE SET
                                name = EXCLUDED.name,
                                raw_data = EXCLUDED.raw_data
                            """)
                    .bind("id", id)
                    .bind("name", program.path("name").asText(null))
                    .bind("rawData", rawData)
                    .execute());
        } catch (Exception e) {
            throw new RuntimeException("Erreur upsert program " + id, e);
        }
    }

    public void upsertSchedule(String courseId, Cours.Schedule schedule) {
        if (courseId == null || courseId.isBlank() || schedule.getSemester() == null) {
            return;
        }

        try {
            String rawData = mapper.writeValueAsString(schedule);
            jdbi.useHandle(handle -> handle.createUpdate("""
                            INSERT INTO schedules (course_id, semester, raw_data, fetched_at)
                            VALUES (:courseId, :semester, CAST(:rawData AS jsonb), now())
                            ON CONFLICT (course_id, semester) DO UPDATE SET
                                raw_data = EXCLUDED.raw_data,
                                fetched_at = now()
                            """)
                    .bind("courseId", courseId)
                    .bind("semester", schedule.getSemester())
                    .bind("rawData", rawData)
                    .execute());
        } catch (Exception e) {
            throw new RuntimeException("Erreur upsert schedule " + courseId + " " + schedule.getSemester(), e);
        }
    }

    public Optional<List<Cours>> findCoursesBy(String param, String value, boolean includeSchedule, String semester) {
        if (param == null || value == null) {
            return Optional.empty();
        }

        String sql;
        String queryValue;
        if (param.equalsIgnoreCase("id")) {
            sql = value.matches("^[A-Z]{3}\\d{4}$")
                    ? "SELECT raw_data::text FROM courses WHERE upper(id) = upper(:value) ORDER BY id"
                    : "SELECT raw_data::text FROM courses WHERE upper(id) LIKE upper(:value) ORDER BY id";
            queryValue = value.matches("^[A-Z]{3}\\d{4}$") ? value : value + "%";
        } else if (param.equalsIgnoreCase("name")) {
            sql = "SELECT raw_data::text FROM courses WHERE lower(name) LIKE lower(:value) ORDER BY id";
            queryValue = "%" + value + "%";
        } else if (param.equalsIgnoreCase("description")) {
            sql = "SELECT raw_data::text FROM courses WHERE lower(description) LIKE lower(:value) ORDER BY id";
            queryValue = "%" + value + "%";
        } else {
            return Optional.empty();
        }

        List<Cours> courses = jdbi.withHandle(handle ->
                handle.createQuery(sql)
                        .bind("value", queryValue)
                        .map((rs, ctx) -> readCourse(rs.getString(1)))
                        .list()
        );

        if (courses.isEmpty()) {
            return Optional.empty();
        }

        if (includeSchedule) {
            for (Cours course : courses) {
                course.setSchedules(findSchedules(course.getId(), semester));
            }
        }

        return Optional.of(courses);
    }

    public List<Cours.Schedule> findSchedules(String courseId, String semester) {
        String sql = semester == null || semester.isBlank()
                ? "SELECT raw_data::text FROM schedules WHERE course_id = :courseId ORDER BY semester"
                : "SELECT raw_data::text FROM schedules WHERE course_id = :courseId AND semester = :semester ORDER BY semester";

        return jdbi.withHandle(handle -> {
            var query = handle.createQuery(sql).bind("courseId", courseId);
            if (semester != null && !semester.isBlank()) {
                query.bind("semester", semester);
            }
            return query.map((rs, ctx) -> readSchedule(rs.getString(1))).list();
        });
    }

    public Optional<List<String>> findAllCourseIds() {
        List<String> ids = jdbi.withHandle(handle ->
                handle.createQuery("SELECT id FROM courses ORDER BY id")
                        .mapTo(String.class)
                        .list()
        );

        if (!ids.isEmpty()) {
            return Optional.of(ids);
        }

        Set<String> fromPrograms = new HashSet<>();
        for (JsonNode program : findProgramRawData()) {
            collectCourseIds(program, fromPrograms);
        }

        return Optional.of(new ArrayList<>(fromPrograms));
    }

    public List<Map<String, String>> findAllPrograms() {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT id, name FROM programs ORDER BY id")
                        .map((rs, ctx) -> {
                            Map<String, String> map = new HashMap<>();
                            map.put("id", rs.getString("id"));
                            map.put("name", rs.getString("name"));
                            return map;
                        })
                        .list()
        );
    }

    public String coursesForProgramJson(String programId) {
        Optional<String> raw = jdbi.withHandle(handle ->
                handle.createQuery("SELECT raw_data::text FROM programs WHERE id = :id")
                        .bind("id", programId)
                        .mapTo(String.class)
                        .findOne()
        );

        if (raw.isEmpty()) {
            return "[]";
        }

        try {
            JsonNode program = mapper.readTree(raw.get());
            Set<String> courses = new HashSet<>();
            collectCourseIds(program, courses);
            return mapper.writeValueAsString(List.of(Map.of("courses", new ArrayList<>(courses))));
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture programme " + programId, e);
        }
    }

    public InputStream schedulesAsInputStream(String courseId, String semester) {
        try {
            String json = mapper.writeValueAsString(findSchedules(courseId, semester));
            return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture horaires " + courseId, e);
        }
    }

    private List<JsonNode> findProgramRawData() {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT raw_data::text FROM programs")
                        .map((rs, ctx) -> readJson(rs.getString(1)))
                        .list()
        );
    }

    private Cours readCourse(String json) {
        try {
            return mapper.readValue(json, Cours.class);
        } catch (Exception e) {
            throw new RuntimeException("Erreur mapping course cache", e);
        }
    }

    private Cours.Schedule readSchedule(String json) {
        try {
            return mapper.readValue(json, Cours.Schedule.class);
        } catch (Exception e) {
            throw new RuntimeException("Erreur mapping schedule cache", e);
        }
    }

    private JsonNode readJson(String json) {
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Erreur mapping JSON cache", e);
        }
    }

    private void collectCourseIds(JsonNode node, Set<String> courseIds) {
        if (node == null || node.isNull()) {
            return;
        }

        JsonNode courses = node.get("courses");
        if (courses != null && courses.isArray()) {
            for (JsonNode course : courses) {
                courseIds.add(course.asText());
            }
        }

        if (node.isObject() || node.isArray()) {
            for (JsonNode child : node) {
                collectCourseIds(child, courseIds);
            }
        }
    }
}
