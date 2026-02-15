package org.projet.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * La classe Resultats gère l'extraction et l'affichage des données académiques
 * à partir d'un fichier CSV d'historique.
 */
public class Resultats {

    private String sigleCours;
    private String nom;
    private String moyenne;
    private double score;
    private int participants;
    private int trimestre;

    /**
     * Constructeur : Charge les données du cours depuis le CSV dès l'instanciation.
     * @param sigleCours Le code du cours (ex: "IFT1015") à rechercher.
     */
    public Resultats(String sigleCours) {
        this.sigleCours = sigleCours.trim().toUpperCase();
        Map<String, List<String>> resultats = transformCSVToList();

        if (resultats.containsKey(this.sigleCours)) {

            List<String> reList = resultats.get(this.sigleCours);

            this.nom = reList.get(0);
            this.moyenne = reList.get(1);
            this.score = Double.parseDouble(reList.get(2));
            this.participants = Integer.parseInt(reList.get(3));
            this.trimestre = Integer.parseInt(reList.get(4));

        } else {
            // Valeurs par défaut si le cours n'est pas trouvé
            this.nom = "";
            this.moyenne = "";
            this.score = 0.0;
            this.participants = 0;
            this.trimestre = 0;
        }
    }

    /**
     * Lit le fichier CSV depuis les ressources et transforme chaque ligne en une entrée dans une Map.
     * @return Une Map où la clé est le sigle et la valeur est la liste des informations du cours.
     */
    public Map<String, List<String>> transformCSVToList() {

        Map<String, List<String>> resultatsMap = new HashMap<>();

        InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("historique_cours_prog_117510.csv");

        if (is == null) {
            System.err.println("Erreur : fichier CSV introuvable.");
            return resultatsMap;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line;
            br.readLine(); // ignorer l'en-tête

            while ((line = br.readLine()) != null) {

                // Split CSV qui ignore les virgules à l'intérieur des guillemets
                String[] row = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                if (row.length >= 6) {

                    String key = row[0].replace("\"", "").trim().toUpperCase();

                    List<String> values = new ArrayList<>();
                    for (int i = 1; i < row.length; i++) {
                        values.add(row[i].replace("\"", "").trim());
                    }

                    resultatsMap.put(key, values);
                }
            }

        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du CSV : " + e.getMessage());
        }

        return resultatsMap;
    }
    /**
     * Retourne le nom du cours.
     * @return nom du cours.
     */
    public String getNom() {
        return nom;
    }
    /**
     * Retourne la moyenne obtenue pour le cours.
     * @return moyenne du cours.
     */
    public String getMoyenne() {
        return moyenne;
    }
    /**
     * Retourne le score numérique associé au cours.
     * @return score du cours.
     */
    public double getScore() {
        return score;
    }
    /**
     * Retourne le nombre de participants au cours.
     * @return nombre de participants.
     */
    public int getParticipants() {
        return participants;
    }
    /**
     * Retourne le trimestre associé aux résultats du cours.
     * @return trimestre du cours.
     */
    public int getTrimestre() {
        return trimestre;
    }

    /** @return Indique si le cours a été trouvé lors du chargement. */
    public boolean isCoursPresent() {
        return nom != null && !nom.isEmpty();
    }

    /** @return Une chaîne formatée affichant tous les détails du cours. */
    public String voirResultats() {
        if (!isCoursPresent()) {
            return "Désolé ! Nous n'avons trouvé aucun résultat pour le cours "
                    + sigleCours + ". Vérifiez que le sigle est correct.";
        }

        return "Résultats pour le cours " + sigleCours + " - " + nom + " :\n" +
               "Moyenne : " + moyenne + "\n" +
               "Score : " + score + "\n" +
               "Participants : " + participants + "\n" +
               "Trimestre : " + trimestre;
    }
}

