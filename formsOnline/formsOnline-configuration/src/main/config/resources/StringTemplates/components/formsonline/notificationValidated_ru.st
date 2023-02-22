$senderName$ только что принял ваш запрос &#39;$formName$&#39;.

$if(comment)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$comment$</div>
$endif$

$if(pendingValidation)$
$if(pendingValidation.validationType.intermediate)$
Сейчас он ожидает промежуточной проверки.
$endif$
$if(pendingValidation.validationType.final)$
Сейчас он ожидает окончательной проверки.
$endif$
$endif$
