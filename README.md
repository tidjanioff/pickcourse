# PickCourse

## Description du projet

PickCourse est une **plateforme d'aide au choix de cours** destinée aux étudiants de l'Université de Montréal.  
Elle est accessible via une **API REST** (backend Java) et une **interface graphique** (JavaFX).

<p align="center">
  <img src="docs/images/pickCourseLogo.png" alt="PickCourse Logo" height="150">
</p>




La plateforme combine :

- des **données officielles** (ex. informations et horaires issus de l'API Planifium, résultats académiques),
- des **données inofficielles** collectées auprès des étudiants via **Discord** (avis, difficulté perçue, charge de travail).

L'objectif est d'aider les étudiants à faire des **choix de cours éclairés**, en leur permettant notamment de :

- rechercher des cours (par sigle / nom / description),
- consulter les cours d'un programme,
- voir les cours offerts pour un trimestre donné,
- vérifier l'éligibilité (prérequis, etc.),
- consulter des statistiques académiques (score, moyenne, popularité),
- consulter et soumettre des avis étudiants,
- générer un horaire pour un ensemble de cours et détecter les conflits,
- afficher l'horaire d'un cours pour une session spécifique.

---
## Vidéo de démonstration

Une démonstration complète présentant l’API REST et l’interface graphique JavaFX :

[![Video Demo](https://img.shields.io/badge/Watch-Video%20Demo-red?style=for-the-badge&logo=youtube)](https://youtu.be/AL3vs1haIUE)

---

## Structure du projet

Le dépôt est organisé comme suit :

- **.github/workflows/**  
  Configuration CI (exécution automatisée des tests).

- **docs/**  
  Rapport du projet (site MkDocs + thème Material).

- **implementation/**  
  Implémentation principale du projet :
  - **API REST Java (Javalin)** + logique métier
  - **Interface graphique JavaFX**
  - **Bot Discord Python** (module complémentaire)

- **mkdocs.yml**  
  Configuration du site MkDocs.

- **requirements.txt / requirements**  
  Dépendances Python (bot / rapport selon votre configuration).

> Certains dossiers (ex. `.idea`, `.history`) proviennent de l'environnement de développement ou du template MkDocs.

---

## Organisation du code (implémentation)

L'implémentation suit une organisation par responsabilités (backend REST / GUI / bot).
```text
implementation/
├── discord-bot-python/
│   ├── main.py
│   └── requirements.txt
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── client/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   └── MainApp.java
│   │   │   └── org/projet/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── model/
│   │   │       ├── exception/
│   │   │       └── Main.java
│   │   └── resources/
│   │       ├── historique_cours_prog_117510.csv
│   │       └── PickCourse-logo.png
│   └── test/
│       └── java/org/projet/
│           ├── config/
│           ├── controller/
│           ├── service/
│           ├── repository/
│           └── model/
```

### Backend (API REST) — `org.projet`

- **controller/** : routes HTTP, validation d'entrée, réponses JSON.
- **service/** : logique métier (éligibilité, comparaison, calculs, agrégation).
- **repository/** : accès aux données / persistance.
- **model/** : entités (cours, avis, programme, etc.).
- **exception/** : exceptions personnalisées et gestion d'erreurs.
- **Main.java** : point d'entrée du serveur Javalin.

### Interface graphique (JavaFX) — `client`

- **client/controller/** : contrôleurs UI.
- **client/service/** : appels à l'API REST et logique côté interface.
- **MainApp.java** : point d'entrée de l'application JavaFX.

### Bot Discord (Python)

- **discord-bot-python/main.py** : logique du bot (commande `/avis`, etc.).
- Les avis collectés sont récupérés côté backend et stockés (ex. `Avis.json` selon l'implémentation).

---

## Tests de fonctionnalités

Les fonctionnalités testées incluent :

- voir les cours offerts dans un programme ;
- voir les cours offerts pour un trimestre donné ;
- vérifier l'éligibilité à un cours ;
- voir les résultats académiques d'un cours ;
- comparer des cours ;
- créer un ensemble de cours ;
- générer un horaire et détecter des conflits ;
- voir l'horaire d'un cours pour un trimestre donné.

---

## Exécution du rapport MkDocs

Le rapport est généré avec **MkDocs** à partir des fichiers Markdown présents dans `docs/`.

### 1) Se placer à la racine du projet
```bash
cd pickcourse
```

### 2) Installer MkDocs (et le thème Material)
```bash
python3 -m pip install mkdocs mkdocs-material
```

Ou via un fichier de dépendances (si disponible) :
```bash
python3 -m pip install -r requirements.txt
```

### 3) Lancer le serveur local
```bash
mkdocs serve
```

Puis ouvrir dans le navigateur :
```
http://127.0.0.1:8000/
```

### Problème courant : Unrecognised theme name: 'material'

Si vous voyez :
```
ERROR - Config value 'theme': Unrecognised theme name: 'material'
```

Installez le thème Material :
```bash
python3 -m pip install mkdocs-material
```

Puis relancez :
```bash
mkdocs serve
```

---

## Lancer l'API REST (backend Java)

À exécuter depuis le dossier `implementation/` (celui qui contient `pom.xml`).
```bash
cd implementation
mvn clean test
```

Ensuite, démarrer le serveur en lançant la classe `Main.java` (IDE recommandé) :
```
implementation/src/main/java/org/projet/Main.java
```

Le serveur écoute sur le port **7070**.

---

## Lancer l'interface graphique (JavaFX)

Assurez-vous que l'API REST tourne déjà (port 7070), puis :
```bash
cd implementation
mvn clean javafx:run
```

**NB :** certaines actions ouvrent une fenêtre secondaire (ex. génération d'horaire).  
Tant que cette fenêtre n'est pas fermée, la fenêtre principale peut rester figée.

---

## Lancer le bot Discord

1. Démarrer l'API REST (port 7070).

2. Aller dans `implementation/discord-bot-python/`.

3. Créer un fichier `.env` contenant :
```
TOKEN=VOTRE_TOKEN_DISCORD
```

4. Installer les dépendances et lancer le bot :
```bash
cd implementation/discord-bot-python
python3 -m pip install -r requirements.txt
python3 main.py
```

Le bot (serveur **AvisPickCourse**) permet notamment de soumettre des avis via la commande `/avis`.

---

## Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.