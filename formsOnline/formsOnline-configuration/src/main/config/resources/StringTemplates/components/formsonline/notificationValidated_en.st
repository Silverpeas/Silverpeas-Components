$senderName$ just accepted your request '$formName$'.

$if(comment)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$comment$</div>
$endif$

$if(pendingValidation)$
$if(pendingValidation.validationType.intermediate)$
It is now awaiting intermediate validation.
$endif$
$if(pendingValidation.validationType.final)$
It is now awaiting finale validation.
$endif$
$endif$