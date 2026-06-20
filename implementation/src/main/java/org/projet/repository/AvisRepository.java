package org.projet.repository;

import org.jdbi.v3.core.Jdbi;
import org.projet.config.DatabaseConfig;
import org.projet.model.Avis;

import java.util.List;

/**
 * Repository PostgreSQL pour les avis étudiants.
 */
public class AvisRepository {
    private final Jdbi jdbi;

    public AvisRepository() {
        this(DatabaseConfig.createJdbi());
    }

    public AvisRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void insert(Avis avis) {
        jdbi.useHandle(handle -> handle.createUpdate("""
                        INSERT INTO reviews (
                            commentaire,
                            note_difficulte,
                            sigle_cours,
                            valide,
                            note_charge_travail,
                            nom_professeur
                        )
                        VALUES (
                            :commentaire,
                            :noteDifficulte,
                            :sigleCours,
                            :valide,
                            :noteChargeTravail,
                            :nomProfesseur
                        )
                        """)
                .bind("commentaire", avis.getCommentaire())
                .bind("noteDifficulte", avis.getNoteDifficulte())
                .bind("sigleCours", avis.getSigleCours())
                .bind("valide", avis.isValide())
                .bind("noteChargeTravail", avis.getNoteChargeTravail())
                .bind("nomProfesseur", avis.getNomProfesseur())
                .execute());
    }

    public List<Avis> findAll() {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT
                            commentaire,
                            note_difficulte,
                            sigle_cours,
                            valide,
                            note_charge_travail,
                            nom_professeur
                        FROM reviews
                        ORDER BY id
                        """)
                .map((rs, ctx) -> mapAvis(rs))
                .list());
    }

    public List<Avis> findBySigleCours(String sigleCours) {
        return jdbi.withHandle(handle -> handle.createQuery("""
                        SELECT
                            commentaire,
                            note_difficulte,
                            sigle_cours,
                            valide,
                            note_charge_travail,
                            nom_professeur
                        FROM reviews
                        WHERE sigle_cours = :sigleCours
                        ORDER BY id
                        """)
                .bind("sigleCours", sigleCours)
                .map((rs, ctx) -> mapAvis(rs))
                .list());
    }

    private Avis mapAvis(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Avis(
                rs.getString("sigle_cours"),
                rs.getString("nom_professeur"),
                rs.getInt("note_charge_travail"),
                rs.getInt("note_difficulte"),
                rs.getString("commentaire"),
                rs.getBoolean("valide")
        );
    }
}
