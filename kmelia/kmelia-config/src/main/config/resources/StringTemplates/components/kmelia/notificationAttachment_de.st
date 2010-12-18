$senderName$ teilt Ihnen mit, dass Ihnen ein neues Dokument zur Verfügung steht...
$if(senderMessage)$
<br/>Message :
"$senderMessage$"<br/> 
$endif$

Access path of the document in the publication "$publicationName$" is :
$path$

$if(attachmentTitle)$
Name of the document : $attachmentTitle$<br/>
$endif$
$if($attachmentDesc$)$
Description of the document : $attachmentDesc$<br/>
$endif$
Name of the file : $attachmentFileName$<br/>
$if(attachment.majorNumber)$
Version of the document : $attachment.majorNumber$.$attachment.minorNumber$<br/>
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
