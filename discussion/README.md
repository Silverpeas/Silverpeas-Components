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
- Historique chronologique (récents en bas), pages de 30 messages, curseur exclusif stable, limite métier de 100.
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
après validation de leur transaction. Le signal contient uniquement l'identifiant de l'instance ;
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

### Hauteur dans la navigation Silverpeas

Discussion mesure la hauteur disponible dans son propre document (iframe), sous le fil d'Ariane
et en réservant les marges et espacements réels du cadre natif. La mesure suit les redimensionnements
de la fenêtre, du viewport et du cadre, sans modifier le document parent. L'en-tête et la saisie
restent hors du défilement de l'historique ; les auteurs disposent d'une liste défilante indépendante.
Sur écran étroit, les auteurs sont affichés dans un bandeau au-dessus du fil.
