$if(albumPart)$
Este álbum aún no contiene ningún subálbum.<br/>
Para agregar más, use:
<ul>
$if(dragAndDropEnable)$<li class="dragAndDropUploadCheck"> <b>esta área</b> para <b>arrastrar y soltar</b> carpetas (dependiendo de las capacidades de su navegador WEB) en esta área</li> $endif$
<li>la operación <a href="$albumUrl$">$albumOperation$</a></li>
</ul><br/><br/>
$endif$
Este álbum aún no contiene ningún medio.
$if(mediaPart)$<br/>
Para agregar más, use:
<ul>
$if(dragAndDropEnable)$<li class="dragAndDropUploadCheck"> <b>esta área para arrastrar y soltar medios</b></li> $endif$
<li>la operación <a href="$photoUrl$">$photoOperation$</a></li>
<li>la operación <a href="$videoUrl$">$videoOperation$</a></li>
<li>la operación <a href="$soundUrl$">$soundOperation$</a></li>
<li>la operación <a href="$streamingUrl$">$streamingOperation$</a></li>
</ul>
$endif$
