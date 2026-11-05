# Visioconférences depuis Discussion

Le bouton caméra du composeur ouvre une fenêtre de création avec un titre et le nombre
de membres invités. Il apparaît uniquement si la messagerie instantanée et sa fonction visio
sont activées et autorisées pour l’utilisateur. Le brouillon de message est conservé.

La création publie un lien nommé dans le canal ou le fil courant. Les autres membres actifs
ayant accès à l’application reçoivent une notification Silverpeas par leur canal habituel.
Les comptes invités, anonymes, supprimés ou désactivés sont exclus. Les invitations sont
émises après validation de la transaction et un dernier contrôle des accès ; aucune invitation
n’est envoyée dans Converse.js. Les abonnements habituels aux messages restent indépendants.

Le lien « Rejoindre la visioconférence » ouvre un nouvel onglet pour utiliser caméra et microphone.
Une salle aléatoire est créée à la connexion au service vidéo. Les liens des salles peuvent être
partagés : les règles d’accès et de modération de la conférence relèvent du service Jitsi configuré.
Supprimer le message ne ferme pas une conférence déjà ouverte.

## Configuration existante

Fichier `org/silverpeas/chat/settings/chat.properties` :

- `chat.enable` et les paramètres XMPP habituels doivent activer la messagerie ;
- `chat.client.spid` sélectionne le client ;
- `chat.client.<client>.visio.enabled` active la visio ;
- `chat.client.<client>.visio.url` désigne le point d’entrée vidéo natif (`/visio`) ou externe.

Pour Converse.js, le préfixe est `chat.client.conversejs`. L’URL configurée doit être une URL
HTTP(S) sans paramètres ni fragment. Le point d’entrée natif conserve les réglages Jitsi existants,
y compris la configuration du domaine et du jeton serveur. Discussion ne copie aucun jeton.
Aucune migration SQL ni modification de Core n’est nécessaire.
