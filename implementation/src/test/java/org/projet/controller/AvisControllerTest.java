package org.projet.controller;

import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.projet.controller.AvisController.RequeteAvis;
import org.projet.model.Avis;
import org.projet.service.AvisService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Classe de tests unitaires pour AvisController.
 */
@ExtendWith(MockitoExtension.class)
class AvisControllerTest {

    private AvisController avisController;

    @Mock
    private AvisService avisService;

    @Mock
    private Context ctx;

    @BeforeEach
    void setUp() {
        avisController = new AvisController();
        // Inject the mock service into the controller using reflection
        try {
            java.lang.reflect.Field serviceField = AvisController.class.getDeclaredField("avisService");
            serviceField.setAccessible(true);
            serviceField.set(avisController, avisService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Teste la soumission d'un avis valide via soumettreAvis().
     * Vérifie que l'avis est enregistré et qu'un code 200 est retourné.
     */
    @Test
    void testSoumettreAvis_Success() {
        // Arrange
        RequeteAvis requete = new RequeteAvis();
        requete.sigleCours = "ift1234";
        requete.professeur = "Prof Test";
        requete.noteDifficulte = 3;
        requete.noteCharge = 4;
        requete.commentaire = "Excellent cours!";

        when(ctx.bodyAsClass(RequeteAvis.class)).thenReturn(requete);
        doNothing().when(avisService).enregistrerAvis(anyString(), anyString(), anyInt(), anyInt(), anyString());
        when(ctx.status(200)).thenReturn(ctx);

        avisController.soumettreAvis(ctx);

        verify(avisService).enregistrerAvis("IFT1234", "Prof Test", 3, 4, "Excellent cours!");
        verify(ctx).status(200);
        verify(ctx).result("Avis enregistré avec succès");
    }

    /**
     * Teste la soumission d'un avis invalide via soumettreAvis().
     * Vérifie qu'une IllegalArgumentException retourne un code 400 avec un message d'erreur.
     */
    @Test
    void testSoumettreAvis_IllegalArgumentException() {
        // Arrange
        RequeteAvis requete = new RequeteAvis();
        requete.sigleCours = "INVALID";
        requete.professeur = "Prof Test";
        requete.noteDifficulte = 3;
        requete.noteCharge = 4;
        requete.commentaire = "Commentaire";

        when(ctx.bodyAsClass(RequeteAvis.class)).thenReturn(requete);
        doThrow(new IllegalArgumentException("Cours inexistant"))
            .when(avisService).enregistrerAvis(anyString(), anyString(), anyInt(), anyInt(), anyString());
        when(ctx.status(400)).thenReturn(ctx);

        
        avisController.soumettreAvis(ctx);

        verify(ctx).status(400);
        verify(ctx).result("L'entrée est incorrecte. Veuillez reessayer.");
    }

    /**
     * Teste la gestion d'une erreur serveur lors de la soumission d'un avis.
     * Vérifie qu'une exception inattendue retourne un code 500.
     */
    @Test
    void testSoumettreAvis_ServerError() {
        RequeteAvis requete = new RequeteAvis();
        requete.sigleCours = "IFT1234";
        requete.professeur = "Prof Test";
        requete.noteDifficulte = 3;
        requete.noteCharge = 4;
        requete.commentaire = "Commentaire";

        when(ctx.bodyAsClass(RequeteAvis.class)).thenReturn(requete);
        doThrow(new RuntimeException("Erreur base de données"))
            .when(avisService).enregistrerAvis(anyString(), anyString(), anyInt(), anyInt(), anyString());
        when(ctx.status(500)).thenReturn(ctx);

    
        avisController.soumettreAvis(ctx);
        verify(ctx).status(500);
        verify(ctx).result("Erreur serveur : Erreur base de données");
    }

    /**
     * Teste la récupération de tous les avis via getAllAvis().
     * Vérifie que la liste d'avis est retournée avec un code 200.
     */
    @Test
    void testGetAllAvis_Success() {
        List<Avis> avisList = Arrays.asList(
            new Avis("IFT1234", "Prof A", 4, 3, "Bon cours", true),
            new Avis("IFT5678", "Prof B", 5, 2, "Très intéressant", true)
        );

        when(avisService.getAllAvis()).thenReturn(avisList);
        when(ctx.status(200)).thenReturn(ctx);

        
        avisController.getAllAvis(ctx);

        verify(avisService).getAllAvis();
        verify(ctx).status(200);
        verify(ctx).json(avisList);
    }

    /**
     * Teste la récupération de tous les avis lorsque la liste est vide.
     * Vérifie qu'un code 400 est retourné avec un message d'erreur approprié.
     */
    @Test
    void testGetAllAvis_EmptyList() {
        // Arrange
        when(avisService.getAllAvis()).thenReturn(new ArrayList<>());
        when(ctx.status(400)).thenReturn(ctx);
        when(ctx.status(200)).thenReturn(ctx);

        
        avisController.getAllAvis(ctx);

        verify(ctx).status(400);
        verify(ctx).result("Erreur : avis inexistant");
    }

    /**
     * Teste la gestion d'une IllegalArgumentException dans getAllAvis().
     * Vérifie qu'une erreur de validation retourne un code 400.
     */
    @Test
    void testGetAllAvis_IllegalArgumentException() {
        when(avisService.getAllAvis()).thenThrow(new IllegalArgumentException("Sigle invalide"));
        when(ctx.status(400)).thenReturn(ctx);

        avisController.getAllAvis(ctx);

        verify(ctx).status(400);
        verify(ctx).result("Erreur : sigle de cours invalide");
    }

    /**
     * Teste la gestion d'une erreur serveur dans getAllAvis().
     * Vérifie qu'une exception inattendue retourne un code 500.
     */
    @Test
    void testGetAllAvis_ServerError() {
        when(avisService.getAllAvis()).thenThrow(new RuntimeException("Erreur serveur"));
        when(ctx.status(500)).thenReturn(ctx);

        avisController.getAllAvis(ctx);

        verify(ctx).status(500);
        verify(ctx).result("Erreur : Erreur serveur");
    }

    /**
     * Teste la récupération des avis pour un cours spécifique via getAvisParCours().
     * Vérifie que la liste d'avis du cours est retournée avec un code 200.
     */
    @Test
    void testGetAvisParCours_Success() {
        List<Avis> avisList = Arrays.asList(
            new Avis("IFT1234", "Prof A", 4, 3, "Bon cours", true),
            new Avis("IFT1234", "Prof B", 5, 2, "Excellent", true)
        );

        when(ctx.pathParam("sigle")).thenReturn("ift1234");
        when(avisService.getAvisParCours("IFT1234")).thenReturn(avisList);
        when(ctx.status(200)).thenReturn(ctx);

        avisController.getAvisParCours(ctx);

        verify(avisService).getAvisParCours("IFT1234");
        verify(ctx).status(200);
        verify(ctx).json(avisList);
    }

    /**
     * Teste getAvisParCours() lorsque le sigle est manquant (chaîne vide).
     * Vérifie qu'un code 400 est retourné sans appeler le service.
     */
    @Test
    void testGetAvisParCours_SigleManquant() {
        when(ctx.pathParam("sigle")).thenReturn("");
        when(ctx.status(400)).thenReturn(ctx);

        avisController.getAvisParCours(ctx);

        verify(ctx).status(400);
        verify(ctx).result("Sigle du cours manquant");
        verifyNoInteractions(avisService);
    }



    /**
     * Teste getAvisParCours() lorsqu'aucun avis n'existe pour le cours.
     * Vérifie qu'un code 400 est retourné avec un message approprié.
     */
    @Test
    void testGetAvisParCours_EmptyList() {
        when(ctx.pathParam("sigle")).thenReturn("IFT1234");
        when(avisService.getAvisParCours("IFT1234")).thenReturn(new ArrayList<>());
        when(ctx.status(400)).thenReturn(ctx);
        when(ctx.status(200)).thenReturn(ctx);

        avisController.getAvisParCours(ctx);

        verify(ctx).status(400);
        verify(ctx).result("Erreur : avis inexistant");
    }

    /**
     * Teste la gestion d'une IllegalArgumentException dans getAvisParCours().
     * Vérifie qu'un sigle invalide retourne un code 400.
     */
    @Test
    void testGetAvisParCours_IllegalArgumentException() {
        when(ctx.pathParam("sigle")).thenReturn("INVALID");
        when(avisService.getAvisParCours("INVALID")).thenThrow(new IllegalArgumentException("Cours inexistant"));
        when(ctx.status(400)).thenReturn(ctx);

        avisController.getAvisParCours(ctx);

        verify(ctx).status(400);
        verify(ctx).result("Erreur : sigle de cours invalide");
    }

    /**
     * Teste la gestion d'une erreur serveur dans getAvisParCours().
     * Vérifie qu'une exception inattendue retourne un code 500.
     */
    @Test
    void testGetAvisParCours_ServerError() {
        when(ctx.pathParam("sigle")).thenReturn("IFT1234");
        when(avisService.getAvisParCours("IFT1234")).thenThrow(new RuntimeException("Erreur inattendue"));
        when(ctx.status(500)).thenReturn(ctx);

        avisController.getAvisParCours(ctx);

        verify(ctx).status(500);
        verify(ctx).result("Erreur : Erreur inattendue");
    }

    /**
     * Teste que getAvisParCours() convertit automatiquement le sigle en majuscules.
     * Vérifie qu'un sigle en minuscules est correctement transformé avant l'appel au service.
     */
    @Test
    void testGetAvisParCours_ConversionMajuscule() {
        List<Avis> avisList = Arrays.asList(
            new Avis("IFT1234", "Prof A", 4, 3, "Bon cours", true)
        );

        when(ctx.pathParam("sigle")).thenReturn("ift1234");
        when(avisService.getAvisParCours("IFT1234")).thenReturn(avisList);
        when(ctx.status(200)).thenReturn(ctx);

        avisController.getAvisParCours(ctx);

        verify(avisService).getAvisParCours("IFT1234");
        verify(ctx).json(avisList);
    }
}

