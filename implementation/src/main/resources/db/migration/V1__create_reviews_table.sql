CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    commentaire TEXT NOT NULL,
    note_difficulte INTEGER NOT NULL CHECK (note_difficulte BETWEEN 1 AND 5),
    sigle_cours VARCHAR(16) NOT NULL,
    valide BOOLEAN NOT NULL DEFAULT TRUE,
    note_charge_travail INTEGER NOT NULL CHECK (note_charge_travail BETWEEN 1 AND 5),
    nom_professeur TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_reviews_sigle_cours ON reviews (sigle_cours);
