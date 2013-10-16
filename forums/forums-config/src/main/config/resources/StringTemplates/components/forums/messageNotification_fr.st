<div style="font-family:Arial;font-size:11px">
$if(isSubject)$
Un nouveau sujet a été posté.
$else$
Un nouveau message a été posté en réponse à <b>$originTitle$</b>.
$endif$

Titre : <b>$title$</b>
 
$if(notification_receiver_users)$
<i>Ce message a été envoyé aux utilisateurs : $notification_receiver_users$ </i>
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  Ce message a été envoyé
  $endif$
 aux groupes : $notification_receiver_groups$
$endif$
</div>