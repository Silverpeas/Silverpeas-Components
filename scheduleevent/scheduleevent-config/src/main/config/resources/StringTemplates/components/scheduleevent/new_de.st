<div style="font-family:Arial;font-size:11px">
<b>$senderName$</b> wants you to participate in the event <b>$eventName$</b>.
For that, thank you to communicate your availability as soon as possible by clicking the attached link.

$if(notification_receiver_users)$
<i>Diese Nachricht wurde gesendet  nutzer : $notification_receiver_users$ </i>
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  Diese Nachricht wurde gesendet
  $endif$
 gruppen : $notification_receiver_groups$
$endif$
</div>