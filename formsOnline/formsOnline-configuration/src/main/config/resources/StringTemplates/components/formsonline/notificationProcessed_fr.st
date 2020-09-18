$if(validation.validated)$
Je viens d'accepter la demande '$formName$' de $requester.displayedName$.
$endif$
$if(validation.refused)$
Je viens de refuser la demande '$formName$' de $requester.displayedName$.
$endif$
<br/>
$if(validation.comment)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$validation.comment$</div>
$endif$