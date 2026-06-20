package org.projet.service;

import org.projet.model.Avis;
import org.projet.repository.AvisRepository;

import java.util.List;
/**
 * Service responsable de la gestion des avis étudiants.
 * Il permet de valider, enregistrer et récupérer les avis
 * associés aux cours.
 */
public class AvisService {
    private static AvisService instance;
    private final AvisRepository avisRepository;

    /**
     * Cette méthode permet d'initialiser le repository d'avis lors de l'utilisation de l'instance
     * de AvisService.
     */
    private AvisService(){
        this(new AvisRepository());
    }

    AvisService(AvisRepository avisRepository) {
        this.avisRepository = avisRepository;
    }

    /**
     * Cette méthode permet de récupérer l'instance de AvisService dans le cadre du patron de création Singleton.
     * @return l'instance de AvisService.
     */
    public static AvisService getInstance(){
        if(instance == null){
            instance =  new AvisService();
        }
        return instance;

    }

    /**
     * Cette méthode permet de valider l'Avis reçu du bot. A noter que ce dernier fait déjà une vérification
     * minimale à savoir si le sigle est du bon format, le commentaire ne contient pas d'insultes,
     * et les notes sont des entiers entre 0 et 5.
     * @param sigle  id du cours.
     * @param nomProf nom du professeur ( optionnel)
     * @param noteDifficulte  estimation du niveau de difficulté du cours
     * @param noteQualite    estimation de la qualité du cours
     * @param commentaire   commentaire subjectif.
     */
    public void validateAvis(String sigle, String nomProf,
                             int noteDifficulte, int noteQualite,
                             String commentaire) {

        // Si id de cours n'existe pas, erreur.
        if (!CoursService.getInstance().validateIdCours(sigle)) {
            throw new IllegalArgumentException("Cours inexistant");
        }
        // On ne devrait pas faire confiance à une entité externe, donc même si le bot se charge de cette vérification,
        // on la refait à nouveau.
        if (noteDifficulte < 1 || noteDifficulte > 5) {
            throw new IllegalArgumentException("Note difficulté invalide");
        }

        if (noteQualite < 1 || noteQualite > 5) {
            throw new IllegalArgumentException("Note qualité invalide");
        }
        // S'il y a pas de commentaire, erreur.
        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new IllegalArgumentException("Commentaire vide");
        }

//        if (commentaire.length() > 500) {
//            throw new IllegalArgumentException("Commentaire trop long");
//        }
    }


    /**
     * Cette méthode permet d'enregistrer l'avis localement.
     * synchronised pour gérer le cas où plusieurs utilisateurs envoient des avis en même temps.
     * @param sigle sigle du cours
     * @param prof nom du prof
     * @param noteDifficulte note de la difficulté
     * @param noteQualite note de la qualité.
     * @param commentaire  commentaire subjectif.
     */

    public synchronized void enregistrerAvis(String sigle, String prof,
                                             int noteDifficulte, int noteQualite,
                                             String commentaire) {
        this.validateAvis(sigle, prof, noteDifficulte, noteQualite, commentaire);

        Avis avis = new Avis(
                sigle,
                prof,
                noteQualite,
                noteDifficulte,
                commentaire,
                true
        );

        avisRepository.insert(avis);
    }


    /**
     * Cette méthode permet de récupérer la liste des avis associés à un cours.
     * @param sigle  sigle du cours
     * @return  retourne la liste d'avis associée au cours donné.
     */
    public List<Avis> getAvisParCours(String sigle)  {
        // le id du cours doit être valide avant la recherche.
        if (!CoursService.getInstance().validateIdCours(sigle)) {
            throw new IllegalArgumentException("Cours inexistant");
        }

        return avisRepository.findBySigleCours(sigle);

    }

    /**
     * Cette méthode permet de récupérer tous les avis stockés.
     * @return la liste de tous les avis.
     */
    public List<Avis> getAllAvis(){
        return avisRepository.findAll();
    }
}
