package org.projet.controller;

import io.javalin.http.Context;
import org.projet.model.Avis;
import org.projet.model.Cours;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

public class CoursControllerTest {

    
    @Test
    @DisplayName("Comparaison avec cours invalides")
    void testComparerCours_withInvalidCourseIds() {
        CoursController controller = new CoursController();
        Context ctx = mock(Context.class);
        CoursController.RequeteComparaison req = new CoursController.RequeteComparaison();
        req.cours = new String[]{"INVALID1", "INVALID2"};
        req.criteres = new String[]{"name", "credits"};
        when(ctx.bodyAsClass(CoursController.RequeteComparaison.class)).thenReturn(req);

        controller.comparerCours(ctx);
        verify(ctx).status(400);
    }


    //Tests pour rechercherCours

    @Test
    @DisplayName("Recherche de cours par ID valide")
    void testRechercherCours_byValidId() {
        CoursController controller = new CoursController();
        Context ctx = mock(Context.class);
        CoursController.RequeteRecherche req = new CoursController.RequeteRecherche();
        req.param = "id";
        req.valeur = "IFT1025";
        req.includeSchedule = "false";
        req.semester = null;

        when(ctx.bodyAsClass(CoursController.RequeteRecherche.class)).thenReturn(req);

        controller.rechercherCours(ctx);

        verify(ctx).status(anyInt());
    }

    @Test
    @DisplayName("Recherche de cours par nom")
    void testRechercherCours_byName() {
        CoursController controller = new CoursController();
        Context ctx = mock(Context.class);
        CoursController.RequeteRecherche req = new CoursController.RequeteRecherche();
        req.param = "name";
        req.valeur = "Algorithmic";
        req.includeSchedule = "false";
        req.semester = null;

        when(ctx.bodyAsClass(CoursController.RequeteRecherche.class)).thenReturn(req);

        controller.rechercherCours(ctx);

        verify(ctx).status(anyInt());
    }

    @Test
    @DisplayName("Recherche de cours par description")
    void testRechercherCours_byDescription() {
        CoursController controller = new CoursController();
        Context ctx = mock(Context.class);
        CoursController.RequeteRecherche req = new CoursController.RequeteRecherche();
        req.param = "description";
        req.valeur = "fundamentals";
        req.includeSchedule = "false";
        req.semester = null;

        when(ctx.bodyAsClass(CoursController.RequeteRecherche.class)).thenReturn(req);

        controller.rechercherCours(ctx);

        verify(ctx).status(anyInt());
    }

    @Test
    @DisplayName("Recherche avec paramètre invalide")
    void testRechercherCours_byInvalidParam() {
        CoursController controller = new CoursController();
        Context ctx = mock(Context.class);
        CoursController.RequeteRecherche req = new CoursController.RequeteRecherche();
        req.param = "invalid_param";
        req.valeur = "value";
        req.includeSchedule = "false";
        req.semester = null;

        when(ctx.bodyAsClass(CoursController.RequeteRecherche.class)).thenReturn(req);

        controller.rechercherCours(ctx);

        verify(ctx, atLeastOnce()).status(anyInt());
    }

    @Test
    @DisplayName("Recherche avec schedule et semester")
    void testRechercherCours_withScheduleAndSemester() {
        CoursController controller = new CoursController();
        Context ctx = mock(Context.class);
        CoursController.RequeteRecherche req = new CoursController.RequeteRecherche();
        req.param = "id";
        req.valeur = "IFT1025";
        req.includeSchedule = "true";
        req.semester = "FALL";

        when(ctx.bodyAsClass(CoursController.RequeteRecherche.class)).thenReturn(req);

        controller.rechercherCours(ctx);

        verify(ctx).status(anyInt());
    }

    //Tests pour checkEligibility

    @Test
    @DisplayName("Vérification d'éligibilité avec cours et prérequis valides")
    void testCheckEligibility_validCourseAndPrerequisites() {
        CoursController controller = new CoursController();
        Context ctx = mock(Context.class);
        CoursController.RequeteEligibilite req = new CoursController.RequeteEligibilite();
        req.idCours = "IFT2255";
        req.listeCours = List.of("IFT1025", "IFT1030");

        when(ctx.bodyAsClass(CoursController.RequeteEligibilite.class)).thenReturn(req);

        controller.checkEligibility(ctx);

        verify(ctx).json(any());
    }

    @Test
    @DisplayName("Vérification d'éligibilité avec cours invalide")
    void testCheckEligibility_invalidCourse() {
        CoursController controller = new CoursController();
        Context ctx = mock(Context.class);
        CoursController.RequeteEligibilite req = new CoursController.RequeteEligibilite();
        req.idCours = "INVALID_COURSE";
        req.listeCours = List.of("IFT1025");

        when(ctx.bodyAsClass(CoursController.RequeteEligibilite.class)).thenReturn(req);

        controller.checkEligibility(ctx);

        verify(ctx).json(any());
    }

    @Test
    @DisplayName("Vérification d'éligibilité avec liste de prérequis vide")
    void testCheckEligibility_emptyPrerequisites() {
        CoursController controller = new CoursController();
        Context ctx = mock(Context.class);
        CoursController.RequeteEligibilite req = new CoursController.RequeteEligibilite();
        req.idCours = "IFT2255";
        req.listeCours = List.of();

        when(ctx.bodyAsClass(CoursController.RequeteEligibilite.class)).thenReturn(req);

        controller.checkEligibility(ctx);

        verify(ctx).json(any());
    }



}
