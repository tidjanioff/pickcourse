package org.projet.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AvisTest {

    @Test
    void constructeurConserveLaNoteChargeTravail() {
        Avis avis = new Avis("IFT2255", "Prof Test", 4, 2, "Bon cours", true);

        assertEquals(4, avis.getNoteChargeTravail());
        assertEquals(2, avis.getNoteDifficulte());
    }
}
