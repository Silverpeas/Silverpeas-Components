Die Veröffentlichung '$publicationName$' ist online verfügbar. 
Wenn Ihnen der Inhalt dieser Veröffentlichung nicht passt, können Sie sie ändern oder ihre Onlinesetzung rückgängig machen.

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
