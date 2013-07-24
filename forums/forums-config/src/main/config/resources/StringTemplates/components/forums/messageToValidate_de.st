<div style="font-family:Arial;font-size:11px">
$if(isSubject)$
A new subject was posted.
$else$
A new message was posted in response to <b>$originTitle$</b>.
$endif$
<br/>Title : <b>$title$</b>.
Thank you to validate.

$if(notification_receiver_users)$
Diese Nachricht wurde gesendet nutzer : $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  Diese Nachricht wurde gesendet
  $endif$
 gruppen : $notification_receiver_groups$
$endif$
</div>