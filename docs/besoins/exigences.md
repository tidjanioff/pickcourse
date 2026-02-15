---
title: Analyse des besoins - Exigences
---

# Exigences

## Exigences fonctionnelles

En nous basant sur la liste de souhait de notre client, ainsi que sur notre analyse du domaine du projet, les exigences fonctionnelles sont les suivantes:

- [ ] EF0 : L'utilisateur peut sélectionner un profil ( étudiant/visiteur)

En se basant sur l'hypothèse selon laquelle la plateforme est publique, le profil étudiant correspondra donc aux étudiants de l'UdeM et le profil visiteur correspond à toute personne


- [ ] EF1 : L’utilisateur peut créer un compte.
- [ ] EF2  : L'utilisateur peut se connecter;
- [ ] EF3 : L'utilisateur peut personnaliser son profil ;
- [ ] EF4 : L'utilisateur peut modifier son profil;
- [ ] EF7 : L'utilisateur peut rechercher un cours par sigle ou par nom ou mot-clé ( par exemple s'il recherche "Programmation" alors tous les cours qui contiennent "Programmation" dans leurs noms doivent s'afficher ( IFT1015, IFT1025,...));
- [ ] EF8 : L'utilisateur peut consulter la liste des avis d'un cours;
- [ ] EF9 : L'utilisateur peut filtrer sa recherche ( selon ses préférences, centres d'intérêt et données personnelles genre cycle, programme);
- [ ] EF10 : L'utilisateur peut voir la description détaillée des cours ( pré-requis, co-requis, exigences liées au programme ou au cycle);
- [ ] EF11 : Le système doit être doté d'un outil de comparaison qui permet à l'utilisateur d'évaluer la charge de travail totale d'une combinaison de cours. Pour cela, le système doit calculer la charge de travail de chaque cours ( par exemple en établissant une corrélation entre les moyennes sur plusieurs années et la charge de travail selon les avis non-officiels), et pourra donc faire une moyenne pour avoir la charge de travail totale de la combinaison ( Facile + Moyen + rès Difficile = Moyen par exemple);

- [ ] EF12 : Le système doit proposer des cours à l'utilisateur basé sur son historique de recherche et son profil; 
- [ ] EF13:  L'utilisateur peut poster des avis sur le système;
- [ ] EF14 : L'utilisateur peut ajouter des cours à ses favoris ;
- [ ] EF15 : L'utilisateur peut démarrer une discussion.
    Comme dans un forum, il devrait être possible sur la plateforme de démarrer une discussion au sujet d'un cours ( par exemple poser une question particulière et par chance, des personnes ayant déjà fait le cours passeront par là et répondront). Cette fonctionnalité peut également être intéressante dans le contexte d'un cours pour lequel on a pas pu récolter au moins 5 avis ( et dans ce cas, aucun avis ne s'affiche).

- [ ] EF16: Le système peut proposer certaines discussions aux utilisateurs basés sur leur historique et leur profil.

On part du principe que chaque discussion a pour nom le nom d'un cours suivi d'un certain identifiant de discussion, et ainsi, de la même façon que le système propose des cours à l'utilisateur, il pourra lui proposer des discussions.


## Exigences non fonctionnelles

Les exigences non-fonctionnelles liées à notre système sont les suivantes:

- [ ] ENF1 :  L'affichage des résultats de recherche doit se faire en moins de 1 seconde même avec un nombre important d'utilisateurs simultanés; ( performance)
-  [ ] ENF2 : Les avis associés à un cours ne peuvent être affichés que si leur nombre excède 4. 
- [ ] ENF3 : La plateforme doit être compatible avec les systèmes d'exploitation majeurs ( Windows, Mac, Linux) afin de satisfaire la portabilité;
- [ ] ENF4 : La plateforme doit utiliser l'API Planifium; ( c'est une contrainte d'implémentation)
- [ ] ENF5 :  La plateforme doit communiquer avec un bot Discord qui se charge de la collecte de données sur un serveur Discord;
- [ ] ENF6 : Les données sensibles ( noms des personnes qui ont posté les avis, noms associés aux anciens étudiants ou toute autre information personnelle ) inclus dans des avis doivent être supprimées de sorte à ce que rien ne puisse être associé à qui que ce soit, ceci afin de protéger les données personnelles.

- [ ] ENF7 : Toutes les données doivent être centralisées au sein de la plateforme, c'est-à-dire qu'il ne faut pas de lien vers un serveur Discord par exemple dans la page des avis;

- [ ] ENF8 :  L'interface doit être ergonomique et simple à utiliser, les principaux principes de UI/UX design doivent être respectés afin d'offir une bonne expérience aux utilisateurs de la plateforme.

- [ ] ENF9 :  L'adresse courriel utilisée lors de l'inscription doit être une adresse courriel de l'UdeM ( *.*(.*)+@umontreal.ca ) car on veut que seuls les étudiants de l'UdeM aient accès aux données de Planifium, ce qui à notre sens, est logique.;
- [ ] ENF10: Le système doit être disponible à 99.99% de temps ( le pourcentage restant pour les maintenances qui impliquent la fermeture du système pour une période et dans ce cas, la période doit être bien indiquée sur le site ( "Le site sera indisponible entre 17h et 17h30)) ceci afin de favoriser la fiabilité;


## Priorisation

Les exigences critiques représentent les fonctionnalités essentielles au fonctionnement de la plateforme.
Ce sont celles qu’on doit absolument inclure dans la première version pour que le système soit utilisable et cohérent.


| Exigence | Description |
|-----------|------------------------|
| **EF1** | L'utilisateur peut créer un compte. |
| **EF2** | L'utilisateur peut se connecter. |
| **EF7** | L'utilisateur peut rechercher un cours par sigle, nom ou mot-clé. |
| **EF8** | L'utilisateur peut consulter la liste des avis d'un cours. |
| **EF10** | L'utilisateur peut voir la description détaillée d’un cours. |
| **EF8** | L'utilisateur peut comparer des cours |


## Types d'utilisateurs


| Type d’utilisateur | Description | Exemples de fonctionnalités accessibles |
|--------------------|-------------|------------------------------------------|
| **Étudiant** | Utilisateur principal du système, cherchant à faire des choix de cours éclairés. | Recherche de cours, consultation des détails, comparaison de cours, visualisation des résultats académiques, filtres d’avis, personnalisation selon le profil. |
| **Bot Discord** | Agent automatisé chargé de transmettre les avis étudiants collectés sur Discord. | Soumission d’avis, envoi automatisé des données d’opinion vers l’API. |
| **Administrateur** | Responsable technique de la cohérence et de la mise à jour des données. | Importation des résultats académiques, gestion du stockage, configuration du système. |


