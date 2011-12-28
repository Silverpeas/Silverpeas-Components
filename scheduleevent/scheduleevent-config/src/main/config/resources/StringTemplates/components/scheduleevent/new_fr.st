<div style="font-family:Arial;font-size:11px">
<b>$senderName$</b> souhaite que vous participiez à l'&eacute;v&eacute;nement <b>$eventName$</b>.
Pour cela, merci de lui faire part de vos disponibilit&eacute;s au plus vite en cliquant sur le lien ci-joint.

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