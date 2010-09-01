In einem Ihrer Abonnemente ist ein Dokument erstellt worden.
Name der Veröffentlichung : "$publicationName$"
Speicherort : $path$

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
