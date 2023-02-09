$if(validation.validated)$
Acabo de aceptar la solicitud &#39;$formName$&#39; de $requester.displayedName$.
$endif$
$if(validation.refused)$
Acabo de negar la solicitud &#39;$formName$&#39; de $requester.displayedName$.
$endif$
<br/>
Ya no tienes que intervenir.
<br/>
$if(validation.comment)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$validation.comment$</div>
$endif$
