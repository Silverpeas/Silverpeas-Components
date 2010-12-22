<div style="font-family:Arial;font-size:11px">
Publication <b>$publicationName$</b> has been added to one of your subscriptions.

Path : $path$

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
</div>