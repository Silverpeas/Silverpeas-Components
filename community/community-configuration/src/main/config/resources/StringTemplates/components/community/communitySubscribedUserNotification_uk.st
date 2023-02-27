<div style="font-family:Arial;font-size:11px">
ЩОБ РОЗМІСТИТИ ТУТ СВІЙ ПЕРСОНАЛІЗОВАНИЙ ТЕКСТ

$if(notification_receiver_users)$
Це повідомлення було відправлено
користувачам: $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
$if(!notification_receiver_users)$
Це повідомлення було відправлено
$endif$
до груп: $notification_receiver_groups$
$endif$
</div>
