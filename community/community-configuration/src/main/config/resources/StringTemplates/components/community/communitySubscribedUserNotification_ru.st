<div style="font-family:Arial;font-size:11px">
РАЗМЕСТИТЕ ВАШ ПЕРСОНАЛИЗИРОВАННЫЙ ТЕКСТ ЗДЕСЬ

$if(notification_receiver_users)$
Это сообщение было отправлено
пользователям: $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
$if(!notification_receiver_users)$
Это сообщение было отправлено
$endif$
в группы: $notification_receiver_groups$
$endif$
</div>
