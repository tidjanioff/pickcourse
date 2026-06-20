package org.projet.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.projet.model.Cours;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class CoursRepositoryTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("pickcourse")
            .withUsername("pickcourse")
            .withPassword("devpassword");

    private final ObjectMapper mapper = new ObjectMapper();
    private CoursRepository repo;
    private CatalogCacheRepository cacheRepository;

    @BeforeEach
    void setUp() throws Exception {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .load()
                .migrate();

        Jdbi jdbi = Jdbi.create(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        jdbi.useHandle(handle -> {
            handle.execute("TRUNCATE TABLE schedules, courses, programs RESTART IDENTITY");
            handle.execute("TRUNCATE TABLE reviews RESTART IDENTITY");
        });

        cacheRepository = new CatalogCacheRepository(jdbi);
        repo = new CoursRepository(cacheRepository);
        seedCatalog();
    }

    @Test
    @DisplayName("getAllCoursesId() retourne les cours du cache sans doublons")
    void testGetAllCoursesId() throws Exception {
        Optional<List<String>> opt = repo.getAllCoursesId();

        assertTrue(opt.isPresent());
        assertEquals(List.of("IFT1025", "IFT2255"), opt.get());
    }

    @Test
    @DisplayName("getCourseBy(id) retourne une liste contenant le cours demandé")
    void testGetCourseByIdIFT1025() throws Exception {
        Optional<List<Cours>> opt = repo.getCourseBy("id", "IFT1025", null, null);

        assertTrue(opt.isPresent(), "L'Optional doit être présent");
        assertEquals(1, opt.get().size(), "La recherche par id doit retourner un seul Cours");
        assertEquals("IFT1025", opt.get().get(0).getId());
    }

    @Test
    @DisplayName("getCourseBy(id) supporte la recherche par préfixe de sigle")
    void testGetCourseByIdPrefix() throws Exception {
        Optional<List<Cours>> opt = repo.getCourseBy("id", "IFT", null, null);

        assertTrue(opt.isPresent());
        assertEquals(2, opt.get().size());
    }

    @Test
    @DisplayName("getCourseBy(id) retourne Optional.empty() pour un id inexistant")
    void testGetCourseByIdCoursInexistant() throws Exception {
        Optional<List<Cours>> opt = repo.getCourseBy("id", "TIDJANI45", null, null);

        assertTrue(opt.isEmpty(), "Un id inexistant doit retourner Optional.empty()");
    }

    @Test
    @DisplayName("getCourseBy('name') retourne une liste de cours par recherche de mot-clé")
    void testGetCourseByName() throws Exception {
        Optional<List<Cours>> opt = repo.getCourseBy("name", "Programmation", "false", null);

        assertTrue(opt.isPresent(), "La recherche par nom devrait retourner un résultat");
        assertFalse(opt.get().isEmpty(), "La liste ne devrait pas être vide");
        assertTrue(opt.get().get(0).getName().contains("Programmation"));
    }

    @Test
    @DisplayName("getCourseBy(description) retourne une liste de cours par mot-clé")
    void testGetCourseByDescription() throws Exception {
        Optional<List<Cours>> opt = repo.getCourseBy("description", "logiciel", "false", null);

        assertTrue(opt.isPresent());
        assertEquals("IFT2255", opt.get().get(0).getId());
    }

    @Test
    @DisplayName("getCourseBy() ajoute les horaires depuis le cache")
    void testGetCourseByWithSchedules() throws Exception {
        Optional<List<Cours>> opt = repo.getCourseBy("id", "IFT2255", "true", "A25");

        assertTrue(opt.isPresent());
        assertEquals(1, opt.get().get(0).getSchedules().size());
        assertEquals("A25", opt.get().get(0).getSchedules().get(0).getSemester());
    }

    @Test
    @DisplayName("getCourseBy() refuse un filtre session si includeSchedule=false")
    void testGetCourseBySemesterWithoutSchedules() throws Exception {
        Optional<List<Cours>> opt = repo.getCourseBy("id", "IFT2255", "false", "A25");

        assertTrue(opt.isEmpty());
    }

    @Test
    @DisplayName("getAllPrograms() retourne les programmes du cache")
    void testGetAllPrograms() {
        List<Map<String, String>> programs = repo.getAllPrograms();

        assertEquals(1, programs.size());
        assertEquals("117510", programs.get(0).get("id"));
        assertEquals("Informatique", programs.get(0).get("name"));
    }

    @Test
    @DisplayName("getCoursesForAProgram() retourne le JSON attendu par le service")
    void testGetCoursesForAProgram() throws Exception {
        String json = repo.getCoursesForAProgram("117510");

        assertTrue(json.contains("IFT1025"));
        assertTrue(json.contains("IFT2255"));
    }

    @Test
    @DisplayName("fetchSchedules() retourne un flux JSON depuis le cache")
    void testFetchSchedules() throws Exception {
        try (InputStream inputStream = repo.fetchSchedules("IFT2255", "A25")) {
            var json = mapper.readTree(inputStream);
            assertTrue(json.isArray());
            assertEquals("A25", json.get(0).get("semester").asText());
        }
    }

    private void seedCatalog() throws Exception {
        cacheRepository.upsertCourse(cours("IFT1025", "Programmation 2", "Programmation objet"));
        cacheRepository.upsertCourse(cours("IFT2255", "Génie logiciel", "Projet logiciel"));
        cacheRepository.upsertProgram(mapper.readTree("""
                {
                  "id": "117510",
                  "name": "Informatique",
                  "segments": [
                    {
                      "blocs": [
                        {
                          "courses": ["IFT1025", "IFT2255"]
                        }
                      ]
                    }
                  ]
                }
                """));

        Cours.Schedule schedule = new Cours.Schedule();
        schedule.setSigle("IFT2255");
        schedule.setName("Génie logiciel");
        schedule.setSemester("A25");
        cacheRepository.upsertSchedule("IFT2255", schedule);
    }

    private Cours cours(String id, String name, String description) {
        Cours cours = new Cours();
        cours.setId(id);
        cours.setName(name);
        cours.setDescription(description);
        cours.setCredits(3);
        cours.setRequirement_text("Aucun");
        cours.setUdemWebsite("https://example.com/" + id);
        return cours;
    }
}
