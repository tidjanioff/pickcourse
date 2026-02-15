---
title: Conception - Modèle de données
---

# Modèle de données

## Entités principales

### Cours
Représente un cours universitaire (ex. IFT2255).
- Attributs (diagramme) : `id`, `description`, `sigle`, `horaireSession`, `indicateurSession`, `nombreCredits`, etc.
- Rôle : support des avis et des résultats; chaque avis et chaque résultat est toujours lié à un cours précis.

### Avis
Représente l’avis déposé par un étudiant sur un cours.
- Attributs (diagramme) : `id`, `contenu`, `noteAttendue`, `noteObtenue`, `difficulte`, `qualite`, `chargeTravail`, etc.
- Rôle : stocke l’évaluation qualitative et quantitative d’un étudiant sur un cours (permet ensuite des statistiques, filtres, recommandations…).

### Resultats
Représente les résultats académiques agrégés d’un étudiant.
- Attributs (diagramme) : `nombreEchecs`, `nombreCredits`, `moyenne`, `trimestre`, etc.
- Rôle : donne une vision globale de la progression de l’étudiant et peut servir à adapter les conseils (ex. charge de travail à venir).

## Relations entre entités

- **User – Avis**
  - Multiplicité (interprétée à partir du diagramme et du contexte) : `User 1 ── 0..* Avis`
  - Interprétation : un utilisateur peut déposer plusieurs avis, mais chaque avis est associé à un seul utilisateur auteur.

- **Cours – Avis**
  - Multiplicité : `Cours 1 ── 0..* Avis`
  - Interprétation : un cours peut recevoir plusieurs avis, mais chaque avis concerne exactement un cours.

- **Cours – Resultats**
  - Multiplicité (contexte) : `Cours 1 ── 0..* Resultats`
  - Interprétation : un cours peut apparaître dans plusieurs bulletins de résultats (plusieurs étudiants), mais chaque ligne de résultat est liée à un cours précis.


## Contraintes métier 

- **Un Avis doit toujours être lié à un Cours**
  - Pas d’avis anonyme et pas d’avis “orphelin”.
  - Contrainte :  `avis.cours != null`.
- **Les valeurs numériques d’un Avis sont bornées**
  - `difficulte`, `chargeTravail` ∈ [1, 5].
  - Ces contraintes garantissent la cohérence et facilitent les calculs.

- **Les Resultats doivent rester cohérents avec les Cours suivis**
  - `nombreCredits` doit correspondre à la somme des crédits des cours associés.
  - Un trimestre ne doit pas contenir de cours dupliqués pour un même étudiant.

## Évolution potentielle du modèle

Actuellement, l’horaire d’un cours est intégré directement dans l’entité Cours.
Une évolution serait d’introduire des entités dédiées aux sessions, sections et activités. Cela permettrait d’éviter la duplication d’informations entre cours répétés chaque session, de représenter explicitement la structure réelle de l’université, et de faciliter la gestion des informations sur les sessions de manière indépendante du reste (popularité, taux de réussite, etc.).

