$senderName$ inform you of the launch of new parution... 

Name : $infoLetterTitle$
$if(infoLetterDesc)$
Description : $infoLetterDesc$
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
