package org.projet.service;

import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projet.model.Avis;
import org.projet.repository.AvisRepository;
import org.projet.repository.CoursRepository;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
class AvisServiceTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("pickcourse")
            .withUsername("pickcourse")
            .withPassword("devpassword");

    private AvisRepository avisRepository;

    @BeforeEach
    void setUp() throws Exception {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .load()
                .migrate();

        Jdbi jdbi = Jdbi.create(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        jdbi.useHandle(handle -> handle.execute("TRUNCATE TABLE reviews RESTART IDENTITY"));
        avisRepository = new AvisRepository(jdbi);

        CoursRepository mockRepo = mock(CoursRepository.class);
        when(mockRepo.getAllCoursesId()).thenReturn(Optional.of(List.of("IFT2255")));

        Field instanceField = CoursService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        CoursService coursService = CoursService.getInstance();
        coursService.setCoursRepository(mockRepo);
    }

    @Test
    void enregistrerAvisPropageLesErreursDeValidation() {
        AvisService service = new AvisService(avisRepository);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.enregistrerAvis("IFT9999", "Prof Test", 3, 4, "Commentaire")
        );
    }

    @Test
    void enregistrerAvisPersisteDansPostgres() {
        AvisService service = new AvisService(avisRepository);

        service.enregistrerAvis("IFT2255", "Prof Test", 3, 4, "Excellent cours");

        List<Avis> avis = service.getAvisParCours("IFT2255");
        assertEquals(1, avis.size());
        assertEquals("IFT2255", avis.get(0).getSigleCours());
        assertEquals("Prof Test", avis.get(0).getNomProfesseur());
        assertEquals(3, avis.get(0).getNoteDifficulte());
        assertEquals(4, avis.get(0).getNoteChargeTravail());
        assertEquals("Excellent cours", avis.get(0).getCommentaire());
    }

    @Test
    void getAllAvisRetourneLesAvisPostgres() {
        AvisService service = new AvisService(avisRepository);

        service.enregistrerAvis("IFT2255", "Prof A", 2, 5, "Avis A");
        service.enregistrerAvis("IFT2255", "Prof B", 4, 1, "Avis B");

        List<Avis> avis = service.getAllAvis();
        assertEquals(2, avis.size());
        assertEquals("Avis A", avis.get(0).getCommentaire());
        assertEquals("Avis B", avis.get(1).getCommentaire());
    }
}
