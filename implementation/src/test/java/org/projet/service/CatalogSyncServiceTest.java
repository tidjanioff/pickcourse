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

        server = startPlanifiumStub(validProgramsResponse(), directCoursesResponseSingle());
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
                """, directCoursesResponseSingle());
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
        server = startPlanifiumStub(malformedProgramsResponse(), directCoursesResponseWithNewIds());
        System.setProperty("planifium.base", "http://localhost:" + server.getAddress().getPort());

        new CatalogSyncService(cacheRepository).syncAll();

        assertEquals(5, coursRepository.getAllPrograms().size());
        assertEquals(10, coursRepository.getAllCoursesId().orElseThrow().size());
        assertTrue(coursRepository.getAllCoursesId().orElseThrow().contains("IFT3000"));
        assertTrue(coursRepository.getAllCoursesId().orElseThrow().contains("IFT3999"));
        assertEquals(1, coursRepository.getCourseBy("id", "IFT3000", "true", "A25")
                .orElseThrow()
                .get(0)
                .getSchedules()
                .size());
        assertEquals(1, coursRepository.getCourseBy("id", "IFT3999", "true", "H26")
                .orElseThrow()
                .get(0)
                .getSchedules()
                .size());
    }

    @Test
    void syncAllNeRemplacePasUnCacheExistantParLeSeedLocal() throws Exception {
        server.stop(0);
        server = startPlanifiumStub(malformedProgramsResponse(), directCoursesResponseSingle());
        System.setProperty("planifium.base", "http://localhost:" + server.getAddress().getPort());

        new CatalogSyncService(cacheRepository).syncAll();

        assertEquals(1, coursRepository.getAllPrograms().size());
        assertEquals(List.of("IFT2255"), coursRepository.getAllCoursesId().orElseThrow());
    }

    private HttpServer startPlanifiumStub(String programsResponse, String coursesResponse) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String response;

            if ("/api/v1/programs".equals(path)) {
                response = programsResponse;
            } else if ("/api/v1/courses".equals(path)) {
                response = coursesResponse;
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
            } else if ("/api/v1/courses/IFT3000".equals(path)) {
                response = detailedCourseResponse("IFT3000", "Projet logiciel avancé", "A25");
            } else if ("/api/v1/courses/IFT3999".equals(path)) {
                response = detailedCourseResponse("IFT3999", "Atelier d'intégration", "H26");
            } else if ("/api/v1/schedules".equals(path)) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.contains("IFT3000") && query.contains("A25")) {
                    response = """
                            [
                              {
                                "sigle": "IFT3000",
                                "name": "Projet logiciel avancé",
                                "semester": "A25",
                                "sections": []
                              }
                            ]
                            """;
                } else if (query != null && query.contains("IFT3999") && query.contains("H26")) {
                    response = """
                            [
                              {
                                "sigle": "IFT3999",
                                "name": "Atelier d'intégration",
                                "semester": "H26",
                                "sections": []
                              }
                            ]
                            """;
                } else {
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
                }
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

    private String directCoursesResponseSingle() {
        return """
                [
                  {
                    "id": "IFT2255",
                    "name": "Génie logiciel",
                    "credits": 3,
                    "description": "Projet logiciel",
                    "available_terms": {
                      "autumn": true,
                      "winter": true,
                      "summer": false
                    },
                    "available_periods": {
                      "daytime": true,
                      "evening": false
                    },
                    "schedules": []
                  }
                ]
                """;
    }

    private String directCoursesResponseWithNewIds() {
        return """
                [
                  {
                    "id": "IFT3000",
                    "name": "Projet logiciel avancé",
                    "credits": 3,
                    "description": "Atelier pratique de developpement.",
                    "available_terms": {
                      "autumn": true,
                      "winter": false,
                      "summer": false
                    },
                    "available_periods": {
                      "daytime": true,
                      "evening": false
                    },
                    "schedules": []
                  },
                  {
                    "id": "IFT3999",
                    "name": "Atelier d'intégration",
                    "credits": 3,
                    "description": "Integration de systemes et projet de synthese.",
                    "available_terms": {
                      "autumn": false,
                      "winter": true,
                      "summer": false
                    },
                    "available_periods": {
                      "daytime": true,
                      "evening": false
                    },
                    "schedules": []
                  }
                ]
                """;
    }

    private String detailedCourseResponse(String id, String name, String semester) {
        return """
                {
                  "id": "%s",
                  "name": "%s",
                  "description": "Description for %s",
                  "credits": 3,
                  "requirement_text": "Aucun",
                  "udemWebsite": "https://example.com/%s",
                  "schedules": [
                    {
                      "sigle": "%s",
                      "name": "%s",
                      "semester": "%s",
                      "sections": []
                    }
                  ]
                }
                """.formatted(id, name, id, id, id, name, semester);
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
