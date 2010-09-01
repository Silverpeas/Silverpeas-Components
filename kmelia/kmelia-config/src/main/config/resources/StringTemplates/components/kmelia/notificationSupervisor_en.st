The publication '$publicationName$' is available online. 
If you do not agree with its content, you can modify it or cancel its online status.
 
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
