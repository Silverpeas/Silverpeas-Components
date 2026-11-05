# Édition des fichiers joints dans le navigateur

Une icône « Modifier dans le navigateur » est proposée à côté du visionneur dans les messages,
les réponses en fil et l’accordéon « Fichiers partagés ». Elle ouvre un nouvel onglet et conserve
le brouillon de Discussion.

L’action est disponible uniquement si un client d’édition WBE est installé, activé et prend en
charge le format bureautique du fichier. Elle réutilise la configuration Collabora/WOPI existante.
Aucune dépendance directe au module optionnel n’est ajoutée ; sans ce module, l’icône est masquée.

## Coédition sans réservation

Discussion expose directement la pièce jointe à l’interface WBE native. Plusieurs membres ayant
les droits d’écriture sur l’application peuvent rejoindre le même document avec un identifiant
commun stable. Il n’y a aucune réservation WebDAV, ni étape de libération manuelle. Les verrous
techniques du protocole WOPI restent gérés par le moteur natif pour coordonner les enregistrements.
Un fichier déjà réservé par un ancien mécanisme n’est pas écrasé ni libéré de force.

Chaque enregistrement met à jour la pièce jointe d’origine via le service natif des documents,
actualise sa taille et sa date, puis signale le changement aux sessions Discussion.
Le fichier reste accessible au téléchargement et au visionneur sous le même identifiant.
La limite de taille des fichiers de l’instance est contrôlée avant remplacement du contenu.

## Droits et cycle de vie

Les droits d’accès à l’application sont contrôlés lors de l’ouverture et lors des échanges WBE.
Les comptes invités, anonymes, supprimés ou désactivés ne peuvent pas éditer. Un message supprimé
ou un accès retiré empêche de poursuivre l’édition. Les documents d’un canal archivé restent
consultables mais ne sont plus modifiables. L’identité de l’utilisateur authentifié par WOPI
reste limitée à la requête ; elle n’est pas conservée sur l’objet partagé entre les coéditeurs.

Aucune migration SQL ni modification de Core n’est nécessaire.

## Validation manuelle avec Collabora

1. Joindre un document bureautique pris en charge dans un canal actif.
2. Ouvrir son édition avec deux comptes autorisés et vérifier la coédition.
3. Enregistrer, revenir dans Discussion et télécharger le fichier modifié.
4. Vérifier qu’aucune réservation ni action de libération n’apparaît.
5. Vérifier l’absence de l’action lorsque le module est désactivé ou le format incompatible.
6. Vérifier le refus d’enregistrement après retrait d’accès, archivage ou suppression du message.

Les tests automatisés couvrent l’adaptateur WBE, les contrôles d’accès, les écritures bornées,
les routes et l’ouverture de l’éditeur. La coédition effective doit aussi être validée sur une
instance disposant d’un serveur Collabora opérationnel.
