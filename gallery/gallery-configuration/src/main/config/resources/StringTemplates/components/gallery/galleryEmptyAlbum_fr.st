$if(albumPart)$
  Cet album ne contient pas encore de sous albums.<br/>
  Pour en ajouter, utiliser :
  <ul>
    $if(dragAndDropEnable)$<li class="dragAndDropUploadCheck"><b>cette zone</b> pour effectuer un <b>glisser-déposer</b> de dossiers (selon les capacités de votre navigateur WEB) dans cette zone</li>$endif$
    <li>l'opération <a href="$albumUrl$">$albumOperation$</a></li>
  </ul><br/><br/>
$endif$
Cet album ne contient pas encore de média.
$if(mediaPart)$<br/>
  Pour en ajouter, utiliser :
  <ul>
    $if(dragAndDropEnable)$<li class="dragAndDropUploadCheck"><b>cette zone pour effectuer un glisser-déposer de médias</b></li>$endif$
    <li>l'opération <a href="$photoUrl$">$photoOperation$</a></li>
    <li>l'opération <a href="$videoUrl$">$videoOperation$</a></li>
    <li>l'opération <a href="$soundUrl$">$soundOperation$</a></li>
    <li>l'opération <a href="$streamingUrl$">$streamingOperation$</a></li>
  </ul>
$endif$