$if(validation.validated)$
I just accepted $requester.displayedName$'s request based on '$formName$'.
$endif$
$if(validation.refused)$
I just denied $requester.displayedName$'s request based on '$formName$'.
$endif$
<br/>
Sie müssen nicht mehr eingreifen.
<br/>
$if(validation.comment)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$validation.comment$</div>
$endif$