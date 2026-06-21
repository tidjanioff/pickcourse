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
import java.util.ArrayList;
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

        // Pass 1: programs.
        try {
            JsonNode programs = fetchProgramsRaw();
            SyncCounts counts = syncFromLivePrograms(programs);
            long millis = Duration.between(start, Instant.now()).toMillis();
            LOGGER.info("Program sync completed: programs=" + counts.programs
                    + ", durationMs=" + millis);
            if (counts.programs == 0) {
                LOGGER.warning("Catalog sync completed with missing data: programs=" + counts.programs
                        + ", courses=0, schedules=0, durationMs=" + millis);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Catalog sync failed", e);
        }

        // Pass 2: direct courses.
        List<Cours> directCourses = new ArrayList<>();
        try {
            directCourses = syncDirectCourses(fetchCoursesRaw());
            long millis = Duration.between(start, Instant.now()).toMillis();
            LOGGER.info("Direct courses sync completed: " + directCourses.size() + " courses upserted");
            if (directCourses.isEmpty()) {
                LOGGER.warning("Direct courses sync completed with zero courses upserted, durationMs=" + millis);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Direct courses sync failed", e);
        }

        // Pass 3: schedules driven by the direct course list.
        try {
            int scheduleCount = syncSchedulesForDirectCourses(directCourses);
            long millis = Duration.between(start, Instant.now()).toMillis();
            LOGGER.info("Schedule sync completed: " + scheduleCount
                    + " schedules upserted across " + directCourses.size() + " courses");
            if (scheduleCount == 0) {
                LOGGER.warning("Schedule sync completed with zero schedules upserted across "
                        + directCourses.size() + " courses, durationMs=" + millis);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Schedule sync failed", e);
        }
    }

    public JsonNode fetchProgramsRaw() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri("/programs", null))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Planifium programs status: " + response.statusCode()
                    + ", body: " + body);
        }

        JsonNode programs = mapper.readTree(body);
        if (!programs.isArray()) {
            throw new IllegalStateException("Planifium programs response must be a JSON array, got: " + body);
        }
        return programs;
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
            LOGGER.warning("Skipping course " + courseId + ": Planifium status=" + response.statusCode()
                    + ", body: " + response.body());
            return Optional.empty();
        }

        JsonNode root = mapper.readTree(response.body());
        if (!root.isObject() || !root.hasNonNull("id")) {
            LOGGER.warning("Skipping course " + courseId + ": malformed Planifium response body: "
                    + response.body());
            return Optional.empty();
        }

        return Optional.of(mapper.treeToValue(root, Cours.class));
    }

    public JsonNode fetchCoursesRaw() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri("/courses", null))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Planifium courses status: " + response.statusCode()
                    + ", body: " + body);
        }

        JsonNode courses = mapper.readTree(body);
        if (!courses.isArray()) {
            throw new IllegalStateException("Planifium courses response must be a JSON array, got: " + body);
        }
        return courses;
    }

    public Optional<Cours.Schedule> fetchScheduleFromPlanifium(String courseId, String semester) {
        if (semester == null || semester.isBlank()) {
            return Optional.empty();
        }

        try (InputStream response = fetchSchedulesFromPlanifium(courseId, semester)) {
            if (response == null) {
                LOGGER.warning("Skipping schedule " + courseId + " " + semester + ": empty Planifium response");
                return Optional.empty();
            }

            JsonNode schedules = mapper.readTree(response);
            if (!schedules.isArray()) {
                LOGGER.warning("Skipping schedule " + courseId + " " + semester
                        + ": Planifium response is not an array: " + schedules);
                return Optional.empty();
            }

            for (JsonNode schedule : schedules) {
                if (semester.equals(schedule.path("semester").asText())) {
                    return Optional.of(mapper.treeToValue(schedule, Cours.Schedule.class));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to fetch schedule " + courseId + " " + semester, e);
        }

        LOGGER.warning("Skipping schedule " + courseId + " " + semester
                + ": no matching schedule returned by Planifium");
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

    private SyncCounts syncFromLivePrograms(JsonNode programs) throws Exception {
        SyncCounts counts = new SyncCounts();
        Set<String> courseIds = new HashSet<>();

        for (JsonNode program : programs) {
            cacheRepository.upsertProgram(program);
            counts.programs++;
            collectCourseIds(program, courseIds);
        }

        for (String courseId : courseIds) {
            Optional<Cours> courseOpt = fetchCourseFromPlanifium(courseId, true);
            if (courseOpt.isEmpty()) {
                continue;
            }

            Cours course = courseOpt.get();
            cacheRepository.upsertCourse(course);
            counts.courses++;

            if (course.getSchedules() != null) {
                for (Cours.Schedule schedule : course.getSchedules()) {
                    Cours.Schedule freshSchedule = fetchScheduleFromPlanifium(courseId, schedule.getSemester())
                            .orElse(schedule);
                    cacheRepository.upsertSchedule(courseId, freshSchedule);
                    counts.schedules++;
                }
            }
        }

        return counts;
    }

    private List<Cours> syncDirectCourses(JsonNode courses) throws Exception {
        List<Cours> directCourses = new ArrayList<>();
        for (JsonNode courseNode : courses) {
            try {
                Cours course = mapper.treeToValue(courseNode, Cours.class);
                cacheRepository.upsertCourse(course);
                directCourses.add(course);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Skipping malformed direct course payload: " + courseNode, e);
            }
        }
        return directCourses;
    }

    private int syncSchedulesForDirectCourses(List<Cours> directCourses) {
        int schedulesSynced = 0;

        for (Cours directCourse : directCourses) {
            try {
                Optional<Cours> detailedCourseOpt = fetchCourseFromPlanifium(directCourse.getId(), true);
                if (detailedCourseOpt.isEmpty()) {
                    LOGGER.warning("Skipping schedule sync for course " + directCourse.getId()
                            + ": unable to load detailed course data");
                    continue;
                }

                Cours detailedCourse = detailedCourseOpt.get();
                if (detailedCourse.getSchedules() == null || detailedCourse.getSchedules().isEmpty()) {
                    LOGGER.warning("Skipping schedule sync for course " + directCourse.getId()
                            + ": no schedules returned by Planifium");
                    continue;
                }

                for (Cours.Schedule schedule : detailedCourse.getSchedules()) {
                    Cours.Schedule freshSchedule = fetchScheduleFromPlanifium(directCourse.getId(), schedule.getSemester())
                            .orElse(schedule);
                    cacheRepository.upsertSchedule(directCourse.getId(), freshSchedule);
                    schedulesSynced++;
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Unable to sync schedules for course " + directCourse.getId(), e);
            }
        }

        return schedulesSynced;
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

    private static class SyncCounts {
        private int programs;
        private int courses;
        private int schedules;
    }
}
