package client;

import client.controller.ClientController;
import client.service.ApiService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.projet.model.Cours;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cette classe permet de gérer la logique de la vue pour l'interface du client.
 */
public class MainApp extends Application {

    private BorderPane root;
    private ClientController clientController;
    private VBox rechercheLayout;
    private VBox resultContainer;
    private VBox listeCoursProgrammeBox;


    /**
     * Point d'entrée de l'application JavaFX.
     * Cette méthode initialise la scène, la barre de navigation
     * et affiche la page d'accueil par défaut.
     * @param stage la fenêtre principale de l'application
     */
        @Override
    public void start(Stage stage) {
        root = new BorderPane();

        //top barr
        GridPane topBar = new GridPane();
        topBar.setPadding(new Insets(30, 0, 30, 0));
        topBar.setStyle("-fx-background-color: #623E32;");
        topBar.setPrefHeight(60);

        String[] labels = {
                "Accueil", "Rechercher", "Programmes", "Avis",
                "Comparaison", "Résultats Académiques", "Horaire"
        };

        Label[] menuItems = new Label[labels.length];
        for (int i = 0; i < labels.length; i++) {
            Label lbl = new Label(labels[i]);
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(Pos.CENTER);
            lbl.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            final int idx = i;

            lbl.setOnMouseEntered(e -> lbl.setStyle(
                    "-fx-text-fill: #1abc9c; -fx-font-size: 14px; -fx-underline: true;"));
            lbl.setOnMouseExited(e -> lbl.setStyle(
                    "-fx-text-fill: white; -fx-font-size: 14px;"));

            menuItems[i] = lbl;
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / labels.length);
            cc.setHgrow(Priority.ALWAYS);
            topBar.getColumnConstraints().add(cc);
            topBar.add(lbl, i, 0);
        }

        // Actions
        menuItems[0].setOnMouseClicked(e -> afficherAccueil());
        menuItems[1].setOnMouseClicked(e -> afficherRecherche());
        menuItems[2].setOnMouseClicked(e -> afficherCoursProgramme());
         menuItems[3].setOnMouseClicked(e -> {
            if (clientController == null) clientController = new ClientController();
            clientController.afficherAllAvis(); // ouvre la fenêtre séparée
        });

        menuItems[4].setOnMouseClicked(e -> {
            if (clientController == null) clientController = new ClientController();
            root.setCenter(clientController.getVueComparaison());
        });
        menuItems[5].setOnMouseClicked(e -> {
            if (clientController == null) clientController = new ClientController();
            root.setCenter(clientController.afficherResultatsAcademiques());
        });
        menuItems[6].setOnMouseClicked(e -> afficherHoraireDialog());

        root.setTop(topBar);

        // Contenu principal
        afficherAccueil();

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setMinWidth(1800);
        stage.setMinHeight(1000);
        stage.setTitle("PickCourse");
        stage.setMaximized(true);
        stage.setResizable(true);
        stage.show();
    }
    /**
     * Affiche la page d'accueil de l'application.
     * Cette vue contient un texte de bienvenue, une description
     * et des boutons permettant de naviguer rapidement.
     */
    private void afficherAccueil() {
        HBox accueil = new HBox();
        accueil.setPadding(new Insets(40));
        accueil.setSpacing(50);
        accueil.setStyle("-fx-background-color: #f4f4f4;"); // couleur de fond
        accueil.setAlignment(Pos.CENTER);

        // Texte à gauche
        VBox textBox = new VBox(20);
        textBox.setAlignment(Pos.CENTER_LEFT);


        Text text1 = new Text("Bienvenue sur Pick");
        text1.setStyle("-fx-font-size: 60px; -fx-font-weight: bold; -fx-fill: black;");

        Text text2 = new Text("Course");
        text2.setStyle("-fx-font-size: 60px; -fx-font-weight: bold; -fx-fill: #623E32;");

        TextFlow flow = new TextFlow(text1, text2);

        Label subtitle1 = new Label("La plateforme par excellence pour les étudiants de l'Université de Montréal souhaitant s'inscrire à des cours.");
        subtitle1.setStyle("-fx-font-size: 25px; -fx-text-fill: #555;");
        subtitle1.setWrapText(true);
        subtitle1.setMaxWidth(600);
        Label subtitle = new Label("Comparez, explorez et choisissez ces derniers en un clic!");
        subtitle.setStyle("-fx-font-size: 25px; -fx-text-fill: #555;");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(600);

        HBox actions = new HBox(20);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button btnRecherche = new Button("Rechercher un cours");
        btnRecherche.setStyle("-fx-background-color: #623E32; -fx-text-fill: white; -fx-font-size: 20px; -fx-padding: 20 25;");
        btnRecherche.setOnAction(e -> afficherRecherche());

        Button btnProgramme = new Button("Voir les programmes");
        btnProgramme.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-size: 20px; -fx-padding: 20 25;");
        btnProgramme.setOnAction(e -> afficherCoursProgramme());

        actions.getChildren().addAll(btnRecherche, btnProgramme);
        textBox.getChildren().addAll(flow, subtitle1, subtitle, actions);

        // Big image à droite
        Image img = new Image(getClass().getResourceAsStream("/PickCourse-logo.png"));
        ImageView imageView = new ImageView(img);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(500); // ajuste pour couvrir la hauteur
        imageView.setFitWidth(600);

        VBox imageBox = new VBox(imageView);
        imageBox.setAlignment(Pos.CENTER_RIGHT);
        // on les ajoute côte à côte
        accueil.getChildren().addAll(textBox, imageBox);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        HBox.setHgrow(imageBox, Priority.ALWAYS);

        root.setCenter(accueil);
    }

    /**
     * Cette méthode affiche l'interface permettant de
     * rechercher les programmes et de consulter les
     * cours associés.
     */
    private void afficherCoursProgramme() {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20;");

        Label title = new Label("Voir les cours d'un programme");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField champProgramme = new TextField();
        champProgramme.setPromptText("Entrez l'ID ou le nom du programme");

        TextField champSession = new TextField();
        champSession.setPromptText("Entrez la session (optionnel)");

        ScrollPane scrollPane = new ScrollPane();
        listeCoursProgrammeBox = new VBox(5);
        listeCoursProgrammeBox.setStyle("-fx-padding: 10; -fx-background-color: #f9f9f9; -fx-spacing: 5;");
        scrollPane.setContent(listeCoursProgrammeBox);
        scrollPane.setFitToWidth(true);

        Button btnLancer = new Button("Afficher les programmes");
        btnLancer.setStyle("-fx-background-color: #623E32; -fx-text-fill: white;");

        btnLancer.setOnAction(e -> {
            String input = champProgramme.getText().trim();
            String session = champSession.getText().trim();
            if (input.isEmpty()) return;

            new Thread(() -> {
                ApiService api = new ApiService();

                if (input.matches("\\d+")) { // C'est un ID → affichage direct des cours
                    Platform.runLater(() -> afficherCoursPourProgramme(input, session));
                } else { // C'est un nom → recherche des programmes
                    List<ApiService.ProgrammeDTO> programmes = api.rechercherProgrammesParNom(input);

                    Platform.runLater(() -> {
                        listeCoursProgrammeBox.getChildren().clear();

                        if (programmes.isEmpty()) {
                            listeCoursProgrammeBox.getChildren().add(new Label("Aucun programme trouvé."));
                            return;
                        }

                        for (ApiService.ProgrammeDTO p : programmes) {
                            HBox hbox = new HBox();
                            hbox.setStyle("-fx-padding: 5; -fx-border-color: #3498db; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-background-color: #eaf2f8;");
                            Label lbl = new Label(p.name);
                            lbl.setStyle("-fx-text-fill: #3498db; -fx-cursor: hand; -fx-font-size: 14;");
                            lbl.setOnMouseClicked(ev -> afficherCoursPourProgramme(p.id, session));
                            hbox.getChildren().add(lbl);
                            listeCoursProgrammeBox.getChildren().add(hbox);
                        }
                    });
                }
            }).start();
        });

        layout.getChildren().addAll(
                title,
                champProgramme,
                new Label("Session (optionnel) :"),
                champSession,
                btnLancer,
                scrollPane
        );

        root.setCenter(layout);
    }

    /**
     * Affiche les cours associés à un programme donné.
     *
     * @param programmeId identifiant du programme
     * @param session session sélectionnée par l'utilisateur
     */
    private void afficherCoursPourProgramme(String programmeId, String session) {
        listeCoursProgrammeBox.getChildren().clear();
        listeCoursProgrammeBox.getChildren().add(new Label("Chargement des cours..."));

        new Thread(() -> {
            ApiService api = new ApiService();
            List<Cours> cours;

            if (session != null && !session.isEmpty()) {
                List<String> ids = api.getCoursesBySemester(programmeId, session);
                cours = ids.stream()
                        .map(api::rechercherCoursParSigle)
                        .filter(c -> c != null)
                        .toList();
            } else {
                cours = api.getCoursesForAProgram(programmeId);
            }

            Platform.runLater(() -> {
                listeCoursProgrammeBox.getChildren().clear();

                if (cours.isEmpty()) {
                    listeCoursProgrammeBox.getChildren().add(new Label("Aucun cours trouvé."));
                } else {
                    Label titreCours = new Label("Cours pour le programme : " + programmeId);
                    titreCours.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 5 0 10 0;");
                    listeCoursProgrammeBox.getChildren().add(titreCours);

                    for (Cours c : cours) {
                        HBox hbox = new HBox();
                        hbox.setStyle("-fx-padding: 3 0 3 10;");
                        Label lbl = new Label(c.getId() + " - " + c.getName());
                        lbl.setStyle("-fx-text-fill: #2c3e50; -fx-cursor: hand;");
                        lbl.setOnMouseClicked(e -> afficherCoursDetail(c, session));
                        hbox.getChildren().add(lbl);
                        listeCoursProgrammeBox.getChildren().add(hbox);
                    }
                }
            });
        }).start();
    }

    /**
     * Affiche les détails d'un cours sélectionné dans l'interface programme..
     *
     * @param cours cours à afficher
     * @param session session associée
     */

    private void afficherCoursDetail(Cours cours, String session) {
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

        Button btnHoraire = new Button("Voir horaire");
        btnHoraire.setOnAction(e -> {
            if (clientController == null) clientController = new ClientController();
            clientController.afficherHoraire(cours, session); // passer la session
        });

        Button btnAvis = new Button("Voir avis");
        btnAvis.setOnAction(e -> {
            if (clientController == null) clientController = new ClientController();
            clientController.afficherAvis(cours);
        });

        HBox boutons = new HBox(10, btnHoraire, btnAvis);
        container.getChildren().addAll(titre, description, credits, prereq, equiv, concom, boutons);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        root.setCenter(scroll);
    }

    /**
     * Affiche l'interface de recherche de cours.
     * Cette méthode initialise les champs et les résultats
     * s'ils ne sont pas déjà créés.
     */
    private void afficherRecherche() {
        if (clientController == null) {
            clientController = new ClientController();
        }

        if (rechercheLayout != null) {
            root.setCenter(rechercheLayout);
            return;
        }

        rechercheLayout = new VBox(10);
        rechercheLayout.setStyle("-fx-padding: 20;");

        Label title = new Label("Recherche de cours");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button btnLancerRecherche = new Button("Rechercher");
        btnLancerRecherche.setStyle("-fx-background-color: #623E32; -fx-text-fill: white;");

        btnLancerRecherche.setOnAction(e -> {
            //  vider les anciens résultats
            Platform.runLater(() -> clientController.resetResultats());

            new Thread(() -> clientController.rechercher()).start();
        });


        //  container UNIQUE pour les résultats
        resultContainer = new VBox();
        resultContainer.getChildren().add(clientController.getListeResultats());

        rechercheLayout.getChildren().addAll(
                title,
                new Label("Type (Param) de recherche :"),
                clientController.getTypeRecherche(),
                new Label("Valeur de recherche :"),
                clientController.getChampRecherche(),
                new Label("Session (optionnel) :"),
                clientController.getChampSession(),
                btnLancerRecherche,
                clientController.getMessageLabel(),
                resultContainer
        );

        //  écoute le changement de type
        clientController.getTypeRecherche()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    resultContainer.getChildren().clear();
                    resultContainer.getChildren().add(
                            clientController.getListeResultats()
                    );
                });

        root.setCenter(rechercheLayout);
    }


    /**
     * Ouvre une fenêtre permettant à l'utilisateur
     * de saisir des cours et une session afin
     * d'afficher un horaire.
     */

    private void afficherHoraireDialog() {
        if (clientController == null) clientController = new ClientController();

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Afficher horaire");
        dialog.setHeaderText("Entrez les sigles des cours séparés par des virgules");
        dialog.setContentText("Cours : ");
        dialog.showAndWait().ifPresent(input -> {
            List<String> idsCours = List.of(input.split(",")).stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
            if (idsCours.isEmpty()) {
                clientController.getChampRecherche().setText("Aucun cours saisi.");
                return;
            }

            TextInputDialog sessionDialog = new TextInputDialog("A24");
            sessionDialog.setTitle("Session");
            sessionDialog.setHeaderText("Entrez la session");
            sessionDialog.setContentText("Session : ");
            sessionDialog.showAndWait().ifPresent(session -> {
                // Création fenêtre choix TH/TP
                Stage choixStage = new Stage();
                choixStage.setTitle("Choisir les sections");
                VBox layout = new VBox(10);
                layout.setPadding(new Insets(15));

                Map<String, ComboBox<String>> thMap = new HashMap<>();
                Map<String, ComboBox<String>> tpMap = new HashMap<>();

                for (String coursId : idsCours) {
                    HBox coursBox = new HBox(10);
                    coursBox.setAlignment(Pos.CENTER_LEFT);

                    Label lblCours = new Label(coursId);
                    lblCours.setPrefWidth(100);

                    ComboBox<String> thCombo = new ComboBox<>();
                    thCombo.getItems().addAll("A", "B", "C");
                    thCombo.setValue("A");
                    thMap.put(coursId, thCombo);

                    ComboBox<String> tpCombo = new ComboBox<>();
                    tpCombo.getItems().addAll("A101", "A102", "B101");
                    tpCombo.setValue("A101");
                    tpMap.put(coursId, tpCombo);

                    coursBox.getChildren().addAll(lblCours, new Label("TH:"), thCombo, new Label("TP:"), tpCombo);
                    layout.getChildren().add(coursBox);
                }

                Button btnValider = new Button("Valider");
                btnValider.setOnAction(ev -> {
                    Map<String, Map<String, String>> choix = new HashMap<>();
                    for (String coursId : idsCours) {
                        Map<String, String> sections = new HashMap<>();
                        sections.put("TH", thMap.get(coursId).getValue());
                        sections.put("TP", tpMap.get(coursId).getValue());
                        choix.put(coursId, sections);
                    }

                    clientController.afficherHorairesEnsemble(idsCours, session, choix);
                    choixStage.close();
                });

                layout.getChildren().add(btnValider);
                Scene scene = new Scene(new ScrollPane(layout), 400, 300);
                choixStage.setScene(scene);
                choixStage.show();
            });
        });
    }
    /**
     * Méthode main de lancement de l'application.
     *
     * @param args arguments de lancement
     */
    public static void main(String[] args) {
        launch();
    }
}