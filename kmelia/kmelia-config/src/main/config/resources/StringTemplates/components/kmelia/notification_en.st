$senderName$  has created a new document which is now available... 
$if(senderMessage)$
Content  :
"$senderMessage$" 
$endif$
Access path of the publication "$publicationName$" was : $path$

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
