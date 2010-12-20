<div style="font-family:Arial;font-size:11px">Am folgenden Speicherort ist ein neues Thema erstellt worden :  

<b> $path$ > $topicName$  </b>

$if(notification_receiver_users)$
<i>Diese Nachricht wurde gesendet  nutzer : $notification_receiver_users$</i>
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  Diese Nachricht wurde gesendet
  $endif$
 gruppen : $notification_receiver_groups$
$endif$
</div>