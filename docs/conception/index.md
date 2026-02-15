---
title: Conception 
---

# Conception

La conception de ce projet s'est articulée autour du patron architectural MVC, associé à l'architecture REST.

## Architecture MVC

L'architecture MVC permet de séparer les responsabilités en trois couches essentielles : la vue, le controlleur et le modèle. Dans le cadre de notre projet, seuls le controller et le modèle ont été considérés car pour cette première itération, il n'y aura pas d'interface graphique ou d'interface Web : on simulera les interactions avec l'utilisateur via le terminal. Cependant, pour les itérations à venir, cela changera potentiellement.

L'architecture MVC ici a été utiliséée car elle suit les principes de la conception modulaire, et offre également une meilleure visibilité des rôles de chaque module.

## Architecture REST

Nous allons développer une API REST qui servira d'intermédiaire entre l'utilisateur et PickCourse, qui communiqueront donc en se servant de requêtes HTTP pour obtenir, modifier, supprimer et créer des ressources.

## Choix de conception

Nous avons introduit les services et les repositories, dont le rôle est d'alléger la tâche du controller; les repositories gèrent la communication avec la base de données, et les services s'occupent de la logique métier. Le controller quant à lui réagit aux actions de l'utilisateur et communiquent les requêtes aux services qui s'occupent d'initier les opérations sur la base de données ainsi que la gestion des entités.

Pour profiter des avantages du polymorphisme, la classe Controller a été définie comme étant abstraite ( remarquer le style _italique_), nous permettant ainsi de pouvoir la sous-classer en fonction des controllers qu'on veut avoir. Dans chacun de ces sous-controllers, on aura donc des versions différentes des méthodes CRUD, ainsi que de la méthode permettant d'analyser la requête et la formatter pour qu'elle soit recevable par le service.

Chaque controller est associé à un service unique qui lui gère une seule entité, ce qui permet donc de réduire le couplage. Les différents services effectuent des opérations qui ont un lien logique évident ( par exemple le service Cours s'occupe de la comparaison de cours ainsi que de la validation de ces derniers, mais ne vérifie pas la validité de l'utilisateur par exemple), renforçant la cohésion de notre système.

Par ailleurs, le service Cours n'autorise pas la modification des ressources dans l'API cours qui sera utilisée ( Planifium en l'occurence), ce qui permet donc de sécuriser l'échange de données entre ce service externe et le nôtre; les utilisateurs ne pourraient pas s'amuser à créer des ressources factices et à les push sur Planifium, cela permet donc de renforcer l'interopérabilité entre les deux systèmes.

Le dernier point, qui n'est pas des moindres, concerne l'encapsulation : les attributs sont privés, et les méthodes ( notamment les getters et setters qui n'ont pas été rendues visibles mais seront bien là lors de l'implémentation) sont publiques, ce qui permet d'avoir du contrôle sur les communications avec les classes externes.