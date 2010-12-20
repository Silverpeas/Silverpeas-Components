<div style="font-family:Arial;font-size:11px">Un nouveau thème a été créé à l'emplacement suivant : 

<b> $path$ > $topicName$</b>

$if(notification_receiver_users)$
<i>Ce message a été envoyé aux utilisateurs : $notification_receiver_users$</i>
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  Ce message a été envoyé
  $endif$
 aux groupes : $notification_receiver_groups$
$endif$
</div>