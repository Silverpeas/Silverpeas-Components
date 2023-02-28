$if(albumPart)$
Этот альбом еще не содержит подальбомов.<br/>
Чтобы добавить больше, используйте:
<ul>
$if(dragAndDropEnable)$<li class="dragAndDropUploadCheck"> <b>эту область</b> для <b>перетаскивания</b> папок (в зависимости от возможностей вашего веб-браузера) в эту область</li> $endif$
<li>операция <a href="$albumUrl$">$albumOperation$</a></li>
</ul><br/><br/>
$endif$
В этом альбоме пока нет медиафайлов.
$if(mediaPart)$<br/>
Чтобы добавить больше, используйте:
<ul>
$if(dragAndDropEnable)$<li class="dragAndDropUploadCheck"> <b>эта область для перетаскивания медиафайлов</b></li> $endif$
<li>операция <a href="$photoUrl$">$photoOperation$</a></li>
<li>операция <a href="$videoUrl$">$videoOperation$</a></li>
<li>операция <a href="$soundUrl$">$soundOperation$</a></li>
<li>операция <a href="$streamingUrl$">$streamingOperation$</a></li>
</ul>
$endif$
