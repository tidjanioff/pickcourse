package org.projet.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ResultatsTest {

    @Test
    @DisplayName("voirResultats() retourne les résultats attendus")
    void testVoirResultats(){
        // ARRANGE
        String sigleTest = "ANG1904";
        Resultats resultats = new Resultats(sigleTest);
        // ACT
        String resultatMessage = resultats.voirResultats();
        // ASSERT   
        assertEquals("Anglais 4 (niveau B1.2)", resultats.getNom());
        assertEquals("B", resultats.getMoyenne());
        assertEquals(3.58, resultats.getScore(), 0.01);
        assertEquals(8, resultats.getParticipants());
        assertEquals(5, resultats.getTrimestre());
        
    }
   @Test
    @DisplayName("isCoursPresent() doit retourner TRUE pour un cours existant (ANG1904)")
    void testCoursExistantDansCSV() {
        // ARRANGE 
        String sigleExistant = "ANG1904";
        Resultats res = new Resultats(sigleExistant);

        // ACT 
        boolean estPresent = res.isCoursPresent();

        // ASSERT 
        assertTrue(estPresent, "Le système devrait trouver le cours ANG1904 dans le CSV.");
    }

    @Test
    @DisplayName("isCoursPresent() doit retourner FALSE pour un cours inexistant (IFT9999)")
    void testCoursInexistantDansCSV() {
        // ARRANGE 
        String sigleAbsent = "IFT9999";
        Resultats res = new Resultats(sigleAbsent);

        // ACT 
        boolean estPresent = res.isCoursPresent();

        // ASSERT 
        assertFalse(estPresent, "Le système ne devrait pas trouver le cours IFT9999.");
    }

    @Test
    @DisplayName("Vérifie que le score est bien lu même si le nom du cours contient une virgule")
    void testLectureCoursAvecVirgule() {
        // ARRANGE
        String sigleComplexe = "NUT1970";
        Resultats res = new Resultats(sigleComplexe);

        // ACT
        double scoreObtenu = res.getScore();

        // ASSERT
        
        assertEquals(3.42, scoreObtenu, 0.01);
    }

   @Test
@DisplayName("voirResultats() doit retourner un message convivial si le cours est absent")
void testVoirResultatsCoursInexistant() {
    //  ARRANGE 
    String sigleInconnu = "FAKE999";
    Resultats resultats = new Resultats(sigleInconnu);

    // ACT 
    String message = resultats.voirResultats();

    // ASSERT 
    
    assertFalse(resultats.isCoursPresent(), "Le cours ne devrait pas être considéré comme présent.");
    
    // On vérifie que le message contient les mots clés de ton message d'erreur
    assertTrue(message.contains("Désolé"), "Le message devrait commencer par une excuse.");
    assertTrue(message.contains(sigleInconnu), "Le message devrait mentionner le sigle cherché.");
}





    }