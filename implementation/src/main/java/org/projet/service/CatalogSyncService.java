package org.projet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.projet.model.Cours;
import org.projet.repository.CatalogCacheRepository;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Synchronise le catalogue Planifium vers le cache PostgreSQL local.
 */
public class CatalogSyncService {
    private static final Logger LOGGER = Logger.getLogger(CatalogSyncService.class.getName());
    private static final String PLANIFIUM_BASE_URL = "https://planifium-api.onrender.com/api/v1";

    private final CatalogCacheRepository cacheRepository;
    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public CatalogSyncService() {
        this(new CatalogCacheRepository());
    }

    public CatalogSyncService(CatalogCacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    public void syncAll() {
        Instant start = Instant.now();
        int programsSynced = 0;
        int coursesSynced = 0;
        int schedulesSynced = 0;

        try {
            JsonNode programs = fetchProgramsRaw();
            Set<String> courseIds = new HashSet<>();

            if (programs.isArray()) {
                for (JsonNode program : programs) {
                    cacheRepository.upsertProgram(program);
                    programsSynced++;
                    collectCourseIds(program, courseIds);
                }
            }

            for (String courseId : courseIds) {
                Optional<Cours> courseOpt = fetchCourseFromPlanifium(courseId, true);
                if (courseOpt.isEmpty()) {
                    continue;
                }

                Cours course = courseOpt.get();
                cacheRepository.upsertCourse(course);
                coursesSynced++;

                if (course.getSchedules() != null) {
                    for (Cours.Schedule schedule : course.getSchedules()) {
                        Cours.Schedule freshSchedule = fetchScheduleFromPlanifium(courseId, schedule.getSemester())
                                .orElse(schedule);
                        cacheRepository.upsertSchedule(courseId, freshSchedule);
                        schedulesSynced++;
                    }
                }
            }

            long millis = Duration.between(start, Instant.now()).toMillis();
            LOGGER.info("Catalog sync completed: programs=" + programsSynced
                    + ", courses=" + coursesSynced
                    + ", schedules=" + schedulesSynced
                    + ", durationMs=" + millis);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Catalog sync failed", e);
        }
    }

    public JsonNode fetchProgramsRaw() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri("/programs", null))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Planifium programs status: " + response.statusCode());
        }
        return mapper.readTree(response.body());
    }

    public Optional<Cours> fetchCourseFromPlanifium(String courseId, boolean includeSchedule) throws Exception {
        String path = "/courses/" + URLEncoder.encode(courseId, StandardCharsets.UTF_8);
        URI uri = buildUri(
                path,
                includeSchedule ? Map.of("include_schedule", "true") : null
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return Optional.empty();
        }

        JsonNode root = mapper.readTree(response.body());
        if (!root.isObject() || !root.hasNonNull("id")) {
            return Optional.empty();
        }

        return Optional.of(mapper.treeToValue(root, Cours.class));
    }

    public Optional<Cours.Schedule> fetchScheduleFromPlanifium(String courseId, String semester) {
        if (semester == null || semester.isBlank()) {
            return Optional.empty();
        }

        try (InputStream response = fetchSchedulesFromPlanifium(courseId, semester)) {
            if (response == null) {
                return Optional.empty();
            }

            JsonNode schedules = mapper.readTree(response);
            if (!schedules.isArray()) {
                return Optional.empty();
            }

            for (JsonNode schedule : schedules) {
                if (semester.equals(schedule.path("semester").asText())) {
                    return Optional.of(mapper.treeToValue(schedule, Cours.Schedule.class));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Unable to fetch schedule " + courseId + " " + semester, e);
        }

        return Optional.empty();
    }

    public InputStream fetchSchedulesFromPlanifium(String courseId, String semester) throws Exception {
        URI uri = buildUri(
                "/schedules",
                Map.of(
                        "courses_list", "[\"" + courseId + "\"]",
                        "min_semester", semester
                )
        );

        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        return connection.getInputStream();
    }

    private URI buildUri(String path, Map<String, String> params) {
        String base = planifiumBaseUrl() + path;
        StringBuilder sb = new StringBuilder(base);

        if (params != null && !params.isEmpty()) {
            sb.append("?");
            params.forEach((key, value) -> sb
                    .append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                    .append("&"));
            sb.deleteCharAt(sb.length() - 1);
        }

        return URI.create(sb.toString());
    }

    private String planifiumBaseUrl() {
        String override = System.getProperty("planifium.base");
        if (override != null && !override.isBlank()) {
            return override.replaceAll("/+$", "") + "/api/v1";
        }
        return PLANIFIUM_BASE_URL;
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
