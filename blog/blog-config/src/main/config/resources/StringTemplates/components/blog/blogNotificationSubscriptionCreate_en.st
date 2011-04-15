The following post have been created : 

Name of the post : $blogName$
Date of the event : $blogDate$
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

