package org.projet.service;

import com.sun.net.httpserver.HttpServer;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projet.repository.CatalogCacheRepository;
import org.projet.repository.CoursRepository;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class CatalogSyncServiceTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("pickcourse")
            .withUsername("pickcourse")
            .withPassword("devpassword");

    private HttpServer server;
    private Jdbi jdbi;
    private CatalogCacheRepository cacheRepository;
    private CoursRepository coursRepository;

    @BeforeEach
    void setUp() throws Exception {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .load()
                .migrate();

        jdbi = Jdbi.create(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        jdbi.useHandle(handle -> handle.execute("TRUNCATE TABLE schedules, courses, programs RESTART IDENTITY"));

        cacheRepository = new CatalogCacheRepository(jdbi);
        coursRepository = new CoursRepository(cacheRepository);

        server = startPlanifiumStub(validProgramsResponse());
        System.setProperty("planifium.base", "http://localhost:" + server.getAddress().getPort());

        new CatalogSyncService(cacheRepository).syncAll();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("planifium.base");
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void syncAllRemplitLeCacheCatalogue() throws Exception {
        assertEquals(1, coursRepository.getAllPrograms().size());
        assertEquals(List.of("IFT2255"), coursRepository.getAllCoursesId().orElseThrow());

        var cours = coursRepository.getCourseBy("id", "IFT2255", "true", "A25").orElseThrow();
        assertEquals("Génie logiciel", cours.get(0).getName());
        assertEquals(1, cours.get(0).getSchedules().size());
        assertEquals("A25", cours.get(0).getSchedules().get(0).getSemester());
    }

    @Test
    void fetchProgramsRawEchoueSiPlanifiumRetourneUnObjetErreur() throws Exception {
        server.stop(0);
        server = startPlanifiumStub("""
                {
                  "status_code": 500,
                  "detail": "validation error"
                }
                """);
        System.setProperty("planifium.base", "http://localhost:" + server.getAddress().getPort());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new CatalogSyncService(cacheRepository).fetchProgramsRaw()
        );

        assertTrue(exception.getMessage().contains("Planifium programs response must be a JSON array"));
        assertTrue(exception.getMessage().contains("\"status_code\": 500"));
        assertTrue(exception.getMessage().contains("validation error"));
    }

    @Test
    void syncAllUtiliseLeSeedLocalSiPlanifiumEchoueEtCacheVide() throws Exception {
        truncateCatalog();
        server.stop(0);
        server = startPlanifiumStub(malformedProgramsResponse());
        System.setProperty("planifium.base", "http://localhost:" + server.getAddress().getPort());

        new CatalogSyncService(cacheRepository).syncAll();

        assertEquals(5, coursRepository.getAllPrograms().size());
        assertEquals(8, coursRepository.getAllCoursesId().orElseThrow().size());
        assertTrue(coursRepository.getAllCoursesId().orElseThrow().contains("IFT2255"));
        assertEquals(1, coursRepository.getCourseBy("id", "IFT2255", "true", "A25")
                .orElseThrow()
                .get(0)
                .getSchedules()
                .size());
    }

    @Test
    void syncAllNeRemplacePasUnCacheExistantParLeSeedLocal() throws Exception {
        server.stop(0);
        server = startPlanifiumStub(malformedProgramsResponse());
        System.setProperty("planifium.base", "http://localhost:" + server.getAddress().getPort());

        new CatalogSyncService(cacheRepository).syncAll();

        assertEquals(1, coursRepository.getAllPrograms().size());
        assertEquals(List.of("IFT2255"), coursRepository.getAllCoursesId().orElseThrow());
    }

    private HttpServer startPlanifiumStub(String programsResponse) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String response;

            if ("/api/v1/programs".equals(path)) {
                response = programsResponse;
            } else if ("/api/v1/courses/IFT2255".equals(path)) {
                response = """
                        {
                          "id": "IFT2255",
                          "name": "Génie logiciel",
                          "description": "Projet logiciel",
                          "credits": 3,
                          "requirement_text": "Aucun",
                          "udemWebsite": "https://example.com/IFT2255",
                          "schedules": [
                            {
                              "sigle": "IFT2255",
                              "name": "Génie logiciel",
                              "semester": "A25",
                              "sections": []
                            }
                          ]
                        }
                        """;
            } else if ("/api/v1/schedules".equals(path)) {
                response = """
                        [
                          {
                            "sigle": "IFT2255",
                            "name": "Génie logiciel",
                            "semester": "A25",
                            "sections": []
                          }
                        ]
                        """;
            } else {
                exchange.sendResponseHeaders(404, 0);
                exchange.close();
                return;
            }

            byte[] body = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        httpServer.start();
        return httpServer;
    }

    private String validProgramsResponse() {
        return """
                [
                  {
                    "id": "117510",
                    "name": "Informatique",
                    "segments": [
                      {
                        "blocs": [
                          {
                            "courses": ["IFT2255"]
                          }
                        ]
                      }
                    ]
                  }
                ]
                """;
    }

    private String malformedProgramsResponse() {
        return """
                {
                  "status_code": 500,
                  "detail": "validation error"
                }
                """;
    }

    private void truncateCatalog() {
        jdbi.useHandle(handle -> handle.execute("TRUNCATE TABLE schedules, courses, programs RESTART IDENTITY"));
    }
}
