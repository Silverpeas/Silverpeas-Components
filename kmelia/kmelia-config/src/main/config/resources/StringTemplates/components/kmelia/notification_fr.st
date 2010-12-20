<div style="font-family:Arial;font-size:11px">
<b>$senderName$</b> vous informe que le document <b>$publicationName$</b> est désormais à votre disposition ici : $path$

$if(senderMessage)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">" $senderMessage$ "</div>
$endif$
 
$if(notification_receiver_users)$
<i>Ce message a été envoyé aux utilisateurs : $notification_receiver_users$ </i>
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  Ce message a été envoyé
  $endif$
 aux groupes : $notification_receiver_groups$
$endif$
</div>