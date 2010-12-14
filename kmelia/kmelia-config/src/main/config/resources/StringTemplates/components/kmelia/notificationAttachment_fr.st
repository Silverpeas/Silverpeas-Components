$senderName$ vous informe de l'existence d'un document...
$if(senderMessage)$
Message :
"$senderMessage$" 
$endif$
 
Vous trouverez ce document attaché à la publication "$publicationName$" ici : $path$

Nom du document : $attachmentTitle$
Description du document : $attachmentDesc$
Nom du fichier : $attachmentFileName$
$if(attachment.majorNumber)$
Version du document : $attachment.majorNumber$.$attachment.minorNumber$
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
