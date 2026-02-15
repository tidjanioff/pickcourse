---
title: Analyse des besoins - Risques
---

# Analyse des risques

## Identification des risques


Ce projet présente de nombreux risques, que ce soit sur le plan technique, humain ou organisationnel. Nous avons:

### Risque 1 – Absence prolongée d’un membre clé. 
    
    L’absence prolongée d’un membre clé de l’équipe de développement représente un risque majeur pour la réussite du 
    projet. Ce risque s'accentue si la personne concernée abandonne le cours ou cesse de participer activement aux 
    travaux de l’équipe. Une telle situation pourrait entraîner des retards dans le développement de la plateforme Web, 
    la suppression de certaines fonctionnalités faute de temps, ainsi qu’une augmentation de la charge de travail pour 
    les membres restants.

- **Probabilité** : Moyenne  
- **Impact** : Élevé  
- **Plan de mitigation** :  
  - Répartition claire des responsabilités, afin de permettre une adaptation rapide et efficace en cas d’absence 
    prolongée d’un membre clé. Cela réduit considérablement l’impact potentiel de son indisponibilité.
  - Documentation régulière du travail réalisé, afin d’assurer la traçabilité des tâches et de 
    faciliter la reprise du travail par un autre membre de l’équipe si nécessaire.
  - Favorisation du pair programming, pour favoriser le partage des connaissances et 
    garantir la continuité du développement en cas d’absence imprévue.

### Risque 2 – Augmentation du stress chez les utilisateurs. 

    Imaginons un étudiant, en fin de parcours avec une GPA de 1,9 qui n'a plus le droit à l'erreur, 
    sous peine d'exclusion définitive. Lorsque ce dernier accède à l'outil, il devient obnubilé par ce dernier. 
    Ceci est un cas précis, mais l'outil de manière générale pourrait très vite devenir une source de stress chez les 
    utilisateurs qui passeraient alors des heures et des heures à essayer de perfectionner leurs décisions.

    Un autre cas est un étudiant qui doit prendre un cours obligatoire, mais la charge de travail de ce dernier est 
    estimé à très difficile. Si ce cours n'est disponible qu'à la session d'Automne, il devra attendre une prochaine 
    session où le cours est considéré plus facile pour le prendre. Ce qui non seulement génère du stress, 
    mais aussi détourne son cheminement.

- **Probabilité** : Faible 
- **Impact** : Élevé  
- **Plan de mitigation** :  
  - Mentionner sur la page d'accueil par exemple, que les avis non officiels sont avant tout subjectifs,
    et qu'un cours qui a été mal vécu par 10 étudiants, ne le sera pas forcément pour soi.


### Risque 3 – Le bot Discord est indisponible en raison d’un trop grand nombre de requête.

    S’il y a trop de requêtes simultanément et que le bot n’est pas en mesure de toutes les traiter, il peut commencer à 
    présenter des dysfonctionnements. Ces problèmes peuvent également provenir de l’API de Discord.

- **Probabilité** : Faible  
- **Impact** : Élevé  
- **Plan de mitigation** : 
   - Imposer une limite sur le temps d'écart entre deux requêtes si le nombre d'utilisateurs sur le site est de l'ordre 
     de 10000 par exemple (ce chiffre pourrait changer dépendamment de comment le bot est codé).


### Risque 4 - Association d’un cours à un avis sans lien lors de la récupération des données, entraînant un mauvais appariement entre cours et avis.

    Ce risque est étroitement lié à la manière dont le bot sera codé,mais par exemple si dans un avis une personne 
    cite un cours en exemple (pour référence), et si dans le code on récupère des avis par mot-clé, 
    on peut vite se retrouver avec des avis pas vraiment liés au cours en question.

    Dans la même veine, notre bot pourrait passer à côté de nombreux avis car certaines personnes ne mentionnent 
    pas le sigle du cours dans leurs réponses (exemple: Une personne demande comment les autres ont trouvé un certain 
    cours, et une autre répond bien à la question, sans jamais mentionner ledit cours dans son texte).

- **Probabilité** : Moyenne (pour l'instant)
- **Impact** : Élevé (la charge de travail est calculée en utilisant un avis qui n'a rien à voir)
- **Plan de mitigation** :  
  - Bien coder le bot de sorte à gérer ce genre de situations.
  - Aussi, "formatter" les données du côté de Discord pour faciliter le travail du bot. En effet, si par exemple on crée
    dans les groupes Discord où on veut récupérer les avis (AEDIROUM par exemple) un channel spécifique où donner des 
    avis, régi par des règles de format (par exemple "votre message lorsque vous donnez un avis doit commencer par le 
    sigle du cours concerné"), cela facilitera grandement le travail d'analyse effectué par notre bot, 
    et diminuera le risque 4.


### Risque 5 – Fuite d'informations confidentielles

    Vu qu'on autorise les étudiants à poster des avis directement sur le site, ces derniers pourraient inclure des 
    informations confidentielles qui ne seront pas forcément nettoyées par le bot.

- **Probabilité** : Moyenne
- **Impact** : Élevé pour les personnes concernées, Faible pour le système.
- **Plan de mitigation** : 
   - Nettoyer les données des avis postés localement de la même façon que le bot nettoie les données de Discord.
     En d'autres termes, introduire de la modération dans le système.

### Risque 6 – Pénurie de données pour certains cours

    Par exemple, c'est possible qu'il n'y ait aucun avis pour certains cours. On considère que c'est un risque dans le 
    sens où ça va aboutir à de l'insatisfaction du côté de l'utilisateur.

- **Probabilité** : Assez élevé, surtout qu'il faut au moins 5 avis pour que la page avis apparaisse  
- **Impact** : Élevé  
- **Plan de mitigation** : 
   - Pour chaque cours, se baser sur le profil de l'utilisateur ainsi que sur les statistiques du cours et estimer si 
     cela peut lui convenir.

### Risque 7 – L'API pourrait ne plus être disponible

    Une fois, le professeur avait évoqué que ceci était possible donc si c'est possible, c'est un risque très élevé pour
    notre système vu qu'il y a une dépendance forte avec Planifium.

- **Probabilité** : Faible (mais on ne sait jamais)  
- **Impact** : Extremement élevé  
- **Plan de mitigation** : 
   - Réduire la dépendance entre notre système et les systèmes externes (c'est-à-dire opter pour une conception évolutive). 
     L'étude de cette possibilité sera faite lors de la phase de conception ; on étudiera les options qui s'offrent à nous ;
   - Signer un accord avec les développeurs de l'API nous donnant des droits sur l'API (dans la mesure du possible).
  
## Modification du processus opérationnel

J'imagine que le taux de consultation des conseillers et TGDEs sera en baisse. Les questions dans des groupes Discord pour avoir des avis sur un cours également.

Un processus interne que ça pourrait modifier, est le fait qu'avec l'accord signé (risque 7), on aurait un peu un droit de propriété sur Planifium.
