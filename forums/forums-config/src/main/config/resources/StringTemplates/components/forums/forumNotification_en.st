<div style="font-family:Arial;font-size:11px">
The forum <b>$title$</b> has been created.

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