<div style="font-family:Arial;font-size:11px">
TODO COLOCA AQUÍ TU TEXTO PERSONALIZADO

$if(notification_receiver_users)$
Este mensaje ha sido enviado
a los usuarios: $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
$if(!notification_receiver_users)$
Este mensaje ha sido enviado
$endif$
a grupos: $notification_receiver_groups$
$endif$
</div>
