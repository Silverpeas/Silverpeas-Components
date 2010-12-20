<div style="font-family:Arial;font-size:11px">
<b>$senderName$</b> teilt Ihnen mit, dass Ihnen ein Dokument <b>$publicationName$</b> zur Verfügung steht hier : $path$

$if(senderMessage)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">" $senderMessage$ "</div>
$endif$

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