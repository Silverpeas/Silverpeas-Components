<div style="font-family:Arial;font-size:11px">
$senderName$ informs you about the following news...

<b>$title$</b>
$description$

$if(senderMessage)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">"$senderMessage$"</div>
$endif$

$if(notification_receiver_users)$
This message is sent
 to following users : $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  This message is sent
  $endif$
 to following groups : $notification_receiver_groups$
$endif$
</div>