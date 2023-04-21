$if(albumPart)$
Цей альбом ще не містить підальбомів.<br/>
Щоб додати більше, використовуйте:
<ul>
$if(dragAndDropEnable)$<li class="dragAndDropUploadCheck"> <b>цю область</b> , щоб <b>перетягувати</b> папки (залежно від можливостей вашого ВЕБ-браузера) у цю область</li> $endif$
<li>операція <a href="$albumUrl$">$albumOperation$</a></li>
</ul><br/><br/>
$endif$
Цей альбом ще не містить медіафайлів.
$if(mediaPart)$<br/>
Щоб додати більше, використовуйте:
<ul>
$if(dragAndDropEnable)$<li class="dragAndDropUploadCheck"> <b>цю область для перетягування медіафайлів</b></li> $endif$
<li>операція <a href="$photoUrl$">$photoOperation$</a></li>
<li>операція <a href="$videoUrl$">$videoOperation$</a></li>
<li>операція <a href="$soundUrl$">$soundOperation$</a></li>
<li>операція <a href="$streamingUrl$">$streamingOperation$</a></li>
</ul>
$endif$
