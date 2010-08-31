$senderName$ vous informe qu'un nouveau document est à votre disposition... 
$if(senderMessage)$
Message :
"$senderMessage$" 
$endif$
 
Vous trouverez la publication "$publicationName$" ici : $path$

$if(notification_receiver_users)$
Ce message a été envoyé à : $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
Et aux groupes : $notification_receiver_groups$
$endif$