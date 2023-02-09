$senderName$ acaba de aceptar su solicitud &#39;$formName$&#39;.

$if(comment)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$comment$</div>
$endif$

$if(pendingValidation)$
$if(pendingValidation.validationType.intermediate)$
Ahora está a la espera de una validación intermedia.
$endif$
$if(pendingValidation.validationType.final)$
Ahora está esperando la validación final.
$endif$
$endif$
