$senderName$ inform you of a document... 
$if(senderMessage)$
Content  :
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
This message has been sent
 to users : $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  This message has been sent to
  $endif$
 to groups : $notification_receiver_groups$
$endif$
