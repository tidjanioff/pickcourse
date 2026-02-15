---
title: Implémentation
---

<style>
    @media screen and (min-width: 76em) {
        .md-sidebar--primary {
            display: none !important;
        }
    }
</style>

# Développement de l’application

## Technologies utilisées

- **Langage principal :** Java  
- **Framework backend :** Javalin 
- **Framework frontend :** JavaFX
- **Gestion des dépendances :** Maven  
- **Tests :** JUnit 5, Mockito  
- **Documentation :** JavaDoc + MkDocs  
- **Outils de développement :** Git, GitHub, VSCode / IntelliJ  

## Organisation du code
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
 │   │   │   │
 │   │   │   └── org/projet/
 │   │   │       ├── controller/
 │   │   │       ├── service/
 │   │   │       ├── repository/
 │   │   │       ├── model/
 │   │   │       ├── exception/
 │   │   │       └── Main.java
 │   │   │
 │   │   └── resources/
 │   │       ├── historique_cours_prog_117510.csv
 │   │       └── PickCourse-logo.png
 │   │
 │   └── test/
 │       └── java/org/projet/
 │           ├── config/
 │           ├── controller/
 │           ├── service/
 │           ├── repository/
 │           └── model/
```

## Découpage en couches

### **Controllers**
  - Gèrent les routes HTTP
  - Reçoivent les requêtes
  - Appellent les services

### **Services**
  - Contiennent la logique métier
  - Implémentent les règles d’éligibilité
  - Gèrent les interactions avec les APIs externes

### **Repositories**
  - Abstraction de l’accès à la base de données
  - Requêtes SQL
  - Gestion de la persistance

### **Models**
  - Représentation des entités (Cours, Étudiant, Planification, etc.)

### **Utils**
  - Fonctions utilitaires
  - Validations
  - Transformations de données

## Gestion des dépendances

Les dépendances sont gérées via **Maven** (`pom.xml`), ce qui permet :

- Gestion centralisée des bibliothèques
- Build reproductible
- Intégration facile avec CI/CD (GitHub Actions)

---

## Difficultés rencontrées

### 1. Intégration avec l’API externe (Planifium)

- Gestion des erreurs réseau
- Données parfois incomplètes ou inconsistantes
- Adaptation du format de réponse à notre modèle interne

### 2. Gestion des règles d’éligibilité

- Vérification des prérequis
- Gestion des cycles d’admission
- Cas limites (cours équivalents, doublons)

La logique devenait rapidement complexe et nécessitait une bonne séparation entre validation technique et règles métier.

### 3. Tests unitaires

- Mock des dépendances
- Simulation d’appels API
- Isolation des services

---

## Solutions apportées

### 1. Abstraction de l’API externe

Création d’une couche dédiée permettant :

- Centralisation des appels API
- Transformation des données vers des DTO internes
- Gestion uniforme des erreurs

### 2. Refactorisation en couches strictes

Nous avons renforcé la séparation :

- Controller → uniquement HTTP
- Service → logique métier
- Repository → accès données

Cela a permis :

- Meilleure testabilité
- Code plus maintenable
- Moins de couplage

### 3. Mise en place de tests robustes

- Utilisation de **JUnit 5**
- Utilisation de **Mockito** pour simuler les dépendances
- Couverture des cas normaux et des cas limites

---

## Conclusion

L’implémentation de PickCourse repose sur une architecture backend claire et modulaire, facilitant :

- L’évolution du projet
- L’ajout de nouvelles fonctionnalités
- La maintenance à long terme

Le choix de Java + Javalin + Maven a permis de construire une API stable, testable et extensible, adaptée à un projet académique structuré.