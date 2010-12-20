<div style="font-family:Arial;font-size:11px">
$senderName$  has created a new document <b>$publicationName$</b> which is now available here : $path$

$if(senderMessage)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">" $senderMessage$ " </div>
$endif$

$if(notification_receiver_users)$
<i>This message has been sent to users : $notification_receiver_users$ </i>
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  This message has been sent to
  $endif$
 to groups : $notification_receiver_groups$
$endif$
</div>