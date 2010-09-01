La publication '$publicationName$' est accessible en ligne. 
Si le contenu de cette publication ne vous convient pas, vous pouvez le modifier ou annuler sa mise en ligne.

Emplacement : $path$ 

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
