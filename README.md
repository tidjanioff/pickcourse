# Projet pour le cours IFT2255

# Description du projet 

Ce projet vise à concevoir une **plateforme**, accessible via une **API REST** en interne,  et via une interface graphique nommée PickCourse,
destinée aux étudiants de l’Université de Montréal 
afin de les aider à faire des **choix de cours éclairés**.

La plateforme combine:

- des **données officielles** (résultats académiques, informations provenant de l’API Planifium),
- et des **données inofficielles collectées auprès des étudiants**, via Discord.

L'objectif est de permettre aux étudiants d'obtenir une expérience fluide et fiable tout au long de leur entreprise de recherche de cours, en leur permettant de :

- comparer plusieurs cours ainsi que plusieurs chox de cours selon la charge de travail inofficielle dont l'estimation est basée sur les commentaires d'autres étudiants, la difficulté perçue, le score moyen ou encore la popularité, tous deux basés sur les résultats académiques officiels. Ils peuvent également comparer des cours selon des critères un peu plus généraux comme le nombre de crédits, les sessions de disponibilité et surtout, les horaires;
- rechercher des cours d'un certain programme;
- consulter les avis relatifs à des cours;
- voir les résultats académiques officiels propres à des cours;
- Voir l'horaire d'un cours donné pour une session spécifique. 

# Structure du projet

Le répertoire est organisé comme suit :

- **.github/workflows** :
  Ce dossier contient la configuration CI permettant d'automatiser l'exécution des tests.

- **.history** :
  Ce dossier fait partie des éléments importés à partir du modèle MkDocs fourni pour ce devoir.

- **.idea** :
  Ce dossier fait partie des éléments importés à partir du modèle MkDocs fourni pour ce devoir.

- **diagrammes** :
  Ce dossier contiendra les différents diagrammes que nous aurons à realiser dans le cadre de notre projet. Le dossier contiendra les images de nos differents diagrammes.

- **docs** : 
  Ce dossier contient tous les fichiers Markdown du site pour notre rapport construit avec [MkDocs](https://www.mkdocs.org/) et le thème [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/).

  - **Pour exécuter le rapport, veuillez suivre les instructions plus bas dans la section **EXECUTION DU RAPPORT MKDOCS** .**

- **IFT2255_Implementation** :
  Ce dossier contient notre implementation pour le projet via une API-REST avec Javalin. La structure est inspiré du template fournis par l'enseignant et les auxiliaires.
  La structure est organisée suivant une architecture MVC (Model–View–Controller). On aura la structure suivante :

  - **src** :
    - **main** :
      - **java** :
         - **org/projet**:
           - **Model (`model/`)** : Représentation des entités du domaine (ex. User, Course).
           - **Controller (`controller/`)** : Gestion des requêtes HTTP et appels au service.
           - **Service (`service/`)** : Logique métier central.
           - **Util (`util/`)** : Fonctions utilitaires réutilisables (validation, réponses, etc.).
           - **Config (`config/`)** : Configuration du serveur et définition des routes.
           - **`Main.java`** : Point d’entrée du serveur (initialise Javalin et enregistre les routes).
      - **resources** :
        Ce dossier contient les fichiers JSON utilisés pour stocker de manière permanente nos données.
      - **client** :
         Ce dossier contient la logique pour l'interface GUI.
    - **test** :
      - **java** :
        - **Model** : Contient les tests pour les classes du fichier **`main/java/org/example/model`**.
        - **Controller** : Contient les tests pour les classes du fichier **`main/java/org/example/controller`**.
        - **Service** : Contient les tests pour les classes du fichier **`main/java/org/example/service`**.
        - **Util** : Contient les tests pour les classes du fichier **`main/java/org/example/util`**.
        - **Config** : Contient les tests pour les classes du fichier **`main/java/org/example/config`**.
    - **pom.xml** : 
      Fichier contenant les dépendances Maven.
      - **Avis.json** :
      - Fichier contenant la liste de tous les avis obtenus depuis Discord.

- **.gitignore** :
  Spécifie quelles fichiers sont ignorer par git.

- **mkdocs.yml** :
  Ce fichier contient la configuration de MkDocs.

- **Pipfile** :
  Cet élément a été importé à l’aide du modèle MkDocs fourni pour ce devoir.


- **requirements** :
  Ce fichier contient les dépendances Python.

# Tests de fonctionnalités

Les fonctionnalités testées sont les suivantes :

- Voir les cours offerts dans un programme;
- Voir les cours offerts pour un trimestre donné;
- Vérifier son éligibilité à un cours;
- Voir les résultats académiques d'un cours;
- Comparer deux cours;
- Créer un ensemble de cours; 
- Voir l'horaire d'un cours pour un trimestre donné.
  
# Documentation pour l'interface graphique de l'application

L'interface graphique a été developpée avec javaFX. Nous utilisons la dépendance maven associée à ce module, et pour afficher l'interface on procède comme suit :
- S'assurer d'être dans le dossier IFT2255_Implementation/ ( car ce dernier contient le pom.xml);
- Lancer l'API Rest ( depuis le main.java comme spécifié plus bas dans la documentation du backend)
- Utiliser la commande mvn clean javafx:run;

Une fenêtre s'ouvrira après quelques secondes.
  **NB** : Certaines actions de l'utilisateur ouvrent une deuxième fenêtre ( par exemple dans la section "Horaire", lorsqu'on veut obtenir l'horaire pour un ensemble de cours, une deuxième fenêtre s'ouvre). Sans fermer cette deuxième fenêtre, la fenêtre principale reste figée. Il faut alors toujours fermer la fenêtre secondaire afin de continuer de naviguer dans la fenêtre principale.

# Documentation pour le bot Discord

Notre bot Discord, **Danielle**, a été developpé avec Python. Pour le lancer, on utilise un environnement virtuel dans lequel on installe toutes les dépendances python au préalable, mais cela n'est pas nécessaire si vous avez déjà toutes les dépendances installées sur votre machine principale. Pour run le bot, il faut d'abord run l'API REST ( depuis le fichier main.java comme spécifié plus bas dans la documentation du backend), puis créer un fichier .env qui contient TOKEN=TOKEN_DISCORD. Le token_discord a été joint lors de la remise sur StudiUM. Ensuite, run le main en utilisant la commande python3 main.py.

Le serveur utilisé est *AvisPickCourse*, et vous recevrez un message de bienvenue de la part de Danielle détaillant comment soumettre un avis ( on utilise la commande /avis dans le serveur).

##  Utilisation dans Discord

1. Ouvrir ton **serveur Discord** et accéder au serveur **AvisPickCourse**.
2. Taper `/avis` → Discord ouvrira un **formulaire** avec les champs :

| Champ | Type | Obligatoire | Description |
|-------|------|--------------|-------------|
|Sigle du cours| texte | ✅ | Sigle du cours (ex: IFT2255) |
| commentaire | texte| ✅ | Avis personnel |
| Nom du professeur | texte | ✅ | nom du professeur |
| note difficulté | entier | ✅ |( entier entre 1 et 5) |
| charge de travail | entier | ✅ |( entier entre 1 et 5) |

---

##  Exemple de réponse du bot

Une fois le formulaire soumis, un message est envoyé dans le serveur afin que les autres étudiants puissent aussi voir les avis depuis le serveur. 
```
✅ Merci pour ton avis sur IFT2255 (H2025) avec Dupont.
- Difficulté : 3/5
- Qualité du cours : 4/5
- Charge de travail : 3/5
- Note obtenue : 85
- Commentaire : super cours !
```

---
## Côté API (backend Java)
L'avis est récupéré et stocké dans Avis.json.
# Documentation pour l'API REST développée avec Javalin
## Routes pour notre architecture REST
Pour tester les routes, on utilise POSTMAN après avoir run le fichier main.java dans src/main/java,
Chacune des routes que nous avons définies pour notre architecture REST couvrent des fonctionnalités énoncés. Nous travaillons su rle port 7070.  On a donc :
1. Rechercher des cours : **`POST /cours/rechercher`**
   - **Format pour le body de la requête :**
     -  ` {"param" : Param ,"valeur" : "IFT1025", "includeSchedule": "false","semester":  String}`
   - Les valeurs possibles de Param sont : `id`, `name` et `description`.
   - **Exemple de Body JSON attendu :**
        - `{"param" : "id", "valeur" : "IFT1025", "includeSchedule": "false", "semester":  null }`
        ou encore
        - `{"param" : "id","valeur" : "IFT1025", "includeSchedule": "true","semester":  "A24"}`
    - **Exemple de réponse JSON**: **(statut 200)**
      - `[{"id": "IFT1025","description": "Concepts avancés : classes, objets, héritage, interfaces, réutilisation, événements. Introduction aux structures de données et algorithmes : listes, arbres binaires, fichiers, recherche et tri. Notions d'analyse numérique : précision.","name": "Programmation 2",
      "scheduledSemester": null,"prerequisite_courses": ["IFT1015", "IFT1016"],"equivalent_courses": [],"concomitant_courses": [],"udemWebsite": null,
      "credits": 3.0,"requirement_text": "prerequisite_courses : IFT1015 ou IFT1016",
      "available_terms": {
      "autumn": true,
      "winter": true,
      "summer": true
      },
      "available_periods": {
      "daytime": true,
      "evening": false
      },
      "schedules": []
      }
      ]`
    - **Exemple de réponse JSON : (status 400)**:
      - Cours pas trouvé. Veuillez réessayer. Pour rappel, les paramètres possibles sont id, name et description.

2. Voir les cours offerts dans un programme : **`GET /cours-programme/{id}`**
   - Le parametre **`id`** corresponds a l'ID du programme dont l'on desire obtenir les cours.
   - **Exemple de requête attendu :**
     - **`/cours-programme/117510`**
   - **Exemple de réponse JSON**: **(statut 200)**
     - `[
       "IFT1005",
       "IFT1015",
       "IFT1025",
       "IFT2015",
       "IFT2035",
       "IFT1215",
       "IFT1227",
       "IFT2245",
       "IFT2255",
       "IFT1065",
       "IFT1575",
       "IFT2105",
       "IFT2125",
       "MAT1400",
       "MAT1600",
       "MAT1978",
       "ANG1903",
       "ANG1913",
       "ANG1921",
       "ANG1924",
       "ANG1926",
       "ANG1933",
       "BCM1501",
       "BCM1503",
       "BIO1203",
       "COM2001",
       "DRT1002S",
       "ECN1000",
       "ECN1050",
       "ECN2230",
       "GEO1532",
       "GEO1542",
       "HEC3015",
       "HEC3017",
       "LCO2030",
       "LNG1080",
       "LNG1955",
       "MAT2450",
       "MAT2531",
       "MCB1979",
       "PHI1130",
       "PHI2005",
       "PHY1651",
       "PSY2055",
       "PSY2065",
       "RED2000",
       "REI1010",
       "IFT2905",
       "IFT2935",
       "IFT3911",
       "IFT3913",
       "IFT2425",
       "IFT2505",
       "IFT3065",
       "IFT3155",
       "IFT3205",
       "IFT3225",
       "IFT3245",
       "IFT3275",
       "IFT3295",
       "IFT3325",
       "IFT3335",
       "IFT3355",
       "IFT3375",
       "IFT3395",
       "IFT3515",
       "IFT3545",
       "IFT3655",
       "IFT3700",
       "STT2700",
       "IFT3150",
       "IFT3151",
       "IFT3710",
       "IFT2905",
       "IFT2935",
       "IFT3911",
       "IFT3913",
       "IFT2550",
       "IFT3550",
       "IFT3551",
       "IFT2425",
       "IFT2505",
       "IFT3065",
       "IFT3155",
       "IFT3205",
       "IFT3225",
       "IFT3245",
       "IFT3275",
       "IFT3295",
       "IFT3325",
       "IFT3335",
       "IFT3355",
       "IFT3375",
       "IFT3395",
       "IFT3515",
       "IFT3545",
       "IFT3655",
       "IFT3700",
       "STT2700",
       "IFT2425",
       "IFT2505",
       "IFT3245",
       "IFT3395",
       "IFT3655",
       "IFT3700",
       "IFT4055",
       "BIN6002",
       "BIN6003",
       "IFT6010",
       "IFT6042",
       "IFT6080",
       "IFT6095",
       "IFT6145",
       "IFT6150",
       "IFT6155",
       "IFT6172",
       "IFT6180",
       "IFT6195",
       "IFT6232",
       "IFT6251",
       "IFT6255",
       "IFT6261",
       "IFT6271",
       "IFT6291",
       "IFT6292",
       "IFT6299",
       "IFT6521",
       "IFT6551",
       "IFT6561",
       "IFT2905",
       "IFT2935",
       "IFT3065",
       "IFT3151",
       "IFT3155",
       "IFT3205",
       "IFT3225",
       "IFT3275",
       "IFT3295",
       "IFT3325",
       "IFT3335",
       "IFT3355",
       "IFT3375",
       "IFT3515",
       "IFT3545",
       "IFT3710",
       "IFT3911",
       "IFT3913",
       "STT2700"
       ]`
   - **Exemple de réponse JSON : (status 400)**:
     - `{"error": "Les paramètres fournis sont invalides ou le programme n'existe pas."}`

3. Voir les cours offerts pour un trimestre donné : **`GET /programme/courseBySemester/{id}/{session}`**
    - Le parametre **`id`** corresponds a l'ID du programme dont l'on desire obtenir les cours.
    - Le parametre **`session`** correspond au trimestre pour lequel l'utilisateur souhaite effectuer la recherche.
    - **Exemple de requête attendu :**
        - **`programme/courseBySemester/117510/A25`**
    - **Exemple de réponse JSON**: **(statut 200)**
        - `[
    "IFT1005",
    "IFT1015",
    "IFT1025",
    "IFT2015",
    "IFT2035",
    "IFT1215",
    "IFT1227",
    "IFT2255",
    "IFT1065",
    "IFT1575",
    "IFT2105",
    "IFT2125",
    "MAT1400",
    "MAT1600",
    "MAT1978",
    "ANG1903",
    "ANG1921",
    "ANG1924",
    "ANG1926",
    "ANG1933",
    "BCM1501",
    "BIO1203",
    "COM2001",
    "ECN1000",
    "ECN1050",
    "GEO1532",
    "LNG1080",
    "PHY1651",
    "PSY2055",
    "PSY2065",
    "REI1010",
    "IFT3913",
    "IFT3155",
    "IFT3275",
    "IFT3295",
    "IFT3325",
    "IFT3355",
    "IFT3395",
    "IFT3700",
    "STT2700",
    "IFT3150",
    "IFT3151",
    "IFT3913",
    "IFT2550",
    "IFT3155",
    "IFT3275",
    "IFT3295",
    "IFT3325",
    "IFT3355",
    "IFT3395",
    "IFT3700",
    "STT2700",
    "IFT3395",
    "IFT3700",
    "IFT4055",
    "IFT6150",
    "IFT6155",
    "IFT6255",
    "IFT6271",
    "IFT6291",
    "IFT3151",
    "IFT3155",
    "IFT3275",
    "IFT3295",
    "IFT3325",
    "IFT3355",
    "IFT3913",
    "STT2700"
    ]`
    - **Exemple de réponse JSON : (status 400)**:
        - `{"error": "Les paramètres fournis sont invalides ou le programme n'existe pas."}`
4. Voir l'horaire d'un cours pour un trimestre donné : **`GET /cours/horaires/{id}/{session}`**
    - Le parametre **`id`** corresponds a l'ID du cours.
    - Le parametre **`session`** correspond au trimestre pour lequel l'utilisateur souhaite effectuer la recherche.
    - **Exemple de requête attendu :**
        - **`/cours/horaires/IFT2255/A25`**
    - **Exemple de réponse JSON**: **(statut 200)**
        - `{
    "Section : A": {
        "Professeur(s) :": "Delcourt ,Kévin; ",
        "Volet (2) :": {
            "Volets : ": "Intra",
            "Horaire (1) :": {
                "Date de fin : ": "2025-10-17",
                "Campus : ": "Montréal",
                "Date de debut : ": "2025-10-17",
                "Heures : ": "09:30 - 11:29",
                "Jours :": "Vendredi ; ",
                "Salle : ": "B-2325 3200 Jean-Brillant",
                "Mode d'enseignement : ": "P"
            }
        },
        "Volet (3) :": {
            "Volets : ": "Final",
            "Horaire (1) :": {
                "Date de fin : ": "2025-12-12",
                "Campus : ": "Montréal",
                "Date de debut : ": "2025-12-12",
                "Heures : ": "13:30 - 16:29",
                "Jours :": "Vendredi ; ",
                "Salle : ": "1360 André-Aisenstadt",
                "Mode d'enseignement : ": "P"
            }
        },
        "Volet (1) :": {
            "Horaire (2) :": {
                "Date de fin : ": "2025-12-09",
                "Campus : ": "Montréal",
                "Date de debut : ": "2025-10-27",
                "Heures : ": "13:30 - 15:29",
                "Jours :": "Mardi ; ",
                "Salle : ": "B-3335 3200 Jean-Brillant",
                "Mode d'enseignement : ": "P"
            },
            "Horaire (3) :": {
                "Date de fin : ": "2025-10-10",
                "Campus : ": "Montréal",
                "Date de debut : ": "2025-09-02",
                "Heures : ": "08:30 - 09:29",
                "Jours :": "Vendredi ; ",
                "Salle : ": "N-515 Roger-Gaudry (B)",
                "Mode d'enseignement : ": "P"
            },
            "Horaire (4) :": {
                "Date de fin : ": "2025-12-09",
                "Campus : ": "Montréal",
                "Date de debut : ": "2025-10-27",
                "Heures : ": "08:30 - 09:29",
                "Jours :": "Vendredi ; ",
                "Salle : ": "N-515 Roger-Gaudry (B)",
                "Mode d'enseignement : ": "P"
            },
            "Volets : ": "TH",
            "Horaire (1) :": {
                "Date de fin : ": "2025-10-17",
                "Campus : ": "Montréal",
                "Date de debut : ": "2025-09-02",
                "Heures : ": "13:30 - 15:29",
                "Jours :": "Mardi ; ",
                "Salle : ": "B-3335 3200 Jean-Brillant",
                "Mode d'enseignement : ": "P"
            }
        },
        "Places restantes :": "26",
        "Capacité:": "90"
    },
    "Section : B": {
        "Professeur(s) :": "Lafontant ,Louis Edouard; ",
        "Volet (2) :": {
            "Volets : ": "Intra",
            "Horaire (1) :": {
                "Date de fin : ": "2025-10-17",
                "Campus : ": "Montréal",
                "Date de debut : ": "2025-10-17",
                "Heures : ": "09:30 - 11:29",
                "Jours :": "Vendredi ; ",
                "Salle : ": " ",
                "Mode d'enseignement : ": "P"
            }
        },
        "Volet (3) :": {
            "Volets : ": "Final",
            "Horaire (1) :": {
                "Date de fin : ": "2025-12-12",
                "Campus : ": "Montréal",
                "Date de debut : ": "2025-12-12",
                "Heures : ": "13:30 - 16:29",
                "Jours :": "Vendredi ; ",
                "Salle : ": "B-4325 3200 Jean-Brillant",
                "Mode d'enseignement : ": "P"
            }
        },
        "Volet (1) :": {
            "Horaire (2) :": {
                "Date de fin : ": "2025-12-09",
                "Campus : ": "Montréal",
                "Date de debut : ": "2025-10-27",
                "Heures : ": "10:30 - 12:29",
                "Jours :": "Lundi ; ",
                "Salle : ": "Y-115 Roger-Gaudry (B)",
                "Mode d'enseignement : ": "P"
            },
            "Horaire (3) :": {
                "Date de fin : ": "2025-10-10",
                "Campus : ": "Montréal",
                "Date de debut : ": "2025-09-02",
                "Heures : ": "11:30 - 12:29",
                "Jours :": "Vendredi ; ",
                "Salle : ": "1140 André-Aisenstadt",
                "Mode d'enseignement : ": "P"
            },
            "Horaire (4) :": {
                "Date de fin : ": "2025-12-09",
                "Campus : ": "Montréal",
                "Date de debut : ": "2025-10-27",
                "Heures : ": "11:30 - 12:29",
                "Jours :": "Vendredi ; ",
                "Salle : ": "1140 André-Aisenstadt",
                "Mode d'enseignement : ": "P"
            },
            "Volets : ": "TH",
            "Horaire (1) :": {
                "Date de fin : ": "2025-10-10",
                "Campus : ": "Montréal",
                "Date de debut : ": "2025-09-02",
                "Heures : ": "10:30 - 12:29",
                "Jours :": "Lundi ; ",
                "Salle : ": "Y-115 Roger-Gaudry (B)",
                "Mode d'enseignement : ": "P"
            }
        },
        "Places restantes :": "30",
        "Capacité:": "90"
    },
    "Section : B101": {
        "Professeur(s) :": "Lafontant ,Louis Edouard; ",
        "Volet (1) :": {
            "Horaire (2) :": {
                "Date de fin : ": "2025-12-09",
                "Campus : ": "Montréal",
                "Date de debut : ": "2025-10-27",
                "Heures : ": "09:30 - 11:29",
                "Jours :": "Vendredi ; ",
                "Salle : ": "Z-260 Claire-McNicoll",
                "Mode d'enseignement : ": "P"
            },
            "Volets : ": "TP",
            "Horaire (1) :": {
                "Date de fin : ": "2025-10-10",
                "Campus : ": "Montréal",
                "Date de debut : ": "2025-09-02",
                "Heures : ": "09:30 - 11:29",
                "Jours :": "Vendredi ; ",
                "Salle : ": "Z-260 Claire-McNicoll",
                "Mode d'enseignement : ": "P"
            }
        },
        "Places restantes :": "30",
        "Capacité:": "90"
    },
    "Section : A101": {
        "Professeur(s) :": "Delcourt ,Kévin; ",
        "Volet (1) :": {
            "Horaire (2) :": {
                "Date de fin : ": "2025-12-09",
                "Campus : ": "Montréal",
                "Date de debut : ": "2025-10-27",
                "Heures : ": "09:30 - 11:29",
                "Jours :": "Vendredi ; ",
                "Salle : ": "N-515 Roger-Gaudry (B)",
                "Mode d'enseignement : ": "P"
            },
            "Volets : ": "TP",
            "Horaire (1) :": {
                "Date de fin : ": "2025-10-10",
                "Campus : ": "Montréal",
                "Date de debut : ": "2025-09-02",
                "Heures : ": "09:30 - 11:29",
                "Jours :": "Vendredi ; ",
                "Salle : ": "N-515 Roger-Gaudry (B)",
                "Mode d'enseignement : ": "P"
            }
        },
        "Places restantes :": "26",
        "Capacité:": "90"
    }
    }`
    - **Exemple de réponse JSON : (status 400)**:
        - `{"error": "Les paramètres fournis sont invalides ou le programme n'existe pas ou le cours n'existe pas."}`
5. Vérifier son éligibilité à un cours : **`POST /cours/eligibilitenew`**
    - **Format pour le body de la requête :**
        - `{ "idCours": id,"cycle": number, "listeCours": [cours1, cours2,...] }`
    - Le parametre `idCours` corresponds a l'ID du cours dont l'on souhaite verifier l'éligibilité.
    - Le parametre `cycle` correspond au cycle d'étude de la personne.
    - Le parametre `listeCours` est une liste contenant les cours deja suivie.
    - **Exemple de Body JSON attendu :**
        - `{ "idCours": "IFT2035","cycle": "1", "listeCours": ["IFT1025", "IFT2035"] }`
    - **Exemple de réponse JSON**: **(statut 200)**
        - `Vous êtes éligible à ce cours!`
        - `Vous n'êtes pas éligible à ce cours. Il vous manque les prérequis suivants : IFT1025`

6. Voir les résultats académiques d'un cours : **`POST /cours/voir/resultat`**
    - **Format pour le body de la requête :**
        - `{"sigle": id}`
    - Le parametre `sigle` correspond à l'ID du cours dont l'on souhaite obtenir les résultats.
    - **Exemple de Body JSON attendu :**
        - `{"sigle": "IFT1015"}`
    - **Exemple de réponse JSON**: **(statut 200)**
        - `Résultats pour le cours IFT1015 - Programmation 1 : Moyenne : C Score : 2.29 Participants : 658 Trimestre : 9`
        - `Désolé ! Nous n'avons trouvé aucun résultat pour le cours . Vérifiez que le sigle est correct.`
7. Voir les avis étudiants pour un cours donné : **`GET /cours/{sigle}/avis`**
    - Le parametre **`sigle`** corresponds a l'ID du cours dont l'on desire obtenir les avis.
    - **Exemple de requête attendu :**
        - **`cours/IFT1015/avis`**
    - **Exemple de réponse JSON**: **(statut 200)**
        - `[]`
    - **Exemple de réponse JSON : (status 400)**:
        - `Erreur : sigle de cours invalide`
8. Soumettre un avis pour un cours : **`POST /avis`**
    - **Format pour le body de la requête :**
        - `{"sigleCours":id,"professeur" :professor,"noteDifficulte" :difficulty,"noteQualite" : quality, "commentaire" : comment}`
    - Le parametre `sigleCours` corresponds a l'ID du cours.
    - Le parametre `professeur` correspond au professeur ayant enseigné le cours.
    - Le parametre `noteDifficulte` est une note attribue pour la difficulté. 
    - Le parametre `noteQualite` est une note attribue pour la qualité.
    - Le parametre `commentaire` correspond à un commentaire laissé par l'utilisateur.
   - **Exemple de Body JSON attendu :**
       - `{"sigleCours": "IFT1015","professeur" :"Axel Seguin","noteDifficulte" :"3","noteQualite" : "3", "commentaire" : "Cours très intéressant, devoirs challengeants et examens faciles"}`
   - **Exemple de réponse JSON**: **(statut 200)**
       - `Avis enregistré avec succès`
   - **Exemple de réponse JSON**: **(statut 400)**
       - `"L'entrée est incorrecte. Veuillez reessayer."`
9. Comparer des cours avec les résultats agrégés par rapport à la difficulté des cours (notes obtenues) : **`POST /cours/difficulte`**
    - **Format pour le body de la requête :**
        - `{"sigle":id}`
    - Le parametre `sigle` corresponds a l'ID du cours.
    - **Exemple de Body JSON attendu :**
        - `{"sigle":"IFT2035"}`
    - **Exemple de réponse JSON**: **(statut 200)**
        - `Le cours Concepts des langages de programmation est considéré comme de difficulté moyenne avec un score de 2.85/5`
10. Comparer des cours avec les résultats agrégés par rapport à la popularité des cours (nombre d'inscrits) : **`POST /cours/popularite`**
    - **Format pour le body de la requête :**
        - `{"sigle":id}`
    - Le parametre `sigle` corresponds a l'ID du cours.
    - **Exemple de Body JSON attendu :**
        - `{"sigle":"IFT2035"}`
    - **Exemple de réponse JSON**: **(statut 200)**
        - `Le cours Concepts des langages de programmation est très populaire avec 520 participants.`
11. Comparer des cours avec les résultats agrégés par rapport à la difficulté + la popularité : **`POST /cours/comparer/stats`**
    - **Format pour le body de la requête :**
        - `{ "sigle1": id1,  "sigle2": id2} `
    - Le parametre `sigle1` correspond à l'ID du premier cours pour la comparaison.
    - Le parametre `sigle2` correspond à l'ID du deuxième cours pour la comparaison.
    - **Exemple de Body JSON attendu :**
        - `{ "sigle1": "IFT1015",  "sigle2": "ANG1904"} `
    - **Exemple de réponse JSON**: **(statut 200)**
        - `{
          "popularite": "Le cours Programmation 1 est plus populaire que Anglais 4 (niveau B1.2) avec 658 participants contre 8.",
          "difficulte": "Le cours Anglais 4 (niveau B1.2) est considéré comme plus facile que Programmation 1 avec un score de 3.58/5 contre 2.29/5."
          }`.
12. Comparer des cours : **`POST /cours/comparer`**
    - **Format pour le body de la requête :**
        - `{"cours": ["idCours1", "idCours2",...],"criteres": ["critere1", "critere2",...]}`
    - **Exemple de Body JSON attendu :**
        - `{"cours": ["IFT1025", "IFT2255"],"criteres": ["name", "credits"]}`
    - **Exemple de réponse JSON :(status 200)**
      - `[["IFT1025","Introduction à l'informatique","3"],["IFT2255","Structures de données","3"]]`
    - **Exemple de Body JSON attendu :**
      - `{"cours":  ["IFT1215","ANG1933"],"criteres": ["id","mode"]}`
    - **Exemple de réponse JSON : (status 200)**
      - `[["IFT1215","IFT1215","P"],["ANG1933","ANG1933","AD, SD, MD"]]`
    - **Exemple de réponse JSON (status 400)**
      - `La comparaison n'a pas pu être effectuée. Vérifiez le format des critères de comparaison et celui des ids de Cours.`
    - **Liste de critères **:
      - Lors de l’appel à `/cours/comparer`, les **critères suivants** sont à considérer :
      - | Critère | Description |
        |----------|------------|
        | `id` | Identifiant du cours |
        | `name` | Nom complet du cours |
        | `description` | Description détaillée |
        | `credits` | Nombre de crédits |
        | `scheduledSemester` | Trimestre(s) où le cours est offert |
        | `schedules` | Horaires du cours |
        | `prerequisite_courses` | Cours prérequis |
        | `equivalent_courses` | Cours équivalents |
        | `concomitant_courses` | Cours concomitants |
        | `udemWebsite` | Lien vers le site officiel UdeM |
        | `requirement_text` | Exigences spécifiques |
        | `available_terms` | Termes disponibles |
        | `available_periods` | Périodes disponibles |
        | `mode` | Mode du cours ( P(présentiel), AD( à distance asynchrone) ou SD ( à distance synchrone)) |

13. Créer un ensemble de cours et générer l'horaire correspondant + détection de conflits horaires : **`POST /horaire`** 
    - **Format pour le body de la requête (Pour obtenir les horaires seulement) :**
      - `{"idCours": [id1,id2,...],"session": session,"sections": bool}`
    - Le parametre `idCours` correspond à un tableau contenant les ID des cours pour la generation d'horaire.
    - Le parametre `session` correspond au trimestre de recherche.
    - Le parametre `sections` correspond à `true` pour inclure les sections ou `false` sinon.
    - **Format pour le body de la requête (Pour un horaire de session final (avec les choix de sections)) :**
        - `{"idCours": [id1,id2,...],"session": session,"sections": bool, "choix" = {"id1": {"TH": section,"TP": TPSection},"id2": {"TH": section,"TP": TPSection}}}`
    - Le parametre `idCours` correspond à un tableau contenant les ID des cours pour la generation d'horaire.
    - Le parametre `session` correspond au trimestre de recherche.
    - Le parametre `sections` correspond à `true` pour inclure les sections ou `false` sinon.
    - Le parametre `choix` correspond à specifier pour chaque cours dans `idCours` la section théorique (parametre `TH`) et la section pratique (parametre `TP`).
    - **Exemple de Body JSON attendu :**
        - `{"idCours": ["IFT1015","MAT1400","IFT1215","IFT1065","IFT1005"],"session": "H26","sections": false}`
        - `{"idCours": ["IFT1015","MAT1400","IFT1215","IFT1065","IFT1005"],"session": "H26","sections": false,"choix": {"IFT1015": {"TH": "A", "TP": "A101"},"MAT1400": {"TH": "A","TP" : "A101"},"IFT1215": {"TH": "A","TP": "A101"},"IFT2015": {"TH": "A","TP": "A101"},"IFT2125": {"TH": "A","TP": "A101"}}}`
    - **Exemple de réponse JSON**: **(statut 200)**
        - `{
    "MAT1400": {
        "TH": {
            "A": [
                [
                    "[Ma]",
                    "08:30-10:29"
                ],
                [
                    "[Je]",
                    "08:30-10:29"
                ]
            ],
            "B": [
                [
                    "[Ma]",
                    "08:30-10:29"
                ],
                [
                    "[Je]",
                    "08:30-10:29"
                ]
            ]
        },
        "TP": {
            "A102": [
                [
                    "[Lu]",
                    "15:30-17:29"
                ]
            ],
            "A103": [
                [
                    "[Lu]",
                    "15:30-17:29"
                ]
            ],
            "B101": [
                [
                    "[Lu]",
                    "15:30-17:29"
                ]
            ],
            "A101": [
                [
                    "[Lu]",
                    "15:30-17:29"
                ]
            ],
            "B102": [
                [
                    "[Lu]",
                    "15:30-17:29"
                ]
            ]
        }
    },
    "IFT1065": {
        "TH": {
            "A": [
                [
                    "[Lu]",
                    "10:30-12:29"
                ],
                [
                    "[Me]",
                    "13:30-14:29"
                ]
            ],
            "B": [
                [
                    "[Lu]",
                    "10:30-12:29"
                ],
                [
                    "[Me]",
                    "14:30-15:29"
                ]
            ]
        },
        "TP": {
            "B101": [
                [
                    "[Ve]",
                    "10:30-12:29"
                ]
            ],
            "A101": [
                [
                    "[Ve]",
                    "10:30-12:29"
                ]
            ]
        }
    },
    "IFT1015": {
        "TH": {
            "A": [
                [
                    "[Lu]",
                    "13:30-15:29"
                ],
                [
                    "[Me]",
                    "11:30-12:29"
                ]
            ],
            "B": [
                [
                    "[Ma]",
                    "15:30-17:29"
                ],
                [
                    "[Je]",
                    "13:30-14:29"
                ]
            ]
        },
        "TP": {
            "A102": [
                [
                    "[Je]",
                    "15:30-17:29"
                ]
            ],
            "B101": [
                [
                    "[Je]",
                    "14:30-15:29"
                ]
            ],
            "A101": [
                [
                    "[Je]",
                    "14:30-15:29"
                ]
            ],
            "B102": [
                [
                    "[Je]",
                    "15:30-17:29"
                ]
            ]
        }
    },
    "IFT1005": {
        "TH": {
            "A": [
                [
                    "[Ma]",
                    "13:30-15:29"
                ],
                [
                    "[Ve]",
                    "08:30-09:29"
                ]
            ],
            "B": [
                [
                    "[Je]",
                    "10:30-12:29"
                ],
                [
                    "[Ve]",
                    "14:30-15:29"
                ]
            ]
        },
        "TP": {
            "B101": [
                [
                    "[Ve]",
                    "15:30-17:29"
                ]
            ],
            "A101": [
                [
                    "[Ve]",
                    "15:30-17:29"
                ]
            ]
        }
    },
    "IFT1215": {
        "TH": {
            "A": [
                [
                    "[Lu]",
                    "08:30-10:29"
                ],
                [
                    "[Me]",
                    "08:30-09:29"
                ]
            ]
        },
        "TP": {
            "A101": [
                [
                    "[Me]",
                    "09:30-11:29"
                ]
            ]
        }
    }
}`.
        - `{
    "MAT1400": {
        "TH": {
            "A": [
                [
                    "[Ma]",
                    "08:30-10:29"
                ],
                [
                    "[Je]",
                    "08:30-10:29"
                ]
            ],
            "B": [
                [
                    "[Ma]",
                    "08:30-10:29"
                ],
                [
                    "[Je]",
                    "08:30-10:29"
                ]
            ]
        },
        "TP": {
            "A102": [
                [
                    "[Lu]",
                    "15:30-17:29"
                ]
            ],
            "A103": [
                [
                    "[Lu]",
                    "15:30-17:29"
                ]
            ],
            "B101": [
                [
                    "[Lu]",
                    "15:30-17:29"
                ]
            ],
            "A101": [
                [
                    "[Lu]",
                    "15:30-17:29"
                ]
            ],
            "B102": [
                [
                    "[Lu]",
                    "15:30-17:29"
                ]
            ]
        }
    },
    "IFT1065": {
        "TH": {
            "A": [
                [
                    "[Lu]",
                    "10:30-12:29"
                ],
                [
                    "[Me]",
                    "13:30-14:29"
                ]
            ],
            "B": [
                [
                    "[Lu]",
                    "10:30-12:29"
                ],
                [
                    "[Me]",
                    "14:30-15:29"
                ]
            ]
        },
        "TP": {
            "B101": [
                [
                    "[Ve]",
                    "10:30-12:29"
                ]
            ],
            "A101": [
                [
                    "[Ve]",
                    "10:30-12:29"
                ]
            ]
        }
    },
    "IFT1015": {
        "TH": {
            "A": [
                [
                    "[Lu]",
                    "13:30-15:29"
                ],
                [
                    "[Me]",
                    "11:30-12:29"
                ]
            ],
            "B": [
                [
                    "[Ma]",
                    "15:30-17:29"
                ],
                [
                    "[Je]",
                    "13:30-14:29"
                ]
            ]
        },
        "TP": {
            "A102": [
                [
                    "[Je]",
                    "15:30-17:29"
                ]
            ],
            "B101": [
                [
                    "[Je]",
                    "14:30-15:29"
                ]
            ],
            "A101": [
                [
                    "[Je]",
                    "14:30-15:29"
                ]
            ],
            "B102": [
                [
                    "[Je]",
                    "15:30-17:29"
                ]
            ]
        }
    },
    "IFT1005": {
        "TH": {
            "A": [
                [
                    "[Ma]",
                    "13:30-15:29"
                ],
                [
                    "[Ve]",
                    "08:30-09:29"
                ]
            ],
            "B": [
                [
                    "[Je]",
                    "10:30-12:29"
                ],
                [
                    "[Ve]",
                    "14:30-15:29"
                ]
            ]
        },
        "TP": {
            "B101": [
                [
                    "[Ve]",
                    "15:30-17:29"
                ]
            ],
            "A101": [
                [
                    "[Ve]",
                    "15:30-17:29"
                ]
            ]
        }
    },
    "IFT1215": {
        "TH": {
            "A": [
                [
                    "[Lu]",
                    "08:30-10:29"
                ],
                [
                    "[Me]",
                    "08:30-09:29"
                ]
            ]
        },
        "TP": {
            "A101": [
                [
                    "[Me]",
                    "09:30-11:29"
                ]
            ]
        }
    }
}`
    
14. Comparer des combinaisons de cours : **`POST /cours/comparer/combinaison`**
    - **Format pour le body de la requête :**
        - `{
            "listeCours": [["idCours1", "idCours2",...],["idCours1","idCours2",...],...]
             "session" : "A24"
        }`
    - **Exemple de Body JSON attendu :**
      - `{
            "listeCours": [["IFT1015", "IFT1025"], ["IFT1227"]],
            "session": "A24" 
      }`ou encore
      - `{
            "listeCours": [["IFT1015", "IFT1025"], ["IFT1227"]],
            "session": ""
      }`
    - **Exemple de réponse JSON (status 200)**
      - `[
    [
      "Combinaison 1",
      "Cours=[IFT1015, IFT1025]",
      "Crédits=6",
      "Prérequis=[IFT1015, IFT1016]",
      "Concomitants=[]",
      "Périodes communes=[daytime]",
      "Sessions communes=[winter, autumn, summer]",
      "Horaires=listeHoraires,
      "Conflits=listeConflits"
    ],
    [
      "Combinaison 2",
      "Cours=[IFT1227]",
      "Crédits=3",
      "Prérequis=[IFT1065, IFT1215]",
      "Concomitants=[]",
      "Périodes communes=[daytime]",
      "Sessions communes=[winter, autumn]",
      "Horaires=[IFT1227 [A] [Ma] 10:30-12:29, IFT1227 [A] [Ma] 10:30-12:29, IFT1227 [A] [Je] 15:30-16:29, IFT1227 [A] [Je] 15:30-16:29, IFT1227 [A] [Ma] 10:30-12:29, IFT1227 [A] [Ma] 10:30-12:29, IFT1227 [A] [Ma] 09:30-12:29, IFT1227 [A101] [Je] 16:30-18:29, IFT1227 [A101] [Je] 16:30-18:29]",
      "Conflits=[]"
    ]
    ]`

    
    - **Exemple de réponse JSON (status 400)**:
      - `Requête invalide`
14. Voir les programmes contenant un certain "nom" dans leur nom : **`GET /cours-programme/nom/{nom}`**
   - Le parametre **`nom`** corresponds au mot-clé qu'on veut rechercher dans le nom de programme.
   - **Exemple de requête attendu :**
     - **http://localhost:7070/cours-programme/nom/informatique**
   - **Exemple de réponse JSON**: **(statut 200)** :
   - [
    "117510-Baccalauréat en informatique (B. Sc.)",
    "117520-Majeure en informatique",
    "117540-Mineure en informatique",
    "117550-Certificat en informatique appliquée",
    "117561-Microprogramme de premier cycle d'exploration en technologies informatiques",
    "117573-Microprogramme de premier cycle en administration des systèmes informatiques",
    "119110-Baccalauréat en mathématiques et informatique (B. Sc.)",
    "120510-Baccalauréat en physique et informatique (B. Sc.)",
    "146810-Baccalauréat en bio-informatique",
    "146811-Baccalauréat en bio-informatique (B. Sc.)",
    "217510-Maîtrise en informatique (M. Sc.)",
    "317510-Doctorat en informatique (Ph. D.)"
]
    - **Exemple de réponse JSON (status 400)**:
      - "Les paramètres fournis sont invalides ou le programme n'existe pas."
15. Soumettre un avis ( utilisé principalement par le bot mais c'est possible de le faire depuis postman) : **`POST /avis`**
  - **Format pour le body de la requête :**
        - `{
            "sigleCours" :"sigle",
    "professeur" : "nom",
    "noteDifficulte" :"note",
    "noteCharge": "note",
    "commentaire": "commentaire"
        }`
    - **Exemple de Body JSON attendu :**
      - `{
            "sigleCours" :"IFT2255",
    "professeur" : "Louis-Edouard",
    "noteDifficulte" :"4",
    "noteCharge": "5",
    "commentaire": "cours captivant et intéressant"
        }`
   - **Exemple de réponse JSON**: **(statut 200)** : Avis enregistré avec succès.
    - **Exemple de réponse JSON (status 400)**: Veuillez vérifier les champs.
16. Voir tous les avis: **`GET /cours/avis`**
   - **Exemple de requête attendu :**
     - **http://localhost:7070/cours/avis**
   - **Exemple de réponse JSON**: **(statut 200)** : [
    {
        "commentaire": "Cours très intéressant, professeur à l'écoute. Les démos sont bien organisés et les devoirs sont faciles à faire.",
        "noteDifficulte": 2,
        "sigleCours": "IFT3700",
        "valide": true,
        "noteChargeTravail": 3,
        "nomProfesseur": "Matthieu Taboga"
    },
    {
        "commentaire": "Cours très intéressant, démonstrations bien organisés et devoirs faciles à faire. Je reprendrais ce cours avec plaisir!",
        "noteDifficulte": 2,
        "sigleCours": "IFT3700",
        "valide": true,
        "noteChargeTravail": 4,
        "nomProfesseur": "Matthieu Taboga"
    },
    {
        "commentaire": "Certains cours étaient donnés en ligne à cause de la grève, et la plupart du temps il n'y avait pas d'enregistrements vidéos. C'était difficile à suivre tout au long, mais au moins les examens étaient faciles et basés sur les notes de cours.",
        "noteDifficulte": 3,
        "sigleCours": "IFT3700",
        "valide": true,
        "noteChargeTravail": 3,
        "nomProfesseur": "Eleanor Taboga"
    }, ... ]

17. Comparer par difficulté basée sur les avis  : **`POST /cours/comparer/avis/difficulte`**
  - **Format pour le body de la requête :**
         - `{
            "idsCours" : ["cours1","cours2",..]
        }`
    - **Exemple de Body JSON attendu :**
      - `{
           "idsCours" : ["IFT2255","IFT1227"]
        }`
   - **Exemple de réponse JSON**: **(statut 200)**
   - [
    [
        "IFT1227",
        "2.75"
    ],
    [
        "IFT2255",
        "1.56"
    ]
]

18. Comparer par charge de travail basée sur les avis  : **`POST /cours/comparer/avis/charge`**
  - **Format pour le body de la requête :**
        - `{
            "idsCours" : ["cours1","cours2",..]
        }`
    - **Exemple de Body JSON attendu :**
      - `{
           "idsCours" : ["IFT2255","IFT1227"]
        }`
   - **Exemple de réponse JSON**: **(statut 200)**
   - [
    [
        "IFT1227",
        "4"
    ],
    [
        "IFT2255",
        "2"
    ]
]
Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.
