package org.projet.repository;

import org.projet.model.Cours;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CoursRepositoryTest {

    CoursRepository repo = CoursRepository.getInstance();

    @Test
    @DisplayName("getAllCoursesId() retourne une liste non vide")
    void testGetAllCoursesIdNonVide() throws Exception {
        Optional<List<String>> opt = repo.getAllCoursesId();

        assertTrue(opt.isPresent(), "L'Optional doit être présent");
        assertFalse(opt.get().isEmpty(), "La liste doit contenir au moins un cours");
    }

    @Test
    @DisplayName("getAllCoursesId() contient IFT2255")
    void testGetAllCoursesIdContainsIFT2255() throws Exception {
        Optional<List<String>> opt = repo.getAllCoursesId();

        assertTrue(opt.isPresent());
        List<String> ids = opt.get();

        assertTrue(ids.contains("IFT2255"), "IFT2255 devrait être présent");
    }

    @Test
    @DisplayName("getAllCoursesId() ne contient pas de doublons")
    void testGetAllCoursesIdPasDeDoublons() throws Exception {
        List<String> ids = repo.getAllCoursesId().orElseThrow();

        Set<String> uniques = new HashSet<>(ids);

        assertEquals(
                uniques.size(), ids.size(),
                "Il ne devrait pas y avoir de doublons"
        );
    }


    @Test
    @DisplayName("getCourseBy(id) retourne une liste contenant le cours demandé")
    void testGetCourseByIdIFT1025() throws Exception {

        Optional<List<Cours>> opt =
                repo.getCourseBy("id", "IFT1025", null, null);

        assertTrue(opt.isPresent(), "L'Optional doit être présent");

        List<Cours> list = opt.get();
        assertEquals(1, list.size(), "La recherche par id doit retourner un seul Cours");

        Cours cours = list.get(0);
        assertEquals("IFT1025", cours.getId());
    }

    @Test
    @DisplayName("getCourseBy(id) retourne un nom cohérent pour IFT1025")
    void testGetCourseByIdCheckName() throws Exception {

        List<Cours> list =
                repo.getCourseBy("id", "IFT1025", null, null)
                        .orElseThrow();

        Cours cours = list.get(0);

        assertEquals("Programmation 2", cours.getName());
    }

    @Test
    @DisplayName("getCourseBy(id) retourne Optional.empty() pour un id inexistant")
    void testGetCourseByIdCoursInexistant() throws Exception {

        Optional<List<Cours>> opt =
                repo.getCourseBy("id", "TIDJANI45", null, null);

        assertTrue(opt.isEmpty(), "Un id inexistant doit retourner Optional.empty()");
    }

    @Test
    @DisplayName("getCourseBy('name') retourne une liste de cours par recherche de mot-clé")
    void testGetCourseByName() throws Exception {
        // Teste la recherche par NOM avec un mot-clé courant
        Optional<List<Cours>> opt = repo.getCourseBy("name", "Programmation", "false", null);

        assertTrue(opt.isPresent(), "La recherche par nom devrait retourner un résultat");
        assertFalse(opt.get().isEmpty(), "La liste ne devrait pas être vide");

        // Vérifie le contenu
        assertTrue(opt.get().get(0).getName().contains("programmation"));
    }

    @Test
    @DisplayName("getCourseEligibility() retourne une réponse JSON valide (Requête POST)")
    void testGetCourseEligibility() throws Exception {
        // Teste la connexion réelle à l'API Planifium
        String jsonReponse = repo.getCourseEligibility("IFT2255", List.of("IFT1025"));

        assertNotNull(jsonReponse, "La réponse de l'API ne doit pas être null");
        assertTrue(jsonReponse.contains("eligible"), "Le JSON retourné devrait contenir le champ 'eligible'");
    }

    // test d'invariance
    @Test
    @DisplayName("La méthode getCoursById() retourne bien le cours recherché")
    void testGetCoursByIdCoursIFT1025() throws Exception {
        Optional<List<Cours>> optListe = repo.getCourseBy("id", "IFT1025","null", "null");
        assertTrue(optListe.isPresent(), "Ça devrait retourner un objet Cours");
        Cours cours = optListe.get().get(0);
        assertTrue( cours.getId().equals("IFT1025"));

    }

}
