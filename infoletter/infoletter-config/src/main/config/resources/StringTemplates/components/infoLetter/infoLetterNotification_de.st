$senderName$ teilt Ihnen mit, dass Ihnen eine neue Publikation zur Verfügung steht...

Name : $infoLetterTitle$
$if(infoLetterDesc)$
Beschreibung : $infoLetterDesc$
$endif$


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
