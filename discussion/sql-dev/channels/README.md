# Mise à jour multicanal — environnement de développement

La migration du composant reste en **version 001**. Les installations neuves utilisent
`001/create_table.sql`, déjà enrichi. Sur une base existante, exécuter **une seule fois**
le script correspondant avant de déployer le nouveau composant.

PostgreSQL, par exemple (adapter le compte et la base) :

```sh
psql -U postgres -d Silverpeas -v ON_ERROR_STOP=1 --single-transaction -f postgresql-add-channels.sql
```

Les quatre colonnes ajoutées sont `channelName`, `channelDescription`, `archived`
sur `sc_discussion_conversation`, et `notificationMode` sur `sc_discussion_read`.
Aucun message, fichier, état de lecture ou préférence existant n'est supprimé.
La conversation `scopeKey = main` devient le canal Général. Son nom localisé est
utilisé tant qu'un responsable ne lui donne pas un autre nom.

Les canaux sont accessibles aux membres autorisés de l'instance. Les responsables
peuvent les créer, les renommer et les archiver. Général reste toujours disponible.
Les canaux archivés conservent les liens vers leurs messages et n'acceptent plus
les nouveaux messages. Les préférences ALL / MENTIONS / NONE s'appliquent aux
notifications du canal ; la préférence existante de bureau reste globale à l'instance.
Les notifications natives nécessitent toujours un abonnement ou une mention.

Ces scripts ne sont pas des étapes de migration automatiques et ne doivent pas
être exécutés sur une installation neuve ni une seconde fois.
