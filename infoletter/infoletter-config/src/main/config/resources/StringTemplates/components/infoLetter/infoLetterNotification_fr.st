$senderName$ vous informe de la mise en ligne d'une nouvelle parution...
 
Nom : $infoLetterTitle$
$if(infoLetterDesc)$
Description : $infoLetterDesc$
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
