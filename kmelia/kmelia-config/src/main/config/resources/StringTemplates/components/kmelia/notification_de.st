$senderName$ teilt Ihnen mit, dass Ihnen ein neues Dokument zur Verfügung steht...
$if(senderMessage)$
Message :
"$senderMessage$" 
$endif$
 
Vous trouverez la publication "$publicationName$" ici : $path$

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
