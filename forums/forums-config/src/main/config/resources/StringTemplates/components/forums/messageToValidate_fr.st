<div style="font-family:Arial;font-size:11px">
$if(isSubject)$
Un nouveau sujet a été posté.
$else$
Un nouveau message a été posté en réponse à <b>$originTitle$</b>.
$endif$
<br/>Titre : <b>$title$</b>.
Merci de le valider.

$if(notification_receiver_users)$
Ce message a été envoyé aux utilisateurs : $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  Ce message a été envoyé
  $endif$
 aux groupes : $notification_receiver_groups$
$endif$
</div>