$senderName$ vous informe qu'une idée <b>$title$</b> est proposée par <i>$authorName$</i>.

$if(senderMessage)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">"$senderMessage$"</div>
$endif$

$if(notification_receiver_users)$
Ce message a été envoyé
 aux utilisateurs : $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  Ce message a été envoyé
  $endif$
 aux groupes : $notification_receiver_groups$
$endif$
