$if(validation.validated)$
Я только что принял запрос &#39;$formName$&#39; $requester.displayedName$.
$endif$
$if(validation.refused)$
Я просто отклонил запрос &#39;$formName$&#39; $requester.displayedName$.
$endif$
<br/>
$if(validation.comment)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$validation.comment$</div>
$endif$
