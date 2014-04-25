$senderName$ informs you that a suggestion <b>$title$</b> is proposed by <i>$authorName$</i>.

$if(senderMessage)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">"$senderMessage$"</div>
$endif$

$if(notification_receiver_users)$
This message has been sent
 to users : $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  This message has been sent to
  $endif$
 to groups : $notification_receiver_groups$
$endif$
