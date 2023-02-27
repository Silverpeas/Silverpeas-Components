$if(validation.validated)$
Я щойно прийняв запит &quot;$formName$&quot; з $requester.displayedName$.
$endif$
$if(validation.refused)$
Я щойно відхилив запит &quot;$formName$&quot; з $requester.displayedName$.
$endif$
<br/>
Вам більше не потрібно втручатися.
<br/>
$if(validation.comment)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$validation.comment$</div>
$endif$
