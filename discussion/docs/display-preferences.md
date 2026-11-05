# Préférences locales d’affichage

Discussion mémorise l’état ouvert ou réduit de la liste des canaux, ainsi que l’état des
accordéons Participants, Publications, Messages épinglés, Fichiers partagés et Notifications.
Le fonctionnement exclusif des accordéons est conservé, y compris lorsque tout est fermé.

Les choix sont enregistrés dans `localStorage`, séparément pour chaque utilisateur et chaque
instance de Discussion. Ils sont partagés entre les canaux de cette instance et restaurés à la
navigation suivante. À la première visite, les valeurs par défaut de l’application s’appliquent.

Seules les actions de l’utilisateur sont mémorisées. Les changements temporaires provoqués par
la recherche ou l’ouverture d’un fil ne remplacent pas ces préférences. Les vues de fil intégrées
ne lisent ni ne modifient l’état de la vue principale.

Ces préférences sont propres au navigateur et à son origine. Elles ne sont pas envoyées au serveur
et aucun cookie n’est créé. Si le stockage est bloqué, plein ou invalide, les commandes restent
utilisables et l’application conserve son comportement par défaut.

Le dernier canal consulté est également mémorisé. L’entrée générale de l’application restaure
ce canal s’il figure toujours parmi les canaux actifs accessibles. Sinon, elle reste sur Général.
Les liens explicites vers un canal, un message ou un fil sont prioritaires ; ils ne sont jamais
redirigés vers le canal mémorisé. Les fils intégrés ne remplacent pas la sélection principale.
