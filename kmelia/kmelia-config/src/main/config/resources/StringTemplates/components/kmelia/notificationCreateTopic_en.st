<div style="font-family:Arial;font-size:11px">A new theme has been added in the following place :  

<b> $path$  > $topicName$ </b>

$if(notification_receiver_users)$
<i>This message has been sent  to users : $notification_receiver_users$</i>
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  This message has been sent to
  $endif$
 to groups : $notification_receiver_groups$
$endif$
</div>