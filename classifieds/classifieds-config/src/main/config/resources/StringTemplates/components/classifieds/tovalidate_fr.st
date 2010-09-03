La petite annonce <b>$classified.title$</b> est accessible en ligne.
Si le contenu de cette annonce ne vous convient pas, vous pouvez refuser sa mise en ligne.

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