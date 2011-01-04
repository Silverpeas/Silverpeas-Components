$senderName$ invites you to read the following post... 
$if(senderMessage)$
<br/>Message :
"$senderMessage$"<br/> 
$endif$

Name of the post : $blogName$
Date if the event : $blogDate$
$if(blogCategorie)$
Category : $blogCategorie$
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
