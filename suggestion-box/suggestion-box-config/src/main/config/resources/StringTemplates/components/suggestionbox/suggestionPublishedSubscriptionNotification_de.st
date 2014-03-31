<div style="font-family:Arial;font-size:11px">
Der Vorschlag <b> $ title $ </ b> ist zu veröffentlichen. <br/>
Sie können nun für oder gegen ihn stimmen.

$if(notification_receiver_users)$
Diese Nachricht wurde gesendet
 nutzer : $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  Diese Nachricht wurde gesendet
  $endif$
 gruppen : $notification_receiver_groups$
$endif$
</div>