Le billet suivant vient d'être créé : 

Nom du billet : $blogName$
Date de l'évènement : $blogDate$
$if(blogCategorie)$
Catégorie : $blogCategorie$
$endif$


$if(notification_receiver_users)$
Ce message a été envoyé
 aux utilisateurs : $notification_receiver_users$
$endif$

$if(notification_receiver_groups)$
  $if(!notification_receiver_users)$
  Ce message a été envoyé
  $endif$
 aux groupes : $notification_receiver_groups$
$endif$
