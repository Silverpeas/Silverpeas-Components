# Discussion — premier socle natif, Feature #15552

## État

Implémentation initiale dans `Silverpeas-Components`, branche `feature-15552`.
La branche locale et `origin/feature-15552` pointaient sur `f472ca9bc1` avant les changements,
sans modification locale. Seuls `pom.xml` et le nouveau répertoire `discussion/` sont concernés.
Aucun commit, push ou PR n'a été créé.

Ce socle compile et ses tests passent. Il reste à valider dans une instance Silverpeas/WildFly
avant de le considérer comme une V1 prête à livrer.

## Architecture réelle de suggestionBox examinée

| Sujet | Constat dans suggestionBox | Choix pour Discussion |
| --- | --- | --- |
| Maven | Agrégateur `suggestionBox/pom.xml`, configuration JAR, bibliothèque JAR, WAR | Même découpage en trois modules, ajout au réacteur racine |
| Contrôleur de session | `control/SuggestionBoxWebController` hérite de `WebComponentController`, lui-même contrôleur de session Silverpeas | `DiscussionWebController` et `DiscussionWebRequestContext` |
| Routage | `WEB-INF/web.xml` déclare le `WebComponentRequestRouter` générique et le contrôleur ; routes annotées `@GET`, `@POST`, `@Path`, `@Homepage`, `@RedirectToInternalJsp` | Même mécanisme, entrée `/Rdiscussion/*` ; pas de servlet RequestRouter spécifique nécessaire |
| Vues | JSP sous `suggestionBox/jsp`, tags `view`, JSTL Jakarta, ressources de langue fournies par le contrôleur | JSP `discussion.jsp` et fragment `messages.jsp`, tags natifs et ressources séparées |
| Frontend | Pages JSP et ressources JavaScript, dont un frontend AngularJS et des ressources REST | JavaScript limité au chargement de fragments JSP avec `sp.ajaxRequest`, sans nouveau framework |
| Métier | `SuggestionBoxService`, `DefaultSuggestionBoxService`, modèle actif `SuggestionBox`, `SuggestionCollection`, `Suggestion` | Interface `ConversationService` et implémentation transactionnelle dédiée ; modèle sans dépendance aux commentaires |
| Persistance | Entités `SilverpeasJpaEntity`, `UuidIdentifier`, `SuggestionBoxRepository`, façade `SuggestionRepository` et `SuggestionJPARepository` ; aucun DAO JDBC à reproduire ici | Trois entités et trois repositories JPA avec le même socle Silverpeas |
| Services transversaux | SuggestionBox utilise notamment pièces jointes/WYSIWYG, commentaires, notes, notifications, abonnements et indexation | Aucun couplage à `CommentService` ou Converse.js ; ces intégrations ne sont pas nécessaires au premier socle |
| Cycle de vie | `SuggestionBoxInstancePostConstruction` et `SuggestionBoxInstancePreDestruction`, beans techniques nommés | Hooks analogues : création de la conversation principale, nettoyage des messages et repères de lecture |
| Descripteur | `xmlcomponents/suggestionBox.xml`, profils admin/publisher/writer/user et correspondances avec les rôles d'espace | `discussion.xml`, profils admin/writer/user, libellés et aides FR/EN/DE |
| Permissions | `@LowestRoleAccess`, contrôles auteur/gestionnaire/modérateur dans `SuggestionBoxWebManager`, contrôles d'accès REST hérités | Vérification métier de l'accès à l'instance via `ComponentAccessController`, du rôle et de l'auteur ; contrôle complémentaire des routes |
| i18n et configuration | Bundles FR/EN/DE, settings, icons, namespace de journalisation, scripts de migration pour quatre SGBD | Même organisation des ressources ; icônes natives réutilisées depuis Forums |
| Tests | Aucun fichier de test unitaire ou d'intégration trouvé sous `suggestionBox/` dans cette branche | Nouveau socle de tests JUnit 5 suivant `JEETestContext`, `@TestedBean`, `@TestManagedMock` et le conteneur de test Silverpeas ; exemples pris dans quickinfo |

Les anciennes applications du dépôt ont encore des classes `SessionController` et `RequestRouter`
spécifiques. SuggestionBox utilise la génération MVC récente. Le choix ci-dessus suit son code réel
et la recommandation d'`AGENTS.md` pour une nouvelle application.

Les règles qualité SonarCloud actives ont été consultées avant l'écriture du code, ainsi que les
problèmes ouverts du POM racine : aucun problème ouvert n'y était signalé. Aucune analyse Sonar
locale ou Jenkins de cette nouvelle implémentation n'a été exécutée.

## Fonctionnalités implémentées

- Une conversation `main` créée par instance. La contrainte d'unicité porte sur `(instanceId, scopeKey)`.
- Messages de texte brut, limités à 2 000 caractères : publication, consultation, édition, suppression.
- Accès à la discussion réservé aux rédacteurs et gestionnaires Silverpeas.
- Édition et suppression réservées à l'auteur ou au gestionnaire de l'instance, avec refus des invités et anonymes pour les écritures.
- Vérification de l'appartenance du message à la conversation de l'instance avant toute modification.
- Contrôle de version lors d'une édition ou suppression ; les conflits sont traduits en HTTP 409.
- Historique chronologique (récents en bas), pages de 30 messages par défaut (`history.pageSize`), curseur exclusif stable, limite métier de 100.
- Défilement infini avec `IntersectionObserver`, bouton de chargement et reprise après erreur.
- Compteur des messages non lus, excluant les messages de l'utilisateur et les messages supprimés.
- Action explicite « Marquer comme lu », utilisant la séquence de la page affichée. Une simple consultation ne marque pas automatiquement l'historique comme lu.
- Repères de lecture monotones par utilisateur ; les invités et anonymes ne créent pas de repère persistant.
- JSP avec échappement du texte, aucune logique JavaScript ou CSS inline ; utilisation de l'aide AJAX et des jetons de sécurité de la plateforme.
- Réponses de discussion avec `Cache-Control: no-store`.

## Persistance et extension

`sc_discussion_conversation` porte l'identité de conversation et sa dernière séquence.
`sc_discussion_message` porte l'identité du message, sa conversation, sa séquence, son texte et son état de suppression.
`sc_discussion_read` porte le dernier repère de lecture d'un utilisateur pour une conversation.
Toutes les entités conservent les champs d'audit et la version optimiste du socle Silverpeas.

Les opérations d'écriture prennent un verrou pessimiste sur la conversation dans une transaction.
Cela sérialise l'attribution des séquences et les créations de repères de lecture. Le contrôle de version
protège en complément les modifications issues de formulaires périmés.

La suppression remplace le texte par un contenu vide visuellement et conserve une trace technique.
Le curseur de pagination reste ainsi utilisable. Les traces supprimées sont exclues du compteur des non-lus.
Les index et contraintes couvrent les couples conversation/séquence et conversation/utilisateur.

Les identifiants de conversation sont distincts des identifiants d'instance et des identifiants de message.
Cela laisse une base structurelle pour plusieurs conversations, fils ou réactions. L'API actuelle reste
centrée sur la conversation principale d'une instance ; les futurs usages demanderont leur propre API
et leurs règles d'accès. Les notifications, mentions, temps réel et conversations privées restent à concevoir.

## Vérifications effectuées

Depuis la racine du dépôt :

```sh
mvn -pl discussion/discussion-configuration,discussion/discussion-library,discussion/discussion-war -am clean verify -B -ntp
node --test discussion/discussion-war/src/test/javascript/discussion.test.cjs
```

Résultats :

- Build Maven réussi, avec compression JS/CSS et génération des JAR et du WAR.
- 28 tests Java réussis : 5 modèle, 11 service, 2 permissions, 2 cycle de vie, 3 persistance H2/Hibernate, 5 contrôleur web.
- 3 tests JavaScript réussis : chargement par curseur sans doublon de requête, fin d'historique, erreur et reprise manuelle.
- Le test de persistance exécute le script H2 livré, valide les mappings Hibernate et exécute les requêtes réelles. Il vérifie notamment pagination avec ajout entre deux pages, isolation, non-lus et nettoyage.
- Descripteurs d'application et de migration validés contre les XSD officiels Silverpeas.
- `git diff --check` réussi. Les sources de suggestionBox n'ont pas été modifiées.

Les tests ont été lancés sur l'hôte avec Java 21 et Maven 3.9.16. Docker n'était pas accessible.
Le lanceur Node fourni par l'environnement a été utilisé pour les tests JavaScript ; ceux-ci se lancent
séparément de Maven.

## Validation restante avant livraison

1. Déployer dans une instance de développement Silverpeas/WildFly et vérifier la découverte CDI,
   les hooks d'instance, la compilation des JSP, les jetons de sécurité et le rendu dans le look utilisé.
2. Exécuter les migrations sur PostgreSQL, SQL Server et Oracle : les scripts sont fournis pour
   les quatre SGBD, mais seul H2 a été exécuté ici.
3. Vérifier les scénarios simultanés avec plusieurs transactions/utilisateurs sous le serveur réel,
   notamment publication, lecture et conflit d'édition. Les tests actuels ne constituent pas un test de charge.
4. Ajouter les artefacts Discussion à la distribution/assemblage Silverpeas avant une livraison standard.
   Le dépôt d'assemblage et Silverpeas Core n'ont pas été modifiés.

Aucun test Arquillian/WildFly ni test complet du navigateur dans Silverpeas n'a été exécuté.
La génération du WAR ne précompile pas les JSP et ne remplace donc pas cette validation.


## Liens, mentions et réactions en développement

Le nom et la description affichés proviennent des libellés localisés de l'instance.
Les messages restent stockés en texte : les URL HTTP(S) sont rendues comme liens,
les mentions sont enregistrées sous la forme `@[Nom](identifiant)` et les emoji en Unicode.
Le sélecteur de mentions est réservé aux rédacteurs et ne propose que des comptes autorisés
sur l'instance. Les notifications des nouvelles mentions, hors auteur et doublons, sont
émises par le mécanisme Silverpeas après validation de la transaction.

Un utilisateur authentifié disposant de la lecture peut ajouter ou retirer son like.
Une contrainte `(messageId, userId)` et le verrou de conversation évitent les doublons.
La suppression d'un message retire ses likes ; la suppression de l'instance les nettoie aussi.

Le schéma reste volontairement en **version 001** pendant le développement.
`001/create_table.sql` décrit l'installation complète actuelle. Pour une base de développement
ayant déjà installé 001, appliquer explicitement une fois `001/add_likes.sql` du SGBD concerné
avant de déployer cette évolution. Le script n'efface aucune donnée et ne modifie pas la version
du module. Les variantes PostgreSQL et H2 acceptent aussi une nouvelle exécution si la table existe.
Il ne faut pas relancer l'intégralité de `create_table.sql` sur la base existante.

Les tests de navigateur dans `discussion-war/src/test/javascript/discussion-editor-browser.test.cjs`
nécessitent Playwright et son navigateur Chromium ; `CHROME_PATH` permet de choisir un navigateur
Chromium déjà installé. Les autres tests JavaScript utilisent uniquement Node.js.


## Fil chronologique et suppression du rôle lecteur

Le descripteur ne définit plus que les profils gestionnaire (`admin`) et rédacteur (`writer`).
Toutes les routes et les opérations métier exigent au moins le rôle rédacteur. Les anciens
comptes disposant uniquement du profil lecteur doivent être réaffectés explicitement dans
les droits de l'instance ; les droits existants ne sont pas automatiquement élargis.

Le fil affiche les messages récents en bas et la saisie reste sous la zone de défilement.
Les pages plus anciennes sont insérées au-dessus en conservant la position visible.
L'ouverture vise le premier repère non lu visible, ou les derniers messages s'il n'y en a pas ;
un lien de notification vers un message précis reste prioritaire. Le calcul des non-lus
réutilise le repère de lecture existant et exclut les messages de l'utilisateur et les messages
supprimés. La consultation ne modifie pas automatiquement ce repère : l'action explicite
« Marquer comme lu » reste disponible. Aucun changement SQL n'est nécessaire pour cette évolution.

### Actualisation du fil et notifications

Le fil écoute l'événement `DISCUSSION_CHANGED` du canal natif partagé `spServerEventSource`.
Les publications, éditions, suppressions, réactions et lectures explicites produisent un événement
après validation de leur transaction. Le signal contient l'identifiant de l'instance et un indicateur de changement du contenu ;
les droits des destinataires sont revérifiés à chaque envoi et à chaque rejeu. Les confirmations
de lecture ne sont transmises qu'aux sessions de l'utilisateur concerné.

Il n'y a plus de vérification périodique du fil. À réception d'un événement, au retour au premier
plan et à la reconnexion (`OPEN_EVENT_SOURCE`), la page récupère les données à jour. Les rafales
sont regroupées, les changements reçus pendant une requête sont repris ensuite et le rattrapage
est drainé par lots. Les requêtes sont bornées à 100 messages nouveaux et 100 messages déjà chargés,
avec contrôle des droits de l'instance. Les éditions, suppressions, réactions et compteurs non lus
sont réconciliés sans rechargement de page. Un éditeur ouvert, une réaction en cours et la sélection
de texte sont préservés. La lecture remonte sans déplacement forcé ; au bas du fil, les nouveaux
messages restent visibles. Une interruption réseau est indiquée et la synchronisation reprend
automatiquement après un échec ou à la reconnexion. Les éditeurs différés sont réconciliés à leur
fermeture. La présence des auteurs conserve son propre rafraîchissement toutes les 30 secondes et
est aussi actualisée lorsqu'un changement du fil est signalé.

Le transport est celui de Silverpeas, sans nouvelle connexion par iframe : SSE par défaut, ou
WebSocket si `notification.sse.usingWebSocket = true` dans
`org/silverpeas/notificationManager/settings/notificationManagerSettings.properties`.
`notification.sse.enabled` doit rester activé. Aucun réglage global n'est modifié par Discussion.

Seules les nouvelles @mentions déclenchent des notifications Silverpeas, après validation de la
transaction du message. Leur envoi utilise une nouvelle transaction ; le canal et le différé
restent ceux choisis par le destinataire dans Silverpeas (bureau, e-mail, etc.). Le navigateur
doit autoriser les notifications pour afficher celles du bureau. Il n'y a aucun changement SQL
et le schéma reste en version 001.

### Défilement dans la navigation Silverpeas

Le défilement principal est celui du document de l'iframe. L'historique n'a plus de barre de
défilement interne. Le titre reste en haut grâce à un en-tête sticky ; la saisie est fixée en bas
et les auteurs restent à droite (en bandeau sur écran étroit). Les dimensions suivent le cadre
natif et le viewport, sans modifier le document parent. Les listes longues d'auteurs restent
consultables dans leur colonne. Le chargement des anciens messages et les mises à jour en direct
utilisent les mêmes coordonnées de défilement du document afin de conserver la position de lecture.

### Mentions et ouverture d'une notification

L'éditeur affiche les mentions sous la forme `@Nom`. Les identifiants sont conservés séparément
et la syntaxe canonique n'est reconstruite que pour l'envoi au moteur. Modifier ou supprimer le
nom d'une mention retire son association pour éviter de notifier une autre personne par erreur.
Les notifications affichent également les noms sans identifiants. La route d'ouverture d'un
message transmet son identifiant à la page indépendamment du fragment d'URL. Le rattrapage
événementiel conserve ce point de lecture au lieu de suivre immédiatement les messages récents.

### Lecture automatique des messages affichés

Le bouton de lecture manuelle est retiré de la page. Un message devient lu après une seconde
d'affichage effectif dans la fenêtre active. La zone visible exclut l'en-tête, le bandeau des
participants sur mobile et la saisie fixe. Le suivi reprend les messages ajoutés par les événements
ou le défilement infini. Un onglet masqué ou une fenêtre inactive ne confirme aucune lecture.

Les confirmations portent sur des identifiants précis, par lots bornés à 100, vérifiés dans
l'instance autorisée et enregistrés sous verrou de conversation. Les réceptions répétées sont
idempotentes. `sc_discussion_seen` permet de lire un message récent sans déclarer lus les messages
plus anciens jamais affichés. L'ancienne position de lecture est conservée pour les lectures
déjà enregistrées ; elle n'est plus avancée par la page. Le compteur et les autres sessions du même
utilisateur sont actualisés après commit par le canal natif.

Le schéma reste en **001** : la création de cette table est incluse dans `001/create_table.sql`
pour chaque base. Sur une installation de développement existante, exécuter une fois le script
`001/add_seen.sql` correspondant à PostgreSQL, H2, SQL Server ou Oracle avant de déployer le nouveau
code. Il n'y a pas de migration 002 ni de réinitialisation des messages existants.

### Colonne Participants et Publications citées

La colonne latérale utilise deux accordéons natifs, Participants et Publications. Les mises à jour
remplacent uniquement le contenu des listes et conservent la rubrique ouverte. La liste des
publications est dérivée de tous les messages non supprimés de l'instance, y compris l'historique
qui n'est pas chargé à l'écran. Les références répétées sont dédupliquées. Une édition ou une
suppression de message est répercutée à réception de l'événement natif. Les accusés de lecture
privés n'entraînent pas de nouveau calcul des références.

Sont reconnus les permaliens `/Publication/<id>`, les liens natifs `ViewPublication?PubId=<id>`
et `searchResult?Type=Publication&Id=<id>` des applications Kmelia, Toolbox et Kmax. Les URL doivent
appartenir à l'origine et au contexte Silverpeas courants. Aucun lien externe n'est suivi.
Les titres localisés et les droits sont résolus par les services natifs de publication : seules
les publications accessibles à l'utilisateur courant sont affichées, par titre. Les liens
s'ouvrent dans un nouvel onglet pour conserver la discussion et le brouillon.

Aucune table et aucun script SQL supplémentaires ne sont nécessaires pour cette évolution.

### Saisie en cours et notifications de bureau

La saisie dans le compositeur envoie un signal éphémère, sans texte de brouillon, via le canal
SSE/WebSocket existant. Les signaux sont limités en fréquence et expirent sous huit secondes.
Une pause de deux secondes, l'envoi ou la sortie de la page signale l'arrêt ; plusieurs onglets
d'un même utilisateur sont agrégés. Les destinataires doivent avoir accès à Discussion et pouvoir
voir l'auteur connecté selon les règles de visibilité natives de Silverpeas.

Chaque nouvelle publication de message déclenche après commit un signal de bureau distinct des
notifications de @mention. Il exclut l'auteur, n'est ni stocké ni rejoué aux utilisateurs hors ligne
et ne contient pas le texte du message. Le client global est inclus par un décorateur du plugin
natif USERNOTIFICATION, sans modifier Core, afin de fonctionner aussi dans une autre application.
Les préférences de notification des @mentions restent inchangées.

L'alerte de nouveau message est supprimée si la discussion est consultée dans une fenêtre active,
y compris un autre onglet du même navigateur. BroadcastChannel vérifie la consultation entre
onglets ; Web Locks, un historique local borné et le tag du message évitent les doublons des alertes
de nouveau message. Un clic ouvre le message avec la navigation native. Si le navigateur n'a pas
encore reçu d'autorisation, le bouton « Activer les notifications de bureau » permet de la demander
explicitement. Les limites du navigateur (autorisation, fenêtres de navigation privée, stockage
indisponible) peuvent restreindre les notifications ou leur déduplication entre onglets.

Aucun changement SQL supplémentaire n'est requis.

### Recherche native Silverpeas

Le champ de recherche interroge l’index Silverpeas de l’instance courante, au-delà des messages
chargés dans l’historique. Les résultats sont classés par pertinence, paginés par 20 et relus en
base pour exclure les messages supprimés. Chaque lien ouvre le message dans son contexte.
Le texte des messages, avec les noms lisibles des @mentions, est indexé après commit ; les
modifications remplacent l’entrée et les suppressions la retirent. Les images et le contenu des
pièces jointes ne sont pas analysés par cette recherche.

Après la première installation de cette évolution, lancer la réindexation native de l’instance
Discussion dans l’administration Silverpeas pour inclure les messages déjà présents. Le bean
`discussionIndexation` fournit cette intégration. L’indexeur natif est asynchrone : un message
peut apparaître dans les résultats après un bref délai. Les résultats du moteur global Silverpeas
peuvent aussi ouvrir un message via la route native `searchResult`.

Aucun changement SQL n’est nécessaire.

### Réponses en fil

Chaque message principal propose « Répondre » ou le nombre de réponses. Le lien ouvre une vue de
fil avec un retour à la discussion, le message d’origine et son historique de réponses, paginé
comme le flux principal. V1 : un niveau de fil, sans sous-fils. Les réponses partagent la même
conversation et sa séquence ; elles sont liées au message racine par `threadRootId`, sans dépendre
du moteur de commentaires. Un message d’une autre instance ou une réponse ne peut servir de
racine à une nouvelle réponse.

Les réponses conservent les mentions, pièces jointes, images, réactions, épingles, recherche et
notifications natives d’abonnement. Les permaliens de recherche et de notification ouvrent la
bonne vue et le message ciblé. La saisie en cours est limitée au flux ou au fil consulté. Les
non-lus sont suivis par message, et le flux principal affiche les compteurs de réponses non lues.
Un curseur de synchronisation distinct des accusés de lecture permet de traverser les séquences
appartenant aux autres fils sans requêtes en boucle ni lecture implicite.

La suppression du message d’origine ne supprime pas les réponses des autres participants : un
lien compact vers le fil subsiste dans le flux tant que des réponses sont visibles. La destruction
complète de l’instance retire d’abord les réponses pour respecter la contrainte autoréférente.

Le schéma neuf reste en version **001**. Sur un environnement déjà installé, appliquer une fois
`discussion-configuration/src/main/config/migrations/db/<base>/discussion/001/add_threads.sql`
avant d’installer ce composant. Les messages existants restent des messages principaux. Aucun
changement de version du composant ni réindexation des anciens messages ne sont requis pour
cette évolution. Les indexations complètes parcourent aussi les réponses.

### Fils dans la colonne latérale

Depuis le flux principal, « Répondre » ouvre la vue native du fil dans un panneau élargi à droite.
Les accordéons sont temporairement masqués, sans remplacer leur DOM : leur état ouvert/fermé,
le brouillon principal, les fichiers sélectionnés et la position de lecture sont conservés.
Fermer le panneau rétablit la colonne. Le fil courant reste chargé à la fermeture et retrouve
son brouillon si on le rouvre ; ouvrir un autre fil remplace cette vue.

La vue réutilise les routes existantes dans un document de même origine. Elle garde ainsi les
formulaires natifs, les fichiers, le visionneur et le suivi des non-lus. Les identifiants des
écouteurs SSE/WebSocket sont propres à chaque document pour que la conversation et le fil
restent synchronisés simultanément. Le suivi des messages du fil se suspend lorsque le panneau
est fermé, puis rattrape les changements à sa réouverture.

Sur mobile, le panneau prend toute la surface de l’application. Le contenu principal devient
inactif et les messages qu’il contient ne sont pas marqués comme lus derrière le panneau. Les
liens directs vers un fil et l’ouverture d’un compteur avec les touches de modification conservent
la vue de fil dédiée.
Aucune migration SQL supplémentaire n’est nécessaire.

Les permaliens de messages provenant des notifications, de la recherche, des épingles ou des
fichiers affichent désormais le flux principal autour du message d’origine et ouvrent le panneau
latéral sur la réponse ciblée. Le paramètre de ciblage du fil est vérifié côté serveur : une
réponse appartenant à un autre fil ou une autre instance ne peut pas être chargée dans ce contexte.

La largeur de l’application neutralise les marges et espacements horizontaux des cadres natifs
dans ce document uniquement. Dans le panneau, le fil utilise une zone de défilement dont la barre
se trouve à gauche, près du séparateur des colonnes. Seul le conteneur de défilement est en RTL ;
les messages, champs et dialogues gardent leur direction LTR. Le proxy de défilement commun est
utilisé pour la pagination, le positionnement des notifications et les accusés de lecture.


## Canaux publics

Chaque instance propose un canal Général (l'ancienne conversation `main`) et des canaux
supplémentaires partageant ses droits d'accès. Le volet Canaux est réduit par défaut.
Sur ordinateur il décale la conversation ; sur mobile il la recouvre temporairement.
Les responsables créent, renomment et archivent les canaux. Général ne peut pas être archivé.
L'archivage conserve les messages, les pièces jointes et les liens directs.

Les historiques, séquences, non-lus, mentions non lues, réactions, épingles, publications
et fichiers sont isolés par conversation. Les fils appartiennent au canal de leur racine.
Le routage `channels/{scope}/...` conserve la sélection dans chaque requête, sans état
sur le contrôleur de session. Le contexte CDI est limité à la requête ; les traitements
asynchrones résolvent explicitement le canal du message. Les anciennes URL de notification
restent valables et ouvrent le bon canal et, au besoin, le panneau du fil.

La recherche utilise l'index Silverpeas, dans le canal courant ou tous les canaux de
l'instance. Les résultats sont revérifiés en base et identifient leur canal. La réindexation
et la suppression d'une instance parcourent tous ses canaux.

Les brouillons texte, leurs fichiers sélectionnés et l'ancre de lecture sont conservés
pendant les changements de canal dans l'iframe native, dans la mémoire du navigateur
(pour les 30 derniers canaux visités par utilisateur/instance). Ils disparaissent à la
fermeture/rechargement complet de la fenêtre Silverpeas. Ils ne sont pas envoyés au serveur.

Chaque utilisateur peut choisir ALL, MENTIONS ou NONE par canal, en plus de son réglage
global des notifications de bureau. Les canaux sans notification conservent leurs non-lus.
Les événements utilisent le canal partagé SSE/WebSocket natif ; aucun polling des canaux.
La saisie et la suppression des notifications quand on consulte un canal sont cloisonnées.

Voir [sql-dev/channels/README.md](sql-dev/channels/README.md) pour mettre à jour une base
existante. La version de migration reste 001.

Le bouton d'ouverture des canaux est une icône burger avec infobulle. Les responsables
créent un canal via l'icône « + » en tête du volet et gèrent le canal sélectionné
via son engrenage. Les canaux archivés sont regroupés dans la section Archives,
repliée au chargement. Son état d'ouverture est conservé pendant les rafraîchissements
événementiels. Pour réactiver un canal, le sélectionner dans Archives, ouvrir son
engrenage, décocher « Archiver ce canal » et valider. L'historique est conservé.

L'en-tête utilise une bulle avec le compteur de non-lus, masquée à zéro et synchronisée
avec les événements et accusés de lecture existants. Le filtre près du champ de recherche
propose « Ce canal » ou « Tous les canaux ». Sa couleur, son point et son infobulle
indiquent la portée active. Un changement de portée relance la recherche depuis la
première page sans mélanger les réponses en cours. Les commandes sont accessibles
au clavier ; Échap et un clic extérieur ferment le sélecteur.


## Créer un événement Almanach depuis un message

L'icône calendrier « Créer un événement » ouvre un dialogue depuis le compositeur du
canal ou d'un fil. Le brouillon texte et ses pièces jointes sont conservés. Le dialogue
permet de choisir un Almanach du même espace, un titre, le début et la fin, ainsi qu'une
description facultative. Le fuseau de l'utilisateur Silverpeas est affiché et appliqué
côté serveur ; le fuseau du navigateur n'est pas utilisé pour interpréter les champs.
Une durée d'une heure est proposée par défaut. Les heures inexistantes lors d'un
changement d'heure sont refusées.

Les cibles sont les calendriers principaux, non synchronisés depuis un agenda externe,
des instances `almanach` directement dans le même espace. L'utilisateur doit avoir au
moins le rôle éditeur (`PUBLISHER`) dans l'Almanach, conformément à sa création native.
Si plusieurs instances conviennent, le dialogue les propose ; aucune cible d'un autre
espace ni aucun calendrier personnel ne peut être soumis à la place. Sans cible
accessible, le dialogue explique l'indisponibilité et empêche la création.

Tous les autres utilisateurs actifs autorisés dans l'instance Discussion sont ajoutés
comme invités, y compris ceux qui n'ont pas encore publié, avec dédoublonnage des
membres de groupes. Le créateur est l'organisateur. Les comptes anonymes, invités,
supprimés ou retirés sont exclus. Les identifiants des invités sont déterminés par le
serveur, jamais par le navigateur.

L'événement passe par `CalendarEvent.planOn`, qui fournit le lien permanent et les
invitations natives Almanach. Il est enregistré dans la même transaction que le message
Discussion annonçant son titre, ses horaires et son lien. Dans un fil, cette annonce
est une réponse à sa racine. Les notifications natives ne partent qu'après validation
de la transaction ; une erreur de publication annule aussi l'événement. Le compositeur
empêche les doubles clics et ne relance pas automatiquement une création en erreur.
L'historique se rafraîchit après succès, même sans connexion SSE/WebSocket active.

L'intégration utilise les API calendrier du Core, sans dépendance au WAR Almanach,
sans table ou migration SQL supplémentaire. La migration Discussion reste en 001.

Les liens permanents des événements Almanach sont affichés sous le libellé court
« Voir l’événement », tout en conservant leur URL complète pour le clic et l'infobulle.
Le rendu s'applique aussi aux messages déjà stockés et aux chargements d'historique
ou mises à jour en temps réel. Seuls les liens natifs CalendarEvent de la même origine
et du même contexte applicatif sont reconnus ; les autres URL restent explicites.

La suppression d'un message contenant un lien permanent natif vers un événement
Almanach propose de conserver l'événement, de supprimer le message et l'événement,
ou d'annuler. Le choix combiné dépend des droits natifs Almanach, revérifiés lors
du POST ; les deux suppressions partagent la même transaction. Pour un événement
récurrent, le dialogue avertit que toutes les occurrences seront supprimées.
Les annulations et notifications passent par l'API calendrier native.
La reconnaissance fonctionne aussi pour les annonces existantes tant que leur corps
contient un unique événement natif distinct de cette installation. Aucun lien ambigu
vers plusieurs événements n'entraîne de suppression automatique. Aucun changement SQL.


### Proposer une idée

L'ampoule du compositeur ouvre une proposition native dans une boîte à idées du même
espace. L'action « Proposer une idée » au survol d'un message reprend son texte côté
serveur, sans les identifiants techniques des mentions. Le titre et la description
restent modifiables ; les pièces jointes ne sont pas recopiées. Le brouillon et les
fichiers du compositeur restent intacts.

Les cibles sont les instances `suggestionBox` directement dans le même espace, avec
accès effectif et rôle au moins rédacteur. Les droits sont revérifiés au POST. Les
messages sources d'un autre canal, supprimés ou inconnus et les canaux archivés sont
refusés. Le texte saisi est encodé avant de devenir le contenu HTML natif de l'idée.

`SuggestionCollection.add` puis `publish` conservent l'auteur, le circuit natif de
validation, les notifications et l'indexation de la boîte à idées : un rédacteur
soumet à validation, un éditeur publie directement. Le résultat précise si l'idée
attend une validation. Un message avec son lien permanent « Voir l'idée » est ajouté
dans le canal ou le fil courant ; le message source reste inchangé. L'accès à l'idée
reste soumis aux droits et à la validation de la boîte cible.

La bibliothèque SuggestionBox est une dépendance `provided`, déjà présente dans
l'EAR Silverpeas ; elle n'est pas embarquée une seconde fois dans le WAR Discussion.
Aucune modification SQL : la migration Discussion reste en 001.


### Confirmation de suppression des messages et idées

Tous les messages demandent confirmation avant le POST de suppression. La croix,
Échap et « Annuler » ne suppriment rien. Pour une idée ou un événement natif cité par
un unique lien permanent de cette installation, le dialogue propose de supprimer
seulement le message ou les deux contributions. Les liens vers plusieurs idées ou
événements distincts ne sélectionnent aucune contribution arbitraire : seule la
suppression du message est alors proposée. Les annonces existantes sont reconnues.

La suppression d'une idée suit les règles de SuggestionBox : accès effectif à la
boîte, auteur ou gestionnaire pour un brouillon/une idée refusée ; gestionnaire de
la boîte ou administrateur de plateforme pour une idée publiée ou en validation.
Un simple auteur ne peut donc pas supprimer son idée déjà soumise. Le dialogue
l'explique et conserve l'idée quand les droits sont insuffisants. Le POST revérifie
les droits et la version du message, puis utilise la collection native pour retirer
l'idée avec son nettoyage de contenu. Une erreur de droits laisse le message intact.
Les choix Almanach et Boîte à idées sont distincts ; leur cumul est refusé.
Aucune nouvelle table ni migration SQL n'est nécessaire.


### Créer un vote

L'icône de graphique du compositeur ouvre un vote natif dans une application Vote
(`pollingStation`) directement dans le même espace. Les applications Sondage
(`survey`) ne sont jamais proposées. Il faut un accès effectif et le rôle éditeur
ou gestionnaire dans la cible ; Discussion exige aussi un canal non archivé et le
droit d'écriture. Ces contrôles sont refaits au POST.

Le formulaire contient une seule question (160 caractères), de 2 à 10 réponses
distinctes (200 caractères chacune, une par ligne), le choix unique ou multiple et
l'anonymat. Le paramètre natif `useAnonymousMode=yes` force l'anonymat côté serveur,
même si le navigateur soumet le contraire. Le vote est ouvert immédiatement, sans
date de fin, avec résultats immédiats selon le rendu natif ; les autres réglages
peuvent être gérés ensuite dans Vote.

Le moteur Core `QuestionContainerService` enregistre un unique `Question` de style
`radio` ou `checkbox`, avec ses réponses natives, son auteur et son indexation.
La participation, les résultats et le contrôle des participations restent dans
Vote. Discussion publie une annonce dans le canal ou le fil courant et affiche
le lien natif sous « Participer au vote ». Le brouillon et les fichiers du message
restent intacts, les doubles clics sont bloqués et aucune création en échec n'est
relancée automatiquement.

La création native historique fonctionne hors transaction. L'adaptateur nettoie
un vote partiellement créé lorsqu'il connaît son identifiant et enregistre une
compensation CDI `AFTER_FAILURE` pour supprimer le vote si la transaction de son
annonce échoue. Il ne prétend pas rendre ce moteur historique atomique avec JPA.
Les tests du nettoyage utilisent des collaborateurs simulés ; aucun vote réel n'est
créé ou supprimé dans l'instance de développement.

La confirmation de suppression propose de garder le vote ou de supprimer les deux,
selon les droits natifs éditeur/gestionnaire revérifiés au POST. La suppression
native retire aussi les participations et réponses. Les URL étrangères, liens de
Sondage ou messages citant plusieurs contributions ne sélectionnent aucun vote à
supprimer. Le moteur calendrier et la boîte à idées conservent leurs choix propres.
La dépendance Core est `provided` ; aucune migration SQL, toujours version 001.


Les liens natifs vers les événements, idées, votes et publications utilisent le
mécanisme `sp-link` de Silverpeas : ils ouvrent la ressource dans la navigation
courante, y compris depuis un fil, et conservent la structure de la plateforme.
Les publications de l'accordéon utilisent aussi ce mécanisme. Les liens étrangers
à l'installation et les autres URL conservent leur ouverture dans un nouvel onglet.

Les URL des publications citées sont remplacées par leur titre, fourni par la liste
des citations autorisées côté serveur. Le titre suit les actualisations de cette
liste et s'applique aussi à l'historique chargé ensuite. L'URL complète reste dans
l'infobulle et la destination est conservée ; sans titre autorisé disponible, elle
reste explicite. Aucun appel réseau par message ni modification du texte stocké.


### Markdown et collage d'images

Le compositeur utilise maintenant un éditeur visuel Tiptap. Le bouton « Aa » affiche une barre masquée
par défaut : gras, italique, barré, listes, citation, bloc de code et lien nommé.
`Ctrl+B`/`Cmd+B` et `Ctrl+I`/`Cmd+I` appliquent ou retirent la mise en forme autour de
la sélection. La mise en forme est visible pendant la rédaction et la modification ; les marqueurs
Markdown restent uniquement dans le stockage, dans la limite existante de 2 000 caractères. Les mentions conservent leurs
identifiants et notifications lorsqu'elles sont entourées de marqueurs.

Les messages et réponses sont rendus avec markdown-it 15.0.3, embarqué localement
avec sa licence et les notices de ses dépendances. Le profil `zero` n'active qu'un
sous-ensemble : pas de HTML, images Markdown, tableaux, titres ou styles libres.
Les protocoles exécutables ne sont pas autorisés. Le code reste littéral, les mentions
restent cliquables hors code et les liens nommés conservent le libellé saisi. Les liens
natifs continuent d'utiliser la navigation Silverpeas. Les nouveaux fragments de
l'historique et les mises à jour utilisent le même rendu ; aucun HTML n'est stocké.
Les messages existants contenant de la syntaxe Markdown sont aussi interprétés.

Coller une image dans le compositeur ajoute une pièce jointe avec aperçu et retrait,
sans la convertir en donnée Base64 dans le message. Le collage et le sélecteur
partagent les mêmes contrôles : JPEG, PNG ou GIF, 4 images maximum, 5 Mo chacune.
Un collage refusé conserve les images et le brouillon précédents. Le texte normal
ou une légende présente avec l'image conserve le comportement de collage natif.
Les brouillons de canaux et les messages composés uniquement d'images continuent de
fonctionner avec les pièces jointes natives et leur validation serveur.
Aucune modification SQL ni changement de version de migration.


L'édition d'un message affiche maintenant un panneau sur toute la largeur de son
contenu, à la place du rendu publié et de ses actions. Les outils de mise en forme
restent disponibles ; « Annuler » et « Enregistrer » sont alignés à droite. Annuler
ou presser Échap dans le champ restaure le texte et les identifiants des mentions,
sans POST, puis rend le focus au bouton Modifier. Fermer seulement la barre Markdown
avec Échap laisse l'édition ouverte. Le panneau utilise le même formulaire, la même
version optimiste et le même état natif `details[open]` pour protéger les éditions
des remplacements en temps réel, y compris dans l'historique chargé à la demande.


### Édition visuelle Tiptap

Tiptap 3.32.2 et le sérialiseur stable ProseMirror Markdown 1.13.4 sont embarqués
localement, sous licence MIT, sans service cloud ni extension Pro. L'interface
Silverpeas reste compacte : Aa déplie les formats et indique les styles actifs ;
le lien dispose d'un petit formulaire de libellé et d'adresse. Les commandes clavier,
annuler/rétablir, mentions atomiques, emojis et pièces jointes sont conservés.
Les mentions sont sérialisées en `@[Nom](id)` ; seul le nom apparaît à l'écran.
Les images collées utilisent toujours les pièces jointes natives.

Les créations et réponses montent immédiatement l'éditeur ; les modifications le
montent à l'ouverture uniquement. Les éditeurs retirés par l'historique ou une mise
à jour sont détruits. Un champ texte masqué et désactivé conserve la compatibilité
avec les brouillons de canaux et les signaux de saisie ; seul le champ `body`
transmet le Markdown canonique. Les changements externes et l'annulation resynchronisent
le rendu visuel. La longueur est contrôlée sur la source sérialisée, les collages
HTML sont limités au schéma de l'éditeur et les liens exécutables sont refusés.

Le code de reconstruction, le lockfile npm et les notices des dépendances figurent
dans `Silverpeas-Core/core-war/src/main/frontend/richEditor`. Le bundle représente environ 181 Ko gzip
et ne demande aucun téléchargement externe à l'exécution. Maven utilise le bundle
commité : Node/npm sont nécessaires uniquement pour sa reconstruction.
Aucune migration SQL.


### Limites des pièces jointes par instance

Les paramètres natifs de l'application exposent `maxImagesPerMessage` (4 par défaut),
`maxImageSizeMb` (5 Mo par image), `maxFileSizeMb` (20 Mo par fichier) et
`maxFilesPerMessage` (4 fichiers par message).
Les valeurs sont des entiers strictement positifs ; les tailles vont de 1 à 2047 Mo
(1 Mo = 1 048 576 octets). Une valeur absente, vide ou invalide reprend uniquement
la valeur par défaut du paramètre concerné. Le nombre de fichiers reste limité à 4.

La politique est relue côté serveur à chaque envoi et s'applique à tous les canaux
et fils de l'instance. Les flux sont bornés sur leur contenu réel, indépendamment de
la taille annoncée, et le service métier revérifie les objets reçus avant stockage.
Après rechargement de Discussion, le sélecteur, le collage d'images, les infobulles
et les erreurs reflètent les valeurs de l'instance. Les pièces déjà publiées restent
accessibles. Les limites globales de requêtes et d'upload de la plateforme continuent
de s'appliquer. Aucun changement SQL.


La liste des canaux est affichée dès l'ouverture de Discussion. Son bouton de repli
utilise une flèche vers une barre ; le menu de l'entête permet de la rouvrir et
expose l'état courant aux technologies d'assistance. Sur petit écran, la liste
ouverte reste un volet superposé, refermable avec Échap. Elle reste masquée dans
les réponses embarquées.

La création d'un canal publie une annonce dans Général, au nom de son créateur,
avec un lien nommé vers le nouveau canal utilisant la navigation native Silverpeas.
L'annonce utilise le circuit normal des messages (indexation, non-lus, notifications
et temps réel). La création et la publication participent à la même transaction ;
la sélection du canal d'origine est restaurée même si la publication échoue.
Aucune annonce supplémentaire lors d'un renommage ou d'un archivage. Aucun SQL.


Les noms des auteurs de messages et des participants utilisent la balise native
`view:username` et la bulle standard `userZoom` de Silverpeas, sans dupliquer le
profil, les contacts ou les invitations. Les règles natives s'appliquent notamment
à l'utilisateur courant et aux comptes système, supprimés ou désactivés.
Les nouveaux noms insérés par l'historique, le temps réel et le rafraîchissement des
participants activent le même plugin une seule fois. Les réponses en fil chargent
la même vue et le même plugin. Aucun changement SQL.


### Citer et créer un fil

« Citer » ajoute dans le compositeur de la conversation courante une citation
visuelle avec l'auteur, un extrait de 300 caractères au maximum et un lien vers
le message original. Le brouillon et les pièces jointes sont conservés. Une
nouvelle citation remplace la précédente ; la croix de la citation la retire sans
effacer la réponse. Les messages contenant seulement une pièce jointe peuvent
également être cités. Les identifiants techniques des mentions ne sont pas copiés,
pour ne pas notifier à nouveau les personnes citées.

La citation est un bloc Markdown avec un lien natif Silverpeas : aucun nouveau
champ SQL, et les brouillons, éditions, recherches et publications conservent le
même format. Elle constitue un instantané modifiable ; elle ne se réécrit pas si
l'original est modifié ou supprimé. La limite de 2 000 caractères inclut la citation.
« Créer un fil » remplace le libellé de l'ancienne action « Répondre » sur les
messages sans fil. Les fils existants conservent leur compteur et leur panneau.


Les smileys textuels courants (`:)`, `:-)`, `;)`, `:D`, `:(`, `:P`, `:O`,
`:/`, `:'(`, `<3` et leurs variantes) sont rendus en emojis Unicode dans les
messages, les fils et le chargement de l'historique. Le rendu Markdown partagé
s'applique également à l'ouverture de l'éditeur visuel. Les liens, mentions,
blocs de code et code en ligne restent intacts. Les smileys doivent être isolés
pour ne pas modifier des identifiants ou chemins. Le texte déjà stocké n'est pas
réécrit ; l'enregistrement d'une édition conserve le texte visible dans l'éditeur.
Aucun SQL ni nouvelle dépendance.


### Éditeur mutualisé avec les commentaires Core

Discussion charge désormais le plugin natif `richEditor` de Core. Tiptap, le rendu
Markdown sécurisé, les emoji et les mentions ont une implémentation commune avec
les commentaires Silverpeas. Les scripts Discussion sont des adaptateurs : les
pièces jointes, le collage d'images et les actions métier restent propres à Discussion.

Construire et installer dans Maven le Core `feature-15552` avant de compiler
Discussion. Les tests de navigateur extraient ses ressources du WAR Core ; les
bundles ne sont plus dupliqués dans le composant. La migration Discussion reste 001.
La nouvelle colonne de commentaires natifs relève de la migration Core, pas de
Discussion. Documentation : `Silverpeas-Core/docs/rich-editor/README.md`.

### Retirer une image ou un fichier d’un message

L’auteur du message ou un administrateur peut ouvrir « Modifier », puis utiliser
la croix sur les images et fichiers à retirer. Les éléments sélectionnés restent
visibles en atténué (images) ou barrés (noms de fichiers), avec une action pour les
conserver à nouveau avant l’enregistrement. « Annuler » ou Échap rétablit le texte
et toutes les sélections sans suppression.

Le retrait est enregistré avec le message et sa version. Le serveur vérifie les
droits, l’appartenance et la catégorie des pièces jointes, puis supprime uniquement
les éléments sélectionnés après validation de la transaction. Les images et les
fichiers peuvent être retirés en une seule modification. Le changement est propagé
en temps réel, actualise l’accordéon « Fichiers partagés » et déclenche les
notifications de modification habituelles. Le message doit conserver du texte,
une image ou un fichier ; sinon il faut supprimer le message entier.
Aucune modification de schéma SQL n’est nécessaire.

### Vérification des libellés

Les textes de l’interface (y compris infobulles, accessibilité et validation)
proviennent des bundles `DiscussionBundle` ; les notifications utilisent les
bundles et les modèles StringTemplate localisés. Les textes métier saisis par
les utilisateurs restent inchangés. Les messages techniques des exceptions et
les identifiants internes ne sont pas des libellés d’interface.

Le test `discussion-i18n.test.cjs` contrôle que les clés utilisées par les sources
Java/JSP/JavaScript existent dans les bundles par défaut, français, anglais et
allemand. L’éditeur partagé Core fournit également ses valeurs par défaut depuis
son bundle natif, sans texte anglais en dur dans le JavaScript.

## Réglages de fonctionnement

Les paramètres globaux sont dans
`properties/org/silverpeas/components/discussion/settings/DiscussionSettings.properties`.
Ils peuvent être surchargés par les mécanismes habituels de configuration Silverpeas,
notamment un bloc `configfile` du CustomerSettings pointant vers ce fichier. Les
valeurs par défaut préservent le comportement existant.

| Clé | Défaut | Usage |
| --- | ---: | --- |
| `history.pageSize` | 30 | Messages chargés par page, y compris dans un fil et autour d’un message |
| `search.pageSize` | 20 | Résultats de recherche par page |
| `presence.refreshMs` | 30000 | Intervalle de mise à jour des participants connectés |
| `read.visibilityMs` | 1000 | Temps d’affichage avant lecture automatique |
| `read.retryMs` | 2000 | Nouvelle tentative d’envoi d’un accusé de lecture |
| `typing.intervalMs` | 2500 | Intervalle minimal entre signaux de saisie côté navigateur |
| `typing.throttleMs` | 2000 | Limitation des signaux de saisie côté serveur |
| `typing.idleMs` | 5000 | Inactivité avant arrêt du signal de saisie |
| `typing.ttlMs` | 8000 | Expiration d’un signal de saisie |
| `typing.maxStates` | 10000 | Nombre maximal d’états de saisie gardés en mémoire |
| `live.retryInitialMs` | 1000 | Premier délai de nouvelle tentative après échec de synchronisation |
| `live.retryMaxMs` | 30000 | Plafond du délai de nouvelle tentative |
| `pins.excerptLength` | 180 | Caractères Unicode d’un extrait de message épinglé |
| `quote.excerptLength` | 300 | Caractères Unicode d’une citation |
| `event.durationMinutes` | 60 | Durée proposée pour un événement |
| `event.leadMinutes` | 15 | Décalage initial de l’événement par rapport à l’heure courante |

Les unités et plages autorisées sont indiquées dans le fichier. Une valeur absente,
invalide ou hors plage reprend sa valeur par défaut. L’intervalle de saisie effectif
ne peut pas être inférieur au seuil serveur ; son expiration ne peut pas être
inférieure à l’inactivité plus l’intervalle effectif. Le plafond des nouvelles tentatives ne
peut pas être inférieur au délai initial. Le navigateur reçoit les valeurs validées
par le serveur ; les scripts conservent les anciennes valeurs uniquement comme
secours pour des pages déjà ouvertes ou en cache.

Le nombre de fichiers se règle **par instance**, dans les paramètres de l’application.
Il est appliqué au sélecteur de fichiers, aux messages d’aide traduits et à la
validation serveur. Les instances existantes sans valeur conservent la limite de 4.

Les contraintes de stockage (dont les 2 000 caractères d’un message), les formats
acceptés, les limites de lots des API et les bornes de sécurité restent techniques.
La synchronisation des messages reste événementielle ; ces réglages n’ajoutent pas
de scrutation périodique des messages. Aucun script SQL ni nouvelle version de
migration n’est requis.
