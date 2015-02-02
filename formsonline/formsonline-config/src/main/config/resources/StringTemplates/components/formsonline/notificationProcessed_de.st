$if(validated)$
I just accepted $requester.displayedName$'s request based on '$formName$'.
$endif$
$if(denied)$
I just denied $requester.displayedName$'s request based on '$formName$'.
$endif$
<br/>
$if(comment)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$comment$</div>
$endif$