<div style="font-family:Arial;font-size:11px">
The suggestion <b>$title$</b> has been publish.<br/>
You can now vote for or against it.

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