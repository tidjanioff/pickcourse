package org.projet.repository;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.flywaydb.core.Flyway;
import org.projet.model.Avis;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class AvisRepositoryTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("pickcourse")
            .withUsername("pickcourse")
            .withPassword("devpassword");

    private AvisRepository repository;

    @BeforeEach
    void setUp() {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .load()
                .migrate();

        Jdbi jdbi = Jdbi.create(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        jdbi.useHandle(handle -> handle.execute("TRUNCATE TABLE reviews RESTART IDENTITY"));
        repository = new AvisRepository(jdbi);
    }

    @Test
    void insertEtFindAll() {
        repository.insert(new Avis("IFT2255", "Prof Test", 4, 3, "Bon cours", true));

        List<Avis> avis = repository.findAll();

        assertEquals(1, avis.size());
        assertEquals("IFT2255", avis.get(0).getSigleCours());
        assertEquals("Prof Test", avis.get(0).getNomProfesseur());
        assertEquals(4, avis.get(0).getNoteChargeTravail());
        assertEquals(3, avis.get(0).getNoteDifficulte());
        assertEquals("Bon cours", avis.get(0).getCommentaire());
    }

    @Test
    void findBySigleCoursFiltreLesResultats() {
        repository.insert(new Avis("IFT2255", "Prof A", 4, 3, "Avis A", true));
        repository.insert(new Avis("IFT1025", "Prof B", 2, 5, "Avis B", true));

        List<Avis> avis = repository.findBySigleCours("IFT2255");

        assertEquals(1, avis.size());
        assertEquals("Avis A", avis.get(0).getCommentaire());
    }
}
