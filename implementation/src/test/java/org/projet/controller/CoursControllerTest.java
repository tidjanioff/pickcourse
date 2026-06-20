package org.projet.controller;

import io.javalin.http.Context;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.projet.service.CoursService;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("Comparaison des avis par difficulté")
    void testComparerParAvisDifficulte() {
        CoursController controller = new CoursController();
        CoursService service = mock(CoursService.class);
        injectCoursService(controller, service);

        Context ctx = mock(Context.class);
        CoursController.RequeteComparaisonAvis req = new CoursController.RequeteComparaisonAvis();
        req.idsCours = new String[]{"IFT2255", "IFT1025"};
        req.critere = "difficulte";
        List<List<String>> resultat = List.of(
                List.of("IFT2255", "3.50"),
                List.of("IFT1025", "2.50")
        );

        when(ctx.bodyAsClass(CoursController.RequeteComparaisonAvis.class)).thenReturn(req);
        when(service.comparerCoursParAvis(req.idsCours, "difficulte")).thenReturn(resultat);

        controller.comparerParAvis(ctx);

        verify(ctx).status(200);
        verify(ctx).json(resultat);
    }

    @Test
    @DisplayName("Comparaison des avis par charge de travail")
    void testComparerParAvisCharge() {
        CoursController controller = new CoursController();
        CoursService service = mock(CoursService.class);
        injectCoursService(controller, service);

        Context ctx = mock(Context.class);
        CoursController.RequeteComparaisonAvis req = new CoursController.RequeteComparaisonAvis();
        req.idsCours = new String[]{"IFT2255", "IFT1025"};
        req.critere = "charge";
        List<List<String>> resultat = List.of(
                List.of("IFT2255", "4.00"),
                List.of("IFT1025", "2.00")
        );

        when(ctx.bodyAsClass(CoursController.RequeteComparaisonAvis.class)).thenReturn(req);
        when(service.comparerCoursParAvis(req.idsCours, "charge")).thenReturn(resultat);

        controller.comparerParAvis(ctx);

        verify(ctx).status(200);
        verify(ctx).json(resultat);
    }

    @Test
    @DisplayName("Comparaison des avis refuse un critère invalide")
    void testComparerParAvisCritereInvalide() {
        CoursController controller = new CoursController();
        CoursService service = mock(CoursService.class);
        injectCoursService(controller, service);

        Context ctx = mock(Context.class);
        CoursController.RequeteComparaisonAvis req = new CoursController.RequeteComparaisonAvis();
        req.idsCours = new String[]{"IFT2255"};
        req.critere = "popularite";

        when(ctx.bodyAsClass(CoursController.RequeteComparaisonAvis.class)).thenReturn(req);

        controller.comparerParAvis(ctx);

        verify(ctx).status(400);
        verify(ctx).json("Critère invalide. Les valeurs possibles sont difficulte et charge.");
        verifyNoInteractions(service);
    }

    private void injectCoursService(CoursController controller, CoursService service) {
        try {
            Field field = CoursController.class.getDeclaredField("coursService");
            field.setAccessible(true);
            field.set(controller, service);
        } catch (Exception e) {
            fail(e);
        }
    }
}
