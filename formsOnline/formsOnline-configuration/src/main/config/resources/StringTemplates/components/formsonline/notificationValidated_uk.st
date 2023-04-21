$senderName$ щойно прийняв ваш запит &#39;$formName$&#39;.

$if(comment)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$comment$</div>
$endif$

$if(pendingValidation)$
$if(pendingValidation.validationType.intermediate)$
Зараз він очікує проміжної перевірки.
$endif$
$if(pendingValidation.validationType.final)$
Зараз він очікує остаточної перевірки.
$endif$
$endif$
