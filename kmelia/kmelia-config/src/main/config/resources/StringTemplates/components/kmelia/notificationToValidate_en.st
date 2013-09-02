<div style="font-family:Arial;font-size:11px">
Publication <b>$publicationName$</b> have to be validated. $path$

$if(notification_receiver_users)$
This message has been sent to users : $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  This message has been sent 
  $endif$
 to groups : $notification_receiver_groups$
$endif$
</div>