Die Anzeige <b>$classified.title$</b> muss überprüft werden.

$if(notification_receiver_users)$
Diese Nachricht wurde
 den Benutzern gesendet : $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  Diese Nachricht wurde
  $endif$
 den Gruppen gesendet : $notification_receiver_groups$
$endif$