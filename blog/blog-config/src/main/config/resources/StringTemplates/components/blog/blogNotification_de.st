$senderName$ informiert Sie ein Beitrag zur Verfügung steht...

$if(senderMessage)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">"$senderMessage$"</div>
$endif$

Name des Beitrags : $blogName$
Datum : $blogDate$
$if(blogCategorie)$
Kategorie : $blogCategorie$
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
