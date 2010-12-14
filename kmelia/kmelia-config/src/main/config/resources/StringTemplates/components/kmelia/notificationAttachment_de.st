$senderName$ teilt Ihnen mit, dass Ihnen ein neues Dokument zur Verfügung steht...
$if(senderMessage)$
Message :
"$senderMessage$" 
$endif$
 
Access path of the document in the publication "$publicationName$" was : $path$

Name of the document : $attachmentTitle$
Description of the document : $attachmentDesc$
Name of the file : $attachmentFileName$
$if(attachment.majorNumber)$
Version of the document : $attachment.majorNumber$.$attachment.minorNumber$
$endif$

$if(notification_receiver_users)$
Diese Nachricht wurde gesendet
 nutzer : $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  Diese Nachricht wurde gesendet
  $endif$
 gruppen : $notification_receiver_groups$
$endif$
