package org.projet;

import io.javalin.Javalin;

import org.projet.controller.AvisController;
import org.projet.controller.CoursController;

/**
 * Cette classe permet de définir les routes pour notre API et lancer cette dernière.
 */
public class Main {

    /**
     * Cette méthode permet de définir les routes et run l'API.
     * @param args arguments
     */
    public static void main(String[] args) {
        CoursController coursController = new CoursController();
        AvisController avisController = new AvisController();
        var app = Javalin.create().start(7070);
        // #1 Rechercher des cours 
        app.post("/cours/rechercher", coursController::rechercherCours);

        // #2 Voir les cours offerts dans un programme
        app.get("/cours-programme/{id}",coursController::getCoursesForAProgram);
        app.get("/cours-programme/nom/{nom}",coursController::foundPrograms);
        // #3 Voir les cours offerts pour un trimestre donné dans un programme ( id = id du programme)
        app.get("/programme/courseBySemester/{id}/{session}",coursController::getCourseBySemester);

        // #4 Voir l'horaire d'un cours pour un trimestre donné
        app.get("/cours/horaires/{id}/{session}",coursController::getCourseSchedule);

        // #5 Vérifier son éligibilité à un cours
        app.post("/cours/eligibilitenew", coursController::checkEligibilityNew);

        // #6 Voir les résultats académiques d'un cours
        app.post("/cours/voir/resultat", coursController::voirResultats);

        // #7 Voir les avis étudiants pour un cours donné
        app.get("/cours/{sigle}/avis", avisController::getAvisParCours);

        // #8 Soumettre un avis pour un cours 
        app.post("/avis", avisController::soumettreAvis);
        // get all avis
        app.get("/cours/avis", avisController::getAllAvis);

        // #9 Comparer des cours
        
        // #9.1 Avec les résultats agrégés
        // #9.1.1 Par rapport à la difficulté des cours (notes obtenues)
        app.post("/cours/difficulte", coursController::difficulteCours);
        // #9.1.2 Par rapport à la popularité des cours (nombre d'inscrits)
        app.post("/cours/popularite", coursController::populariteCours);
        // #9.1.3 Par rapport à la difficulté + la popularité
        app.post("/cours/comparer/stats", coursController::comparerDeuxCoursByResultats);

        // #9.2 Avec le catalogue seul
        app.post("/cours/comparer", coursController::comparerCours);
        // par difficulté avis
        app.post("/cours/comparer/avis/difficulte", coursController::comparerParDifficulteAvis);
        // par charge de travail avis.
        app.post("/cours/comparer/avis/charge", coursController::comparerParChargeAvis);
    

        // #10 Créer un ensemble de cours et générer l'horaire correspondant + détection de conflits horaires
        app.post("/horaire", coursController::genererHoraire);

        // #11 Comparer des combinaisons de cours 
        app.post("/cours/comparer/combinaison", coursController::comparerCombinaisonCours);


    }
}