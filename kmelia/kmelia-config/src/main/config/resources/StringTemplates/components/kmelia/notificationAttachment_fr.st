
$senderName$ vous informe de l'existence d'un document...

$if(senderMessage)$
Message :
"$senderMessage$"<br/>
$endif$

Vous trouverez ce document attaché à la publication "$publicationName$" ici :
$path$

$if(attachmentTitle)$
Nom du document : $attachmentTitle$<br/>
$endif$
$if(attachmentDesc)$
Description du document : $attachmentDesc$<br/>
$endif$
Nom du fichier : $attachmentFileName$
$if(attachmentCreationDate)$
Date de création : $attachmentCreationDate$<br/>
$endif$
$if(attachmentSize)$
Taille : $attachmentSize$<br/>
$endif$
$if(attachmentAuthor)$
Auteur : $attachmentAuthor$<br/>
$endif$
$if(attachmentMajorNumber)$
Version du document : $attachmentMajorNumber$.$attachmentMinorNumber$<br/>
$endif$

$if(notification_receiver_users)$
Ce message a été envoyé
 aux utilisateurs : $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  Ce message a été envoyé
  $endif$
 aux groupes : $notification_receiver_groups$
$endif$


