
package org.projet.model;

/**
 * Cette classe permet de définir l'entité Avis.
 */
public class Avis {
    private String commentaire;
    private int noteDifficulte;
    private String sigleCours;
    private boolean valide;
    private int noteChargeTravail;
    private String nomProfesseur;

    /**
     * Constructeur vide.
     */
    public Avis(){}

    /**
     * Constructeur d'avis.
     * @param sigleCours
     * @param nomProfesseur
     * @param noteCharge
     * @param noteDifficulte
     * @param commentaire
     * @param valide
     */
    public Avis( String sigleCours, String nomProfesseur, int noteCharge, int noteDifficulte, String commentaire, boolean valide) {
        this.commentaire = commentaire;
        this.noteDifficulte = noteDifficulte;
        this.sigleCours = sigleCours;
        this.noteChargeTravail = noteCharge;
        this.valide = valide;
        this.noteChargeTravail = noteChargeTravail;
        this.nomProfesseur = nomProfesseur;
    }

    /**
     * Cette méthode permet de récupérer le sigle de cours.
     * @return sigle de Cours.
     */
    public String getSigleCours() {

        return sigleCours;
    }


    /**
     * Cette méthode permet de récupérer le nom de professeur.
     * @return le nom d'un professeur.
     */
    public String getNomProfesseur() {
        return nomProfesseur;
    }

    /**
     * Cette méthode permet de récupérer le commentaire.
     * @return le commentaire.
     */

    public String getCommentaire() {
        return commentaire;
    }

    /**
     * Cette méthode permet de récupérer la note de difficulté.
     * @return la note de difficulté.
     */

    public int getNoteDifficulte() {
        return noteDifficulte;
    }

    /**
     * Cette méthode permet de récupérer la note de charge de travail.
     * @return la note de charge de travail.
     */

    public int getNoteChargeTravail() {
        return noteChargeTravail;
    }

    /**
     * Cette méthode permet de get le champ valide.
     * @return  valide.
     */
    public boolean isValide() {
        return valide;
    }

    /**
     * Cette méthode permet de set le champ valide.
     * @param valide booleen.
     */
    public void setValide(boolean valide){
this.valide = valide;
    }

}
