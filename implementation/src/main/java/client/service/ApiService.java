package client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.projet.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cette classe permet de gérer la communication avec l'API REST de la plateforme PickCourse.
 */
public class ApiService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
// url de base de notre api
    private final String baseUrl = "http://localhost:7070/cours/rechercher/";
    /**
     * Retourne l’instance d’ObjectMapper utilisée pour la sérialisation JSON.
     * @return object mapper.
     */
    public ObjectMapper getMapper() {
        return mapper;
    }
    /**
     * Vérifie si une chaîne représente un entier valide.
     * @param s chaîne à vérifier.
     * @return true si la chaîne est un entier, false sinon.
     */
    public static boolean estEntier(String s) {
        if (s == null || s.isEmpty()) return false;
        return s.matches("\\d+");
    }
    /**
     * Cette méthode permet de récupérer les cours pour un programme donné.
     * @param programmeId identifiant du programme
     * @return la liste de cours relatifs à ce programme.
     */

    public List<Cours> getCoursesForAProgram(String programmeId) {
        try {
            String url = "http://localhost:7070/cours-programme/" + programmeId;
// ce bloc de code tente d'envoyer une requête à notre API.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // La route retourne la liste des ids des cours.
                List<String> sigles = mapper.readValue(response.body(), new TypeReference<List<String>>() {});

                // Pour chaque sigle, on récupère le Cours complet
                return sigles.stream()
                        .map(this::rechercherCoursParSigle)
                        .filter(c -> c != null)
                        .toList();

            } else {
                // ça s'affiche dans notre console ( pour pouvoir débogger)
                System.out.println("Erreur API getCoursesForAProgram : " + response.statusCode());
                return List.of();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Cette classe interne permet de parser la sortie de l'api ( json nom-id lors de la recherche de programmes).
     */
    public class ProgrammeDTO {
        /** Identifiant du programme. */
        public String id;

        /** Nom du programme. */
        public String name;

        /**
         * Construit un ProgrammeDTO.
         * @param id identifiant du programme.
         * @param name nom du programme.
         */
        public ProgrammeDTO(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    /**
     * Cette méthode permet d'implémenter la logique pour la recherche de programmes par nom.
     * @param nom nom du programme
     * @return liste des programmes contenant ledit nom.
     */
    public List<ProgrammeDTO> rechercherProgrammesParNom(String nom) {
        try {
            String url = "http://localhost:7070/cours-programme/nom/" + nom;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<String> programmes = mapper.readValue(
                        response.body(),
                        new TypeReference<List<String>>() {}
                );

                return programmes.stream()
                        .map(p -> {
                            String[] parts = p.split("-", 2);
                            return new ProgrammeDTO(parts[0], parts[1]);
                        })
                        .toList();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }


    /**
     * Cette méthode permet de récupérer un cours à partir de son sigle.
     * @param sigle
     * @return
     */
    public Cours rechercherCoursParSigle(String sigle) {
        List<Cours> result = rechercherCours("id", sigle, "false", null); // ou session par défaut
        return result.isEmpty() ? null : result.get(0);
    }



    /**
     * Cette méthode permet de rechercher un cours en faisant des appels HTTP à l'API de PickCourse.
     * @param param id | name | description
     * @param value valeur recherchée
     * @param includeSchedule true ou false
     * @param session ex: H24, A24, null
     */
    public List<Cours> rechercherCours(String param,
                                       String value,
                                       String includeSchedule,
                                       String session) {

        try {
            // On construit le body JSON attendu par l'API
            Map<String, Object> body = new HashMap<>();
            body.put("param", param);
            body.put("valeur", value);
            body.put("includeSchedule", includeSchedule);
            body.put("semester", session);


            String requestBody = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapper.readValue(
                        response.body(),
                        new TypeReference<List<Cours>>() {}
                );
            } else {
                System.out.println("Erreur API : " + response.statusCode());
                return List.of();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return List.of();
        }
    }



    /**
     * Cette méthode permet de retourner la liste de tous les avis.
     * @return la liste de tous les avis
     */

    public List<Avis> getAllAvis(){
        try {
            // URL correcte : sans /rechercher
            String url = "http://localhost:7070/cours" +"/avis";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), new TypeReference<List<Avis>>() {});
            } else {
                System.out.println("Erreur API  : " + response.statusCode());
                return List.of();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Récupère la liste des cours offerts pour un programme et un semestre donnés.
     * @param programmeId id du programme
     * @param session ex: H24, A24
     * @return liste des sigles de cours
     */
    public List<String> getCoursesBySemester(String programmeId, String session) {
        try {
            String url = "http://localhost:7070/programme/courseBySemester/" + programmeId + "/" + session;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), new TypeReference<List<String>>() {});
            } else {
                System.out.println("Erreur API getCoursesBySemester : " + response.statusCode());
                return List.of();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return List.of();
        }
    }
    /**
     * Récupère l'horaire détaillé d'un cours pour une session donnée
     * @param sigle sigle du cours
     * @param session ex: H24, A24
     * @return Map représentant l’horaire (sections, volets, dates, etc.)
     */
    public Map<String, Object> getCourseSchedule(String sigle, String session) {
        try {
            String url = "http://localhost:7070/cours/horaires/" + sigle + "/" + session;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // On parse le JSON en Map pour être flexible
                return mapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
            } else {
                System.out.println("Erreur API getCourseSchedule : " + response.statusCode());
                return Map.of();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    /**
     * Cette méthode permet de récupérer la liste des avis associés à un cours, en faisant des appels HTTP à notre API.
     * @param sigle sigle du cours
     * @return
     */

    public List<Avis> getAvisCours(String sigle) {
        try {
            // URL correcte : sans /rechercher
            String url = "http://localhost:7070/cours/" + sigle + "/avis";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), new TypeReference<List<Avis>>() {});
            } else {
                System.out.println("Erreur API  : " + response.statusCode());
                return List.of();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Cette méthode permet de vérifier son éligibilité à un cours.
     * @param idCours id du cours en question
     * @param listeCours liste de cours déjà faits
     * @param cycle cycle d'études
     * @return retourne un message indiquant l'éligibilité ou non.
     */

    public String checkEligibility(String idCours, List<String> listeCours, Integer cycle) {
        try {
            String url = "http://localhost:7070/cours/eligibilitenew";

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode body = mapper.createObjectNode();
            body.put("idCours", idCours);
            body.put("cycle", cycle);
            body.set("listeCours", mapper.valueToTree(listeCours));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "Erreur API : " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Une erreur est survenue lors de la vérification d'éligibilité.";
        }
    }


    /**
     * Cette méthode permet d'afficher les résultats académiques associés à un cours.
     * @param sigle sigle du cours
     * @return une structure contenant les résultats académiques dudit cours.
     */

    public String afficherResultatAcademiques (String sigle){

        try {
            String url = "http://localhost:7070/cours/voir/resultat";

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode body = mapper.createObjectNode();
            body.put("sigle", sigle);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "Erreur API : " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Une erreur est survenue lors de la vérification d'éligibilité.";
        }
    }

    /**
     * Cette méthode permet de gérer la comparaison de 2 cours.
     * @param idsCours ids de cours à comparer.
     * @param criteres critères de comparaison.
     * @param session session concernée ( optionnel)
     * @return le résultat de la comparaison.
     */
    public List<List<String>> comparerCours(String[] idsCours, String[] criteres, String session) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("cours", idsCours);
            body.put("criteres", criteres);
            body.put("session", session);

            String requestBody = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7070/cours/comparer")) // baseUrl = http://localhost:7070/cours/
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), new TypeReference<List<List<String>>>() {});
            } else {
                System.out.println("Erreur API comparaison : " + response.statusCode());
                return List.of();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Cette méthode permet de gérer la comparaison de 2 cours basé sur les résultats académiques.
     * @param sigle1 sigle du cours 1
     * @param sigle2 sigle du cours 2
     * @return résultat de l'API ( phrase qui indique quel cours est + populaire et + difficile)
     */
    public String comparerCoursParResultats(String sigle1, String sigle2){
        try {
            String url = "http://localhost:7070/cours/comparer/stats";

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode body = mapper.createObjectNode();
            body.put("sigle1", sigle1);
            body.put("sigle2", sigle2);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "Erreur API : " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Une erreur est survenue lors de la vérification d'éligibilité.";
        }
    }

    /**
     * Cette méthode permet de récupérer la popularité estimée d'un cours basé sur les résultats académiques.
     * @param sigleCours sigle du cours
     * @return réponse de l'API.
     */
    public String getPopulariteCours(String sigleCours){
        try {
            String url = "http://localhost:7070/cours/popularite";

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode body = mapper.createObjectNode();
            body.put("sigle", sigleCours);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "Erreur API : " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Une erreur est survenue lors de la vérification d'éligibilité.";
        }
    }

    /**
     * Cette méthode permet de récupérer la difficulté estimée d'un cours basé sur les résultats académiques.
     * @param sigleCours sigle du cours
     * @return réponse de l'API.
     */
    public String getDifficulteCours(String sigleCours){
        try {
            String url = "http://localhost:7070/cours/difficulte";

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode body = mapper.createObjectNode();
            body.put("sigle", sigleCours);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "Erreur API : " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Une erreur est survenue lors de la vérification d'éligibilité.";
        }
    }

    // cette classe interne constitue le corps de la requête
    /**
     * Cette classe interne représente le corps de la requête envoyée à l’API
     * pour générer un horaire à partir d’un ensemble de cours.
     */
    public static class HoraireRequest {
        /** Liste des identifiants des cours sélectionnés. */
        public List<String> idCours;

        /** Session académique concernée (ex : H24, A24). */
        public String session;

        /** Indique si des sections spécifiques sont prises en compte. */
        public boolean sections;

        /** Choix personnalisés de sections pour chaque cours. */
        public Map<String, Map<String, String>> choix;
        /**
         * Constructeur vide requis pour la désérialisation JSON.
         */
        public HoraireRequest() {}
        /**
         * Construit une requête d’horaire avec les paramètres fournis.
         *
         * @param idCours liste des identifiants de cours.
         * @param session session académique.
         * @param sections indique si des sections sont sélectionnées.
         * @param choix choix personnalisés de sections.
         */
        public HoraireRequest(List<String> idCours, String session, boolean sections,
                              Map<String, Map<String, String>> choix) {
            this.idCours = idCours;
            this.session = session;
            this.sections = sections;
            this.choix = choix;
        }
    }

    // cette classe interne permet de parser la réponse de l'API.
    /**
     * Cette classe interne représente le résultat retourné par l’API
     * lors de la génération d’un horaire.
     */
    public static class ResultatHoraire {
        /** Structure représentant l’horaire généré. */
        public Map<String, List<List<String>>> horaire;

        /** Liste des conflits horaires détectés. */
        public List<ConflitHoraireDTO> conflits;

        /**
         * Cette classe représente un conflit horaire détecté
         * pour un ou plusieurs cours.
         */
        public static class ConflitHoraireDTO {
            /** Jour où le conflit a lieu. */
            public String jour;

            /** Intervalle horaire du conflit. */
            public String intervalle;

            /** Ensemble des cours impliqués dans le conflit. */
            public Set<String> cours;
            /**
             * Retourne une représentation textuelle du conflit horaire.
             * @return description du conflit.
             */
            @Override
            public String toString() {
                return jour + " " + intervalle + " : " + String.join(", ", cours);
            }
        }
    }


    /**
     * Cette méthode permet de générer l'horaire d'un ensemble de cours.
     * @param idsCours ids de cours dont on veut générer l'horaire.
     * @param session session
     * @param sections sections
     * @param choix choix personnalisés
     * @return un objet de la classe ResultatHoraire.
     */
    public ResultatHoraire genererHoraire(
            List<String> idsCours,
            String session,
            boolean sections,
            Map<String, Map<String, String>> choix
    ) {
        try {
            // Construire le corps JSON
            HoraireRequest requestBodyObj = new HoraireRequest(idsCours, session, sections, choix);
            String requestBody = mapper.writeValueAsString(requestBodyObj);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7070/horaire"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Désérialiser correctement
                return mapper.readValue(response.body(), ResultatHoraire.class);
            } else {
                System.out.println("Erreur API horaire : " + response.statusCode() + " - " + response.body());
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Cette méthode permet de récupérer la charge totale de travail estimée basée sur les avis.
     * @param idsCours ids de Cours dont on veut estimer la charge.
     * @return liste de cours + estimations
     */

    public List<List<String>> getChargeDeTravailAvis(String[] idsCours) {
        try {
            String url = "http://localhost:7070/cours/comparer/avis/charge";

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode body = mapper.createObjectNode();
            // Pour envoyer un tableau JSON, on utilise putPOJO
            body.putPOJO("idsCours", idsCours);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // On parse le JSON en List<List<String>>
                return mapper.readValue(
                        response.body(),
                        mapper.getTypeFactory().constructCollectionType(
                                List.class,
                                mapper.getTypeFactory().constructCollectionType(List.class, String.class)
                        )
                );
            } else {
                System.err.println("Erreur API : " + response.statusCode());
                return List.of(); // retourne une liste vide si erreur
            }
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // retourne une liste vide en cas d'exception
        }
    }

    /**
     * Cette méthode permet de récupérer la difficulté estimée des cours
     * basée sur les avis des étudiants, via l’API PickCourse.
     *
     * @param idsCours tableau des identifiants des cours à analyser.
     * @return liste contenant, pour chaque cours, les informations liées
     *         à la difficulté perçue selon les avis.
     */
    public List<List<String>> getDifficulteAvis(String[] idsCours) {
        try {
            String url = "http://localhost:7070/cours/comparer/avis/difficulte";

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode body = mapper.createObjectNode();
            // Pour envoyer un tableau JSON, on utilise putPOJO
            body.putPOJO("idsCours", idsCours);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // On parse le JSON en List<List<String>>
                return mapper.readValue(
                        response.body(),
                        mapper.getTypeFactory().constructCollectionType(
                                List.class,
                                mapper.getTypeFactory().constructCollectionType(List.class, String.class)
                        )
                );
            } else {
                System.out.println("Erreur API : " + response.statusCode());
                return List.of(); // retourne une liste vide si erreur
            }
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // retourne une liste vide en cas d'exception
        }
    }

    /**
     * Cette méthode permet de communiquer avec l'API de PickCourse pour comparer des ensembles de cours.
     * @param listeDeListesDeCours liste de listes de cours à comparer
     * @param session session ( optionnelle), nécessaire lorsqu'on veut comparer selon les horaires.
     * @return liste de listes avec des critères de comparaison.
     */

    public List<List<String>> comparerCombinaisonCoursApi(List<List<String>> listeDeListesDeCours, String session) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("listeCours", listeDeListesDeCours);
            body.put("session", session);

            String requestBody = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7070/cours/comparer/combinaison"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapper.readValue(
                        response.body(),
                        mapper.getTypeFactory().constructCollectionType(
                                List.class,
                                mapper.getTypeFactory().constructCollectionType(List.class, String.class)
                        )
                );
            } else {
                System.out.println("Erreur API comparerCombinaisonCours : " + response.statusCode());
                return List.of();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }




}