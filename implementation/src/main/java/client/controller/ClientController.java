package client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.text.Text;
import org.projet.model.*;
import client.service.ApiService;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Cette classe permet de gérer les interactions avec les utilisateurs sur la plateforme PickCourse.
 */

public class ClientController {

    // Composants
    private TextField champRecherche;
    private ListView<Cours> listeResultats;
    private ComboBox<String> typeRecherche; // "ID", "Nom" ou "Mot-clé"
    private Label messageLabel; // au niveau de la classe
    private VBox comparaisonBox; // pour la vue comparaison
    private ListView<String> listeCoursComparaison; // liste des cours à comparer
    private VBox criteresBox; // checkboxes critères
    private TextField sessionField;
    private Label messageResultatAcademiquePopularite = new Label();
    private VBox rechercheLayout;

    private Label messageResultatAcademiqueDifficulte = new Label();
    private Label messageResultatAcademique = new Label();
    private Label messageAvisInofficiels = new Label();
    private ListView<List<String>> listeCombinaisons;

    private TableView<ObservableList<String>> tableComparaison;
    private final ApiService coursService = new ApiService();
    private final String[] CRITERES = {
            "id", "name", "description", "credits", "scheduledSemester", "schedules",
            "prerequisite_courses", "equivalent_courses", "concomitant_courses",
            "mode", "available_terms", "available_periods", "udemWebsite",
            "popularité officielle", "difficulté officielle", "difficulté inofficielle", "charge de travail inofficielle"
    };

    private static final String[] COLONNES_COMBINAISON = {
            "Combinaison",
            "Cours",
            "Crédits",
            "Moyenne inofficielle de la charge de travail",
            "Moyenne inofficielle de la difficulté",
            "Moyenne résultats",
            "Prérequis",
            "Concomitants",
            "Périodes communes",
            "Sessions communes",
            "Horaires",
            "Conflits",
    };


    private TextField champSession = new TextField();

    public TextField getChampSession() {
        return champSession;
    }

    public Label getMessageLabel(){ return messageLabel;}

    /**
     * Cette méthode permet d'initialiser le ClientController() avec l'interface de recherche.
     */
    public ClientController() {
        champRecherche = new TextField();
        listeResultats = new ListView<>();
        typeRecherche = new ComboBox<>();
        typeRecherche.getItems().addAll("ID", "Nom", "Mot-clé");
        typeRecherche.setValue("Nom"); // valeur par défaut
        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-style: italic;");

        initialize();
    }
    public ComboBox<String> getTypeRecherche() {
        return typeRecherche;
    }

    /**
     * Cette méthode permet d'initialiser le controller avec les résultats de la recherche de cours.
     */

    private void initialize() {
        listeResultats.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Cours cours, boolean empty) {
                super.updateItem(cours, empty);
                if (empty || cours == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox container = new VBox(5);
                    container.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-radius: 5; -fx-background-radius: 5; -fx-background-color: #f4f4f4;");

                    Label titre = new Label(cours.getId() + " - " + cours.getName());
                    titre.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

                    Label description = new Label(cours.getDescription());
                    description.setWrapText(true);

                    Label credits = new Label("Crédits: " + cours.getCredits());
                    Label prereq = new Label("Prérequis: " + (cours.getPrerequisite_courses() != null ? String.join(", ", cours.getPrerequisite_courses()) : "Aucun"));
                    Label equiv = new Label("Équivalents: " + (cours.getEquivalent_courses() != null ? String.join(", ", cours.getEquivalent_courses()) : "Aucun"));
                    Label concom = new Label("Concomitants: " + (cours.getConcomitant_courses() != null ? String.join(", ", cours.getConcomitant_courses()) : "Aucun"));

                    // Boutons
                    Button btnHoraire = new Button("Voir horaire");
                    btnHoraire.setOnAction(e -> afficherHoraire(cours));

                    Button btnAvis = new Button("Voir avis");
                    btnAvis.setOnAction(e -> afficherAvis(cours));
                    Button btnEligibilite = new Button("Vérifier l'éligibilité");
                    btnEligibilite.setOnAction(e -> afficherEligibilite(cours));

                    HBox boutons = new HBox(10, btnHoraire, btnAvis, btnEligibilite);
                    container.getChildren().addAll(titre, description, credits, prereq, equiv, concom, boutons);
                    setGraphic(container);




                }
            }
        });
    }

    /**
     * Cette méthode permet de gérer l'action de recherche effectuée par l'utilisater.
     */
    public void rechercher() {
        String texte = champRecherche.getText().trim();
        String sessionTexte = champSession.getText().trim();
        listeResultats.getItems().clear();
        messageLabel.setText(""); // reset message

        if (texte.isEmpty()) {
            messageLabel.setText("Veuillez entrer un texte de recherche.");
            return;
        }

        String param;
        switch (typeRecherche.getValue()) {
            case "ID": param = "id"; break;
            case "Mot-clé": param = "description"; break;
            default: param = "name"; break;
        }

        boolean includeSchedule = !sessionTexte.isEmpty();
        String session = includeSchedule ? sessionTexte : null;

        try {
            List<Cours> resultats = coursService.rechercherCours(param, texte, String.valueOf(includeSchedule), session);


     // platform runlater car ça s'exécute sur un thrad séparé ( pour éviter les bugs ) et son utilisation permet de faire en sorte que ça s'exécute.
            Platform.runLater(() -> {
                if (resultats.isEmpty()) {
                    messageLabel.setText("Aucun cours trouvé pour cette recherche. Vérifiez d'avoir choisi le bon paramètre, et que ce dernier est valide et recommencez.");
                    listeResultats.getItems().clear();
                } else {
                    messageLabel.setText("");
                    listeResultats.getItems().clear();
                    listeResultats.getItems().addAll(resultats);
                }
            });


        } catch (Exception e) { // ou l'exception que ton service lève pour une 404
            // Affiche le message d'erreur sur l'interface
            Platform.runLater(() -> {
                messageLabel.setText("Erreur : cours introuvable ou session invalide.");
                listeResultats.getItems().clear();
            });
        }
    }



    /**
     * Cette méthode permet d'afficher l'horaire d'un cours donné.
     * @param cours cours dont on veut afficher l'horaire.
     */

    public void afficherHoraire(Cours cours) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Horaire pour " + cours.getId() + " - " + cours.getName());

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 10;");

        if (cours.getSchedules() == null || cours.getSchedules().isEmpty()) {
            root.getChildren().add(new Label("Aucun horaire disponible, veuillez préciser une session et recommencez."));
        } else {
            // Récupérer les semestres disponibles
            List<String> semestres = cours.getSchedules().stream()
                    .map(Cours.Schedule::getSemester)
                    .distinct() // garder seulement les valeurs uniques
                    .toList();

            Label labelChoix = new Label("Sélectionnez un semestre:");
            ComboBox<String> comboSemestre = new ComboBox<>();
            comboSemestre.getItems().addAll(semestres);
            comboSemestre.setValue(semestres.get(0)); // valeur par défaut

            VBox horairesBox = new VBox(5); // contiendra les horaires filtrés
            Label messageErreur = new Label();
            messageErreur.setStyle("-fx-text-fill: red; -fx-font-style: italic;");

            Button btnAfficher = new Button("Afficher l'horaire");
            btnAfficher.setOnAction(e -> {
                horairesBox.getChildren().clear();
                messageErreur.setText(""); // reset message
                String semestreChoisi = comboSemestre.getValue();

                if (!semestres.contains(semestreChoisi)) {
                    messageErreur.setText(" Le semestre sélectionné n'existe pas pour ce cours.");
                    return;
                }

                // filtrer le bon semestre
                cours.getSchedules().stream()
                        .filter(s -> s.getSemester().equals(semestreChoisi))
                        .forEach(sched -> {
                            VBox schedBox = new VBox(5);
                            schedBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5; -fx-padding: 5; -fx-background-color: #f9f9f9;");
                            Label semesterLabel = new Label("Semestre: " + sched.getSemester());
                            semesterLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
                            schedBox.getChildren().add(semesterLabel);

                            if (sched.getSections() != null) {
                                for (Cours.Section sec : sched.getSections()) {
                                    VBox secBox = new VBox(3);
                                    secBox.setStyle("-fx-padding: 3; -fx-border-color: lightgray; -fx-border-radius: 3;");
                                    Label secLabel = new Label("Section: " + sec.getName());
                                    secLabel.setStyle("-fx-font-weight: bold;");
                                    secBox.getChildren().add(secLabel);

                                    if (sec.getVolets() != null) {
                                        for (Cours.Volet volet : sec.getVolets()) {
                                            VBox voletBox = new VBox(2);
                                            Label voletLabel = new Label("Volet: " + volet.getName());
                                            voletBox.getChildren().add(voletLabel);

                                            if (volet.getActivities() != null) {
                                                for (Cours.Activity act : volet.getActivities()) {
                                                    String info = String.join("/", act.getDays()) + " "
                                                            + act.getStart_time() + "–" + act.getEnd_time()
                                                            + " (" + act.getMode() + ")";
                                                    voletBox.getChildren().add(new Label(info));
                                                }
                                            }
                                            secBox.getChildren().add(voletBox);
                                        }
                                    }
                                    schedBox.getChildren().add(secBox);
                                }
                            }
                            horairesBox.getChildren().add(schedBox);
                        });
            });

            root.getChildren().addAll(labelChoix, comboSemestre, btnAfficher, messageErreur, horairesBox);
        }

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        Scene scene = new Scene(scroll, 500, 400);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Cette méthode permet d'afficher tous les avis de la plateforme.
     */
    public void afficherAllAvis(){
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Avis enregistrés :");

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 10;");

        List<Avis> avisList = coursService.getAllAvis();

        if (avisList.isEmpty()) {
            root.getChildren().add(new Label("Aucun avis disponible."));
        } else {
            for (Avis avis : avisList) {
                VBox avisBox = new VBox(5);
                avisBox.setStyle("-fx-border-color: gray; -fx-padding: 5; -fx-background-color: #f9f9f9;");
                Label sigleCours = new Label("Cours :" + avis.getSigleCours());
                sigleCours.setStyle("-fx-font-weight: bold;");
                Label auteur = new Label("Professeur : " + avis.getNomProfesseur());
                auteur.setStyle("-fx-font-weight: bold;");

                Label texte = new Label(avis.getCommentaire());
                texte.setWrapText(true);

                Label note = new Label("Note Difficulté : " + avis.getNoteDifficulte());
                Label note2 = new Label("Note Charge de Travail : " + avis.getNoteChargeTravail());

                avisBox.getChildren().addAll(sigleCours, auteur, texte, note,note2);
                root.getChildren().add(avisBox);
            }
        }

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        Scene scene = new Scene(scroll, 500, 400);
        stage.setScene(scene);
        stage.show();
    }
    /**
     * Cette méthode permet d'afficher les avis relatifs à un cours.
     * @param cours cours dont on veut afficher les avis.
     */
    public void afficherAvis(Cours cours) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Avis pour " + cours.getId() + " - " + cours.getName());

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 10;");

        List<Avis> avisList = coursService.getAvisCours(cours.getId());

        if (avisList.isEmpty()) {
            root.getChildren().add(new Label("Aucun avis disponible pour ce cours."));
        } else {
            for (Avis avis : avisList) {
                VBox avisBox = new VBox(5);
                avisBox.setStyle("-fx-border-color: gray; -fx-padding: 5; -fx-background-color: #f9f9f9;");

                Label auteur = new Label("Professeur : " + avis.getNomProfesseur());
                auteur.setStyle("-fx-font-weight: bold;");

                Label texte = new Label(avis.getCommentaire());
                texte.setWrapText(true);

                Label note = new Label("Note Difficulté : " + avis.getNoteDifficulte());
                Label note2 = new Label("Note Charge de Travail : " + avis.getNoteChargeTravail());

                avisBox.getChildren().addAll(auteur, texte, note,note2);
                root.getChildren().add(avisBox);
            }
        }

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        Scene scene = new Scene(scroll, 500, 400);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Cette méthode permet de traiter la vérification d'éligibilité à un cours.
     * @param cours cours auquel on veut vérifier l'éligibilité.
     */
    private void afficherEligibilite(Cours cours) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Vérifier votre éligibilité à " + cours.getId());

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 10;");

        Label instruction = new Label("Vérifiez si vous êtes éligible à ce cours : " + cours.getId());
        instruction.setStyle("-fx-font-weight: bold;");

        TextField champCycle = new TextField();
        Label instructionCycle = new Label("Entrez votre cycle qui doit être un entier entre 1 et 4");
        champCycle.setPromptText("Entrez votre cycle (1-4)");

        TextField champCoursFaits = new TextField();
        champCoursFaits.setPromptText("Ajoutez les cours déjà complétés (séparés par des virgules)");

        Button btnVerifier = new Button("Vérifier l'éligibilité");
        Label resultat = new Label();

        btnVerifier.setOnAction(e -> {
            String cycleStr = champCycle.getText().trim();
            String[] coursFaitsArray = champCoursFaits.getText().split(",");
            List<String> coursFaits = List.of(coursFaitsArray).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            try {
                int cycle = Integer.parseInt(cycleStr);
                String message = new ApiService().checkEligibility(cours.getId(), coursFaits, cycle);
                resultat.setText(message);
            } catch (NumberFormatException ex) {
                resultat.setText("Le cycle doit être un nombre entre 1 et 4");
            }
        });

        root.getChildren().addAll(instruction, instructionCycle, champCycle, champCoursFaits, btnVerifier, resultat);

        Scene scene = new Scene(root, 500, 250);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Cette méthode permet de gérer la logique derrière la requête pour voir les résultats académiques.
     * @return l'interface pour les résultats académiques
     */
    public VBox afficherResultatsAcademiques() {
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20;");

        Label title = new Label("Résultats académiques");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField champSigle = new TextField();
        champSigle.setPromptText("Entrez le sigle du cours (ex: IFT2255)");

        Button btnVoir = new Button("Voir les résultats");
        btnVoir.setStyle("-fx-background-color: #8B635C; -fx-text-fill: white;");

        TextArea zoneResultats = new TextArea();
        zoneResultats.setEditable(false);
        zoneResultats.setWrapText(true);
        zoneResultats.setPrefHeight(300);

        btnVoir.setOnAction(e -> {
            String sigle = champSigle.getText().trim();
            if (sigle.isEmpty()) {
                zoneResultats.setText("Veuillez entrer un sigle de cours.");
                return;
            }

            String resultat = coursService.afficherResultatAcademiques(sigle);
            zoneResultats.setText(resultat);
        });

        root.getChildren().addAll(title, champSigle, btnVoir, zoneResultats);
        return root;
    }

    /**
     * Cette méthode permet de récupérer la vue de la comparaison.
     * @return la vue de la comparaison.
     */

    public VBox getVueComparaison() {
        if (comparaisonBox != null) return comparaisonBox;

        VBox innerBox = new VBox(10);
        innerBox.setStyle("-fx-padding: 20;");

        ScrollPane scrollPane = new ScrollPane(innerBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        comparaisonBox = new VBox();
        comparaisonBox.getChildren().add(scrollPane);

        Label title = new Label("Comparaison de cours");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Mode comparaison de 2 cours
        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton rbDeuxCours = new RadioButton("Comparer 2 cours");
        rbDeuxCours.setToggleGroup(modeGroup);
        rbDeuxCours.setSelected(true);

        RadioButton rbEnsemblesCours = new RadioButton("Comparer des ensembles de cours");
        rbEnsemblesCours.setToggleGroup(modeGroup);

        HBox modeBox = new HBox(10, rbDeuxCours, rbEnsemblesCours);

        // Interface associée
        VBox vueComparaisonCours = new VBox(10);
        listeCoursComparaison = new ListView<>();
        listeCoursComparaison.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listeCoursComparaison.setPrefHeight(200);

        TextField champAjouterCours = new TextField();
        champAjouterCours.setPromptText("Ajouter sigle de cours");

        Button btnAjouter = new Button("Ajouter");
        btnAjouter.setStyle("-fx-background-color: #623E32; -fx-text-fill: white;");
        btnAjouter.setOnAction(e -> {
            String sigle = champAjouterCours.getText().trim();
            if (!sigle.isEmpty() && !listeCoursComparaison.getItems().contains(sigle)) {
                listeCoursComparaison.getItems().add(sigle);
                champAjouterCours.clear();
            }
        });

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setDisable(true);
        btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        listeCoursComparaison.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            btnSupprimer.setDisable(listeCoursComparaison.getSelectionModel().getSelectedItems().isEmpty());
        });

        btnSupprimer.setOnAction(e -> {
            ObservableList<String> selected = FXCollections.observableArrayList(
                    listeCoursComparaison.getSelectionModel().getSelectedItems()
            );
            if (!selected.isEmpty()) {
                listeCoursComparaison.getItems().removeAll(selected);
            }
        });

        HBox ajoutCoursBox = new HBox(10, champAjouterCours, btnAjouter, btnSupprimer);

        criteresBox = new VBox(5);
        for (String crit : CRITERES) {
            CheckBox cb = new CheckBox(crit);
            criteresBox.getChildren().add(cb);
        }
        ScrollPane scrollCriteres = new ScrollPane(criteresBox);
        scrollCriteres.setPrefHeight(150);

        sessionField = new TextField();
        sessionField.setPromptText("Session (optionnel)");

        Button btnComparerCours = new Button("Comparer");
        btnComparerCours.setStyle("-fx-background-color: #623E32; -fx-text-fill: white;");
        btnComparerCours.setOnAction(e -> lancerComparaison());

        vueComparaisonCours.getChildren().addAll(
                ajoutCoursBox,
                listeCoursComparaison,
                new Label("Critères à comparer:"),
                scrollCriteres,
                new Label("Session:"),
                sessionField,
                btnComparerCours
        );

        // Interface comparer ensembles cours
        VBox vueComparaisonEnsembles = new VBox(10);
        VBox combinaisonsBox = new VBox(5);
        List<ListView<String>> combinaisons = new ArrayList<>();

        Button btnAjouterCombinaison = new Button("+ Ajouter une combinaison");
        btnAjouterCombinaison.setOnAction(e -> {
            ListView<String> lv = new ListView<>();
            lv.setPrefHeight(150);
            combinaisons.add(lv);

            VBox bloc = new VBox(5, new Label("Combinaison " + combinaisons.size()), lv, creerAjoutCoursBox(lv));
            combinaisonsBox.getChildren().add(bloc);
        });

        Button btnComparerEnsembles = new Button("Comparer ensembles");
        btnComparerEnsembles.setStyle("-fx-background-color: #623E32; -fx-text-fill: white;");
        btnComparerEnsembles.setOnAction(e -> {
            List<List<String>> ensembles = new ArrayList<>();
            for (ListView<String> lv : combinaisons) {
                if (!lv.getItems().isEmpty()) ensembles.add(new ArrayList<>(lv.getItems()));
            }
            if (ensembles.isEmpty()) {
                messageLabel.setText("Veuillez ajouter au moins une combinaison.");
                return;
            }
            String session = sessionField.getText().trim().isEmpty() ? null : sessionField.getText().trim();
            try {
                List<List<String>> resultat = coursService.comparerCombinaisonCoursApi(ensembles, session);
                afficherTableResultats(resultat);
            } catch (Exception ex) {
                messageLabel.setText("Erreur lors de la comparaison : " + ex.getMessage());
            }
        });

        vueComparaisonEnsembles.getChildren().addAll(btnAjouterCombinaison, combinaisonsBox, btnComparerEnsembles);
        vueComparaisonEnsembles.setVisible(false);
        vueComparaisonEnsembles.setManaged(false);

        // ------------------- Table et message -------------------
        tableComparaison = new TableView<>();
        tableComparaison.setPrefHeight(600);
        tableComparaison.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        tableComparaison.setPlaceholder(new Label("Aucune comparaison à afficher. Sélectionnez des cours + critères puis cliquez sur Comparer."));

        messageResultatAcademique.setWrapText(true);
        messageResultatAcademique.setStyle("-fx-padding: 8;-fx-text-fill: black;-fx-font-style: italic;");

        // Listening to le toggle
        modeGroup.selectedToggleProperty().addListener((obs, old, selected) -> {
            boolean comparerDeuxCours = selected == rbDeuxCours;
            vueComparaisonCours.setVisible(comparerDeuxCours);
            vueComparaisonCours.setManaged(comparerDeuxCours);
            vueComparaisonEnsembles.setVisible(!comparerDeuxCours);
            vueComparaisonEnsembles.setManaged(!comparerDeuxCours);
        });

        innerBox.getChildren().addAll(
        title,
        modeBox,
        vueComparaisonCours,
        vueComparaisonEnsembles,
        tableComparaison
        );


        return comparaisonBox;
    }

    /**
     * Cette méthode permet d'afficher la comparaison basée sur les avis.
     * @param ids ids de cours
     * @param difficulteInoff difficulté ou non
     * @param chargeInoff charge ou non
     */
    private void afficherResultatsInofficiels(String[] ids, boolean difficulteInoff, boolean chargeInoff) {
        VBox container = new VBox(8);
        container.setStyle("-fx-padding: 10;");

        if (difficulteInoff) {
            List<List<String>> diffAvis = coursService.getDifficulteAvis(ids);
            Label titre = new Label("Difficulté inofficielle moyenne");
            titre.setStyle("-fx-font-weight: bold; -fx-underline: true;");
            container.getChildren().add(titre);

            for (int i = 0; i < Math.min(ids.length, diffAvis.size()); i++) {
                String texte = diffAvis.get(i).isEmpty() ? "Aucune donnée" : String.join(", ", diffAvis.get(i));
                Label lbl = new Label(ids[i] + " : " + texte);
                lbl.setWrapText(true);
                container.getChildren().add(lbl);
            }
        }

        if (chargeInoff) {
            List<List<String>> chargeAvis = coursService.getChargeDeTravailAvis(ids);
            Label titre = new Label(" Charge de travail inofficielle moyenne");
            titre.setStyle("-fx-font-weight: bold; -fx-underline: true;");
            container.getChildren().add(titre);

            for (int i = 0; i < Math.min(ids.length, chargeAvis.size()); i++) {
                String texte = chargeAvis.get(i).isEmpty() ? "Aucune donnée" : String.join(": ", chargeAvis.get(i));
                Label lbl = new Label( texte);
                lbl.setWrapText(true);
                container.getChildren().add(lbl);
            }
        }

        messageAvisInofficiels.setGraphic(container);
        messageAvisInofficiels.setText(""); // vider l'ancien texte
    }


    /**
     * Cette méthode permet de gérer l'ajout de cours dans une combinaison.
     * @param lv la liste view.
     * @return HBOX du bloc de combinaison.
     */
    private HBox creerAjoutCoursBox(ListView<String> lv) {
        TextField champCours = new TextField();
        champCours.setPromptText("Ajouter sigle de cours");
        Button btnAjouter = new Button("Ajouter");
        btnAjouter.setStyle("-fx-background-color: #623E32; -fx-text-fill: white;");
        btnAjouter.setOnAction(e -> {
            String sigle = champCours.getText().trim();
            if (!sigle.isEmpty() && !lv.getItems().contains(sigle)) {
                lv.getItems().add(sigle);
                champCours.clear();
            }
        });
        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setOnAction(e -> {
            lv.getItems().remove(lv.getSelectionModel().getSelectedItems());
        });
        return new HBox(5, champCours, btnAjouter, btnSupprimer);
    }



    /**
     * Cette méthode permet de convertir les listes en tableviews.
     * @param resultat liste de cours
     */
    private void afficherTableResultats(List<List<String>> resultat) {
        tableComparaison.getColumns().clear();
        tableComparaison.getItems().clear();

        if (resultat == null || resultat.isEmpty()) return;

        int nbColonnes = Math.min(COLONNES_COMBINAISON.length, resultat.get(0).size());

        for (int i = 0; i < nbColonnes; i++) {
            final int idx = i;

            TableColumn<ObservableList<String>, String> col = new TableColumn<>(COLONNES_COMBINAISON[i]);

            col.setCellValueFactory(data -> {
                ObservableList<String> row = data.getValue();
                return new SimpleStringProperty(idx < row.size() ? nettoyerValeur(row.get(idx)) : "");
            });

            col.setMinWidth(160);

            // Wrap text pour les cellules
            col.setCellFactory(tc -> {
                TableCell<ObservableList<String>, String> cell = new TableCell<>() {
                    private final Text text = new Text();

                    {
                        text.wrappingWidthProperty().bind(tc.widthProperty().subtract(10)); // ajuster au padding
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                        } else {
                            text.setText(item);
                            setGraphic(text);
                        }
                    }
                };
                return cell;
            });

            tableComparaison.getColumns().add(col);
        }

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (List<String> row : resultat) {
            data.add(FXCollections.observableArrayList(row));
        }

        tableComparaison.setItems(data);
    }

    /**
     * Cette méthode permet de nettoyer valeur afin d'enlever le =.
     * @param valeur contenu à nettoyer
     * @return contenu nettoyé
     */

    private String nettoyerValeur(String valeur) {
        if (valeur == null) return "";
        int idx = valeur.indexOf('=');
        return (idx > -1) ? valeur.substring(idx + 1) : valeur;
    }

    // Construis les ensembles
    private List<List<String>> obtenirEnsemblesSelectionnes() {
        List<List<String>> ensembles = new ArrayList<>();
        for (List<String> combinaison : listeCombinaisons.getItems()) {
            ensembles.add(new ArrayList<>(combinaison)); // copie profonde de chaque combinaison
        }
        return ensembles;
    }

    /**
     * Cette méthode gère la logique derrière les deux types de comparaison
     * pour tous les critères possibles.
     */

    private void lancerComparaison() {

        
    List<String> cours = listeCoursComparaison.getItems();

    List<String> criteresSelectionnes = criteresBox.getChildren().stream()
            .filter(node -> node instanceof CheckBox cb && cb.isSelected())
            .map(node -> ((CheckBox) node).getText())
            .toList();

    messageLabel.setText("");

    if (cours.isEmpty() || criteresSelectionnes.isEmpty()) {
        messageLabel.setText("Veuillez sélectionner au moins un cours et un critère.");
        return;
    }

    // 1) Reset tableau
    tableComparaison.getItems().clear();
    tableComparaison.getColumns().clear();

    // 2) Colonne Cours (toujours)
    TableColumn<ObservableList<String>, String> colCours = new TableColumn<>("Cours");
    colCours.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
    tableComparaison.getColumns().add(colCours);

    // 3) Préparer les colonnes selon les critères choisis
    // On garde l’ordre de sélection
    List<String> criteresDansTable = new ArrayList<>(criteresSelectionnes);

    for (int i = 0; i < criteresDansTable.size(); i++) {
        final int idx = i + 1; // +1 car colonne 0 = cours
        String crit = criteresDansTable.get(i);

        TableColumn<ObservableList<String>, String> col = new TableColumn<>(crit);
        col.setCellValueFactory(data -> {
            ObservableList<String> row = data.getValue();
            return new SimpleStringProperty(idx < row.size() ? row.get(idx) : "");
        });
        tableComparaison.getColumns().add(col);
    }

    // 4) Créer une "matrice" : 1 ligne par cours
    // row = [coursId, valCrit1, valCrit2, ...]
    List<ObservableList<String>> rows = new ArrayList<>();

    for (String idCours : cours) {
        ObservableList<String> row = FXCollections.observableArrayList();
        row.add(idCours);

        // placeholders pour chaque critère, rempli ensuite
        for (int i = 0; i < criteresDansTable.size(); i++) row.add("");

        rows.add(row);
    }

    // 5) Remplir les valeurs
    // a) Catalogue : on appelle comparerCours(...) uniquement si au moins un critère "catalogue" est sélectionné
    List<String> criteresCatalogue = criteresDansTable.stream()
            .filter(c -> !c.equals("popularité officielle")
                    && !c.equals("difficulté officielle")
                    && !c.equals("difficulté inofficielle")
                    && !c.equals("charge de travail inofficielle"))
            .toList();

    if (!criteresCatalogue.isEmpty()) {
        List<List<String>> resultatCatalogue = coursService.comparerCours(
                cours.toArray(new String[0]),
                criteresCatalogue.toArray(new String[0]),
                sessionField.getText().trim().isEmpty() ? null : sessionField.getText().trim()
        );

        // resultatCatalogue: chaque row = [sigle, valCritCatalogue1, valCritCatalogue2, ...] dans l’ordre de criteresCatalogue
        // On doit mapper ces valeurs dans les bonnes colonnes globales (criteresDansTable)
        for (List<String> r : resultatCatalogue) {
            if (r.isEmpty()) continue;
            String sigle = r.get(0);

            int rowIndex = cours.indexOf(sigle);
            if (rowIndex < 0) continue;

            ObservableList<String> targetRow = rows.get(rowIndex);

            for (int j = 0; j < criteresCatalogue.size(); j++) {
                String crit = criteresCatalogue.get(j);
                int globalIdx = criteresDansTable.indexOf(crit); // index dans la table globale
                if (globalIdx >= 0 && (j + 1) < r.size()) {
                    targetRow.set(globalIdx + 1, r.get(j + 1)); // +1 car cours en 0
                }
            }
        }
    }

    // b) Officiel : seulement si 2 cours
    if (cours.size() == 2 && (criteresDansTable.contains("popularité officielle") || criteresDansTable.contains("difficulté officielle"))) {
        try {
            String json = coursService.comparerCoursParResultats(cours.get(0), cours.get(1));
            JsonNode root = new ObjectMapper().readTree(json);

            String popularite = root.has("popularite") ? root.get("popularite").asText() : "N/A";
            String difficulte = root.has("difficulte") ? root.get("difficulte").asText() : "N/A";

            if (criteresDansTable.contains("popularité officielle")) {
                int cIdx = criteresDansTable.indexOf("popularité officielle");
                rows.get(0).set(cIdx + 1, popularite);
                rows.get(1).set(cIdx + 1, popularite);
            }
            if (criteresDansTable.contains("difficulté officielle")) {
                int cIdx = criteresDansTable.indexOf("difficulté officielle");
                rows.get(0).set(cIdx + 1, difficulte);
                rows.get(1).set(cIdx + 1, difficulte);
            }

        } catch (Exception ignored) {}
    }

    // c) Avis (inofficiel)
    if (criteresDansTable.contains("difficulté inofficielle")) {
        String[] ids = cours.toArray(new String[0]);
        List<List<String>> diffAvis = coursService.getDifficulteAvis(ids);

        int cIdx = criteresDansTable.indexOf("difficulté inofficielle");
        for (int i = 0; i < Math.min(rows.size(), diffAvis.size()); i++) {
            String val = diffAvis.get(i).isEmpty() ? "Aucune donnée" : String.join(", ", diffAvis.get(i));
            rows.get(i).set(cIdx + 1, val);
        }
    }

    if (criteresDansTable.contains("charge de travail inofficielle")) {
        String[] ids = cours.toArray(new String[0]);
        List<List<String>> chargeAvis = coursService.getChargeDeTravailAvis(ids);

        int cIdx = criteresDansTable.indexOf("charge de travail inofficielle");
        for (int i = 0; i < Math.min(rows.size(), chargeAvis.size()); i++) {
            String val = chargeAvis.get(i).isEmpty() ? "Aucune donnée" : String.join(", ", chargeAvis.get(i));
            rows.get(i).set(cIdx + 1, val);
        }
    }

    // 6) Mettre les lignes dans le tableau
    tableComparaison.setItems(FXCollections.observableArrayList(rows));

    }


    /**
     * Cette méthode permet d'afficher les horaires d'un ensemble de cours.
     * @param idsCours
     * @param session
     * @param choix
     */


    public void afficherHorairesEnsemble(List<String> idsCours, String session, Map<String, Map<String, String>> choix) {
        // Vérification des cours sélectionnés
        if (idsCours == null || idsCours.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Cours manquant");
            alert.setContentText("Veuillez sélectionner au moins un cours.");
            alert.showAndWait();
            return;
        }

        // Vérification de la session
        if (session == null || session.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Session manquante");
            alert.setContentText("Veuillez entrer une session.");
            alert.showAndWait();
            return;
        }

        // Limite de 6 cours max
        if (idsCours.size() > 6) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Nombre de cours dépassé");
            alert.setContentText("Il faut sélectionner au maximum 6 cours.");
            alert.showAndWait();
            return;
        }

        // Appel au service API pour générer les horaires
        ApiService.ResultatHoraire resultat;
        try {
            resultat = coursService.genererHoraire(idsCours, session, true, choix);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Problème de récupération");
            alert.setContentText("Erreur lors de la récupération des horaires.");
            alert.showAndWait();
            return;
        }

        // Vérification si aucun horaire n'a été trouvé
        if (resultat == null || resultat.horaire == null || resultat.horaire.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Aucun horaire");
            alert.setHeaderText(null);
            alert.setContentText("Aucun horaire disponible pour les cours sélectionnés.");
            alert.showAndWait();
            return;
        }

        // Création de la fenêtre pour afficher les horaires
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Horaires des cours sélectionnés");

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 10;");

        // Affichage des horaires pour chaque cours
        for (String coursId : resultat.horaire.keySet()) {
            VBox coursBox = new VBox(5);
            coursBox.setStyle("-fx-border-color: gray; -fx-padding: 5; -fx-background-color: #f9f9f9;");
            coursBox.getChildren().add(new Label("Cours : " + coursId));

            List<List<String>> blocs = resultat.horaire.get(coursId);
            if (blocs == null || blocs.isEmpty()) {
                coursBox.getChildren().add(new Label("Aucun horaire disponible."));
            } else {
                for (List<String> bloc : blocs) {
                    String jours = bloc.get(0);
                    String heures = bloc.get(1);
                    coursBox.getChildren().add(new Label(jours + " " + heures));
                }
            }

            root.getChildren().add(coursBox);
        }

        // Affichage des conflits, s'il y en a
        if (resultat.conflits != null && !resultat.conflits.isEmpty()) {
            VBox conflitBox = new VBox(5);
            conflitBox.setStyle("-fx-border-color: red; -fx-padding: 5; -fx-background-color: #ffe6e6;");
            conflitBox.getChildren().add(new Label("Conflits détectés :"));
            for (ApiService.ResultatHoraire.ConflitHoraireDTO conflit : resultat.conflits) {
                conflitBox.getChildren().add(new Label(conflit.toString()));
            }
            root.getChildren().add(conflitBox);
        }

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        Scene scene = new Scene(scroll, 600, 400);
        stage.setScene(scene);
        stage.show();
    }


    /**
     *  Cette méthode permet d'afficher l'horaire d'un cours pour une session donnée.
     * @param cours cours dont on veut l'horaire.
     * @param session la session en question.
     */

    public void afficherHoraire(Cours cours, String session) {
        if (cours == null || session == null || session.isEmpty()) return;

        ApiService api = new ApiService();
        Map<String, Object> horaires = api.getCourseSchedule(cours.getId(), session);

        Stage stage = new Stage();
        stage.setTitle("Horaire - " + cours.getId());

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.getChildren().add(new Label("Horaire pour le cours " + cours.getId() + " (" + session + ") :"));

        if (horaires.isEmpty()) {
            layout.getChildren().add(new Label("Aucun horaire trouvé pour ce cours."));
        } else {
            // Parcours des sections
            horaires.forEach((section, sectionObj) -> {
                layout.getChildren().add(new Label(section.toString()));

                if (sectionObj instanceof Map<?, ?> voletsMap) {
                    // On parcourt chaque volet ou champ du map
                    voletsMap.forEach((voletKey, voletValue) -> {
                        if (voletValue instanceof Map<?, ?> horaireMap) {
                            // Si c'est un horaire ou un sous-volet
                            horaireMap.forEach((k, v) -> {
                                if (v instanceof Map<?, ?> sousMap) {
                                    // Horaire détaillé
                                    String lblText = k + " → " +
                                            getSafe(sousMap, "Jours :") + " " +
                                            getSafe(sousMap, "Heures : ") + " | " +
                                            getSafe(sousMap, "Salle : ") + " | " +
                                            "Mode: " + getSafe(sousMap, "Mode d'enseignement : ") +
                                            " | Campus: " + getSafe(sousMap, "Campus : ") +
                                            " | Début: " + getSafe(sousMap, "Date de debut : ") +
                                            " | Fin: " + getSafe(sousMap, "Date de fin : ");
                                    Label lbl = new Label(lblText);
                                    lbl.setStyle("-fx-font-size: 13px;");
                                    layout.getChildren().add(lbl);
                                } else if (k.toString().startsWith("Volets")) {
                                    // On peut afficher le type de volet (Intra, Final, TH, TP, etc.)
                                    Label lbl = new Label(voletKey + " → " + v.toString());
                                    lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
                                    layout.getChildren().add(lbl);
                                }
                            });
                        } else {
                            // Champs simples comme Professeur(s), Places restantes, Capacité
                            Label lbl = new Label(voletKey + " → " + (voletValue != null ? voletValue.toString() : "N/A"));
                            lbl.setStyle("-fx-font-size: 13px;");
                            layout.getChildren().add(lbl);
                        }
                    });
                }
            });
        }

        Scene scene = new Scene(new ScrollPane(layout), 500, 400);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Cette méthode est une méthode utilitaire permettant d'éviter les réponses null.
     * @param map le dictionnaire
     * @param key la clé
     * @return chaine de caractères N/A à la place de null.
     */
    private String getSafe(Map<?, ?> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "N/A";
    }



    // getters pour le Main
    public TextField getChampRecherche() { return champRecherche; }
    public ListView<Cours> getListeResultats() { return listeResultats; }
    public TextField getSessionField() { return sessionField;}
    public void resetResultats() {
        listeResultats.getItems().clear();
        messageLabel.setText("");
    }


}
