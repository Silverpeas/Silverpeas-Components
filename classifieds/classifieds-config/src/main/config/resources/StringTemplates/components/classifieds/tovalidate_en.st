The ad <b>$classified.title$</b> has to be validated.

$if(notification_receiver_users)$
This message has been sent
 to following users : $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  This message has been sent
  $endif$
 to following groups : $notification_receiver_groups$
$endif$