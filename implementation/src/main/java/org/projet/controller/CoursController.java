package org.projet.controller;

import io.javalin.http.Context;

import org.projet.model.Cours;
import org.projet.model.Resultats;
import org.projet.service.CoursService;

import java.util.*;

/**
 * Cette classe permet de gérer les requêtes des utilisateurs relatives à la manipulation de cours.
 */
public class CoursController {
    // CoursService est un Singleton, donc on récupère l'instance existante.
    private CoursService coursService =CoursService.getInstance();

    /**
     * Cette méthode permet de vérifier l'éligibilité à un cours ( sans cycle).
     * @param ctx requête + réponse.
     */
    public void checkEligibility(Context ctx){
        RequeteEligibilite req = ctx.bodyAsClass(RequeteEligibilite.class);
        String resultat = coursService.checkEligibility(req.idCours,req.listeCours);
        ctx.json(resultat);
    }


    /**
     * Cette méthode permet de gérer la requête de recherche de cours.
     * @param ctx requête + notre réponse.
     */

    public void rechercherCours(Context ctx){
        RequeteRecherche req = ctx.bodyAsClass(RequeteRecherche.class);
        Optional<List<Cours>> resultat = coursService.rechercherCours(req.param,req.valeur,req.includeSchedule,req.semester);
        if(resultat.isPresent()){
            ctx.status(200);
            ctx.json(resultat.get());
        }else{
            ctx.status(404);
            ctx.json("Cours pas trouvé. Veuillez reessayer. Pour rappel, les paramètres possibles sont id, name et description.");
        }

    }
    /**
     * Cette méthode permet de traiter la requête de l'utilisateur relative à la comparaison de cours.
     * @param ctx le contexte javalin qui contient la requête HTTP de l'utilisateur ainsi que notre réponse.
     */

   public void comparerCours(Context ctx) {
       /* cette ligne de code map le body de la requête avec un objet Java. Cela permet de
       récupérer la liste de cours et celle des critères.
       */

        try {
            RequeteComparaison req = ctx.bodyAsClass(RequeteComparaison.class);
            List<List<String>> resultat =
                    coursService.comparerCours(req.cours, req.criteres, req.session);

            if (resultat == null) {
                ctx.status(400);
                ctx.json("Requête invalide");
                return;
            }

            ctx.status(200);
            ctx.json(resultat);

        } catch (RuntimeException e) {
            ctx.status(400);
            ctx.json("Requête invalide");
        } catch (Exception e) {
            ctx.status(404);
            ctx.json("Contexte invalide");
        }
    }
    /**
     * Cette méthode permet de comparer des ensembles de cours.
     * @param ctx
     */
    public void comparerCombinaisonCours(Context ctx){
        RequeteComparaisonCombinaison req = ctx.bodyAsClass(RequeteComparaisonCombinaison.class);

        try {
            List<List<String>> resultat =
                    coursService.comparerCombinaisonCours(req.listeCours, req.session);
            ctx.status(200);
            ctx.json(resultat);
        } catch (RuntimeException e) {
            ctx.status(400);
            ctx.json(new ArrayList<>());
        }
    }

    /**
     * Cette méthode permet de gérer la requête utilisateur pour la comparaison basée sur la difficulté pour les avis.
     * @param ctx
     */
    public void comparerParDifficulteAvis(Context ctx){
        RequeteComparaisonAvis req = ctx.bodyAsClass(RequeteComparaisonAvis.class);
        try{
            List<List<String>> resultat = coursService.comparerCoursParNoteDifficulteAvis(req.idsCours);
            ctx.status(200);
            ctx.json(resultat);
        }catch(RuntimeException e){
            ctx.status(400);
            ctx.json("Des avis pour ces cours n'ont pas été trouvés.");
        }
    }

    /**
     * Cette méthode permet de gérer la requête utilisateur pour la comparaison basée sur la difficulté pour les avis.
     * @param ctx
     */
    public void comparerParChargeAvis(Context ctx){
        RequeteComparaisonAvis req = ctx.bodyAsClass(RequeteComparaisonAvis.class);
        try{
            List<List<String>> resultat = coursService.comparerCoursParChargeTravailAvis(req.idsCours);
            ctx.status(200);
            ctx.json(resultat);
        }catch(RuntimeException e){
            ctx.status(400);
            ctx.json("Des avis pour ces cours n'ont pas été trouvés.");
        }
    }

    /**
     * Cette méthode vérifie l’éligibilité d’un étudiant à un cours donné.
     * Le controller délègue la logique métier au {@code CoursService} et
     * retourne un message indiquant si l’étudiant est éligible ou non.
     *
     * @param ctx contexte HTTP Javalin contenant la requête JSON
     */
    public void checkEligibilityNew(Context ctx){
        RequeteEligibiliteNew req = ctx.bodyAsClass(RequeteEligibiliteNew.class);
        String resultat = coursService.checkEligibilityNew(req.idCours,req.listeCours, req.cycle);
        ctx.json(resultat);
    }


    /**
     * Cette méthode génère toutes les combinaisons d’horaires possibles pour un ensemble de cours.
     * Si l’option {@code sections} est activée, seuls les horaires correspondant
     * aux sections choisies sont retournés.
     *
     * @param ctx contexte HTTP Javalin contenant la requête JSON
     */
    public void genererHoraire(Context ctx) {
        try {
            RequeteHoraire req = ctx.bodyAsClass(RequeteHoraire.class);

            Map<String, Map<String, Map<String, List<List<String>>>>> horaires =
                    coursService.genererEnsembleHoraire(req.idCours, req.session);

            Object reponse;

            if (Boolean.TRUE.equals(req.sections)) {
                reponse = coursService.appliquerChoix(horaires, req.choix);
            } else {
                reponse = horaires;
            }

            ctx.status(200);
            ctx.json(reponse);

        } catch (Exception e) {
            ctx.status(200);
            ctx.json(new HashMap<>());
        }
    }

    /**
     * Cette méthode permet de trouver les programmes par nom
     * @param ctx requête + notre réponse.
     */
    public void foundPrograms(Context ctx){
        String nom = ctx.pathParam("nom");
        List<String> details = coursService.foundProgramms(nom);

        if (details.isEmpty()) {
            ctx.status(404).json(Map.of("error", "Les paramètres fournis sont invalides ou le programme n'existe pas."));
            return;
        }

        ctx.status(200).json(details);
    }
    /**
     * Cette methode permet d'obtenir les cours offerts dans un programme donne.
     * @param ctx ID du programme.
     **/
    public void getCoursesForAProgram(Context ctx){
        String id = ctx.pathParam("id");
        List<String> details = coursService.getCoursesForAProgram(id);

        if (details.isEmpty()) {
            ctx.status(404).json(Map.of("error", "Les paramètres fournis sont invalides ou le programme n'existe pas."));
            return;
        }

        ctx.status(200).json(details);
    }

    /**
     * Cette methode permet d'obtenir la liste des cours disponible pour un trimestre donnee dans un programme.
     * @param ctx ID du programme dans lequel il faut effectuer la recherche et
     *            du trimestre pour laquelle on effectue la recherche.
     **/
    public void getCourseBySemester(Context ctx){
        String id = ctx.pathParam("id");
        String session = ctx.pathParam("session");

        List<String> details = coursService.getCourseBySemester(session,id);

        if (details.isEmpty()) {
            ctx.status(404).json(Map.of("error","Les paramètres fournis sont invalides ou le programme n'existe pas ou le cours n'existe pas."));
            return;
        }

        ctx.status(200).json(details);
    }

    /**
     * Cette methode permet d'obtenir l'horaire d'un cours pour un trimestre donné.
     * @param ctx ID du cours
     * et le trimestre pour lequel on désire obtenir l'horaire.
     **/
    public void getCourseSchedule(Context ctx){
        String courseID  = ctx.pathParam("id"); //ID du cours.
        String session = ctx.pathParam("session");  //Trimestre pour lequel on effectue la recherche.

        Map<String,Map<String,Object>> details = coursService.getCourseScheduleMap(courseID,session);

        if (details.isEmpty()) {
            ctx.status(404).json(Map.of("error","Les paramètres fournis sont invalides ou le programme n'existe pas ou le cours n'existe pas."));
            return;
        }
        ctx.status(200).json(details);
    }

    /**
     * Analyse la difficulté du cours envoyé via JSON.
     * @param ctx Contexte Javalin.
     */
    public void difficulteCours(Context ctx) {
        RequeteUnCours req = ctx.bodyAsClass(RequeteUnCours.class);
        Resultats res = coursService.getResultats(req.sigle);
        String difficulte = coursService.difficulteCours(res);
        ctx.json(difficulte);
    }


    /**
     * Analyse la popularité du cours envoyé via JSON.
     * @param ctx Contexte Javalin.
     */
    public void populariteCours(Context ctx) {
        RequeteUnCours req = ctx.bodyAsClass(RequeteUnCours.class);
        Resultats res = coursService.getResultats(req.sigle);
        String popularite = coursService.populariteCours(res);
        ctx.json(popularite);
    }


    /**
     * Cette méthode permet de comparer les statistiques de deux cours.
     * @param ctx le contexte javalin qui contient la requête HTTP de l'utilisateur ainsi que notre réponse.
     */
    public void comparerDeuxCoursByResultats(Context ctx) {
        RequeteDeuxCours req = ctx.bodyAsClass(RequeteDeuxCours.class);

        Resultats res1 = coursService.getResultats(req.sigle1);
        Resultats res2 = coursService.getResultats(req.sigle2);


        Map<String, String> reponses = new HashMap<>();
        reponses.put("popularite", coursService.comparerPopularite(res1, res2));
        reponses.put("difficulte", coursService.comparerDifficulte(res1, res2));

        ctx.json(reponses);
    }




    /**
     * Récupère et renvoie les résultats d'un cours au format JSON.
     * @param ctx Le contexte de la requête HTTP.
     */
    public void voirResultats(Context ctx) {
        RequeteResultats req = ctx.bodyAsClass(RequeteResultats.class);
        Resultats res = coursService.getResultats(req.sigle);
        String message = res.voirResultats();
        ctx.json(message);
    }
    /**
     * Cette classe permet de parser le json du body de la requête comparaisonStats. La classe est interne donc
     * on peut déclarer les attributs publics.
     */
    public static class RequeteDeuxCours{
        /** Sigle du premier cours à comparer. */
        public String sigle1;

        /** Sigle du second cours à comparer. */
        public String sigle2;
    }

    /**
     * Cette classe permet de parser le json du body de la requête stats. La classe est interne donc
     * on peut déclarer les attributs publics.
     */
    public static class RequeteStats {
        /** Sigle du cours concerné par l’analyse statistique. */
        public String sigle;
    }


    /**
     * Cette classe parse le json du body de la requête Resultat.
     */
    public static class RequeteResultats {
        /** Sigle du cours dont on souhaite consulter les résultats académiques. */
        public String sigle;
    }
    /**
     * Cette classe permet de parser le json du body de la requête difficulte ou popularite. La classe est interne donc
     * on peut déclarer les attributs publics.
     */
    public static class RequeteUnCours{
        /** Sigle du cours à analyser. */
        public String sigle;
    }

    /**
     * Requête utilisée pour générer les horaires possibles d’un ensemble de cours.
     */
    public static class RequeteHoraire {
        /** Liste des identifiants de cours sélectionnés. */
        public List<String> idCours;

        /** Session académique concernée (ex : A25, H26). */
        public String session;

        /** Indique si des sections spécifiques sont sélectionnées. */
        public Boolean sections;

        /** Choix des sections pour chaque cours. */
        public Map<String, Map<String, String>> choix;
    }

    /**
     * Permet de parser le body de la requête de comparaison par avis.
     */
    public static class RequeteComparaisonAvis{
        /** Tableau des sigles de cours à comparer à partir des avis. */
        public String[] idsCours;
    }
    /**
     * Cette classe permet de parser le json du body de la requête comparaison. La classe est interne donc
     * on peut déclarer les attributs publics.
     */
    public static class RequeteComparaison {
        /** Liste des cours à comparer. */
        public String[] cours;

        /** Critères de comparaison sélectionnés. */
        public String[] criteres;

        /** Session académique concernée par la comparaison. */
        public String session;
    }

    /**
     * Cette classe permet de parser le json du body de la requête recherche. La classe est interne donc
     * on peut déclarer les attributs publics.
     */

    public static class RequeteRecherche{
        /** Paramètre de recherche (id, name ou description). */
        public String param;

        /** Valeur associée au paramètre de recherche. */
        public String valeur;

        /** Indique si l’horaire doit être inclus dans les résultats. */
        public String includeSchedule;

        /** Trimestre académique ciblé par la recherche. */
        public String semester;
    }
    /**
     * Cette classe permet de parser le json du body de la requête eligibilite. La classe est interne donc
     * on peut déclarer les attributs publics.
     */
    public static class RequeteEligibilite{
        /** Identifiant du cours à vérifier. */
        public String idCours;

        /** Liste des cours déjà complétés par l’étudiant. */
        public List<String> listeCours;
    }

    /**
     * Cette classe permet de parser le json du body de la requête comparaisonCombinaison. La classe est interne donc
     * on peut déclarer les attributs publics.
     */
    public static class RequeteComparaisonCombinaison{
        /** Liste de combinaisons de cours à comparer. */
        public List<List<String>> listeCours;

        /** Session académique concernée. */
        public String session;
    }




    /**
     * Requête utilisée pour vérifier l’éligibilité d’un étudiant à un cours,
     * en tenant compte des cours complétés et du cycle d’études.
     */
    public static class RequeteEligibiliteNew{
        /** Identifiant du cours à vérifier. */
        public String idCours;

        /** Liste des cours déjà complétés par l’étudiant. */
        public List<String> listeCours;

        /** Cycle d’études de l’étudiant (ex : 1, 2). */
        public Integer cycle;
    }


}
