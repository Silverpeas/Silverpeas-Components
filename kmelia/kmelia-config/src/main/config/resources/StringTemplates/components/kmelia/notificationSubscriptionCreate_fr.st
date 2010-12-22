<div style="font-family:Arial;font-size:11px">
Le document <b>$publicationName$</b> a été créé dans l'un de vos abonnements. 

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
</div>