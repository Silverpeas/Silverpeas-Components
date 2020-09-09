$senderName$ vient d'accepter votre demande '$formName$'.

$if(comment)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$comment$</div>
$endif$

$if(pendingValidation)$
$if(pendingValidation.validationType.intermediate)$
Elle est désormais en attente de validation intermédiaire.
$endif$
$if(pendingValidation.validationType.final)$
Elle est désormais en attente de validation finale.
$endif$
$endif$