$if(albumPart)$
  Cet album ne contient pas encore de sous albums.<br/>
  Pour en ajouter un, utilisez une des solutions suivantes :
  <ul>
    $if(dragAndDropEnable)$<li>glisser un dossier dans la zone de glisser-déposer</li>$endif$
    <li>l'opération <a href="$albumUrl$">$albumOperation$</a></li>
  </ul><br/><br/>
$endif$
Cet album ne contient pas encore de média.
$if(mediaPart)$<br/>
  Pour en ajouter un, utilisez une des solutions suivantes :
  <ul>
    $if(dragAndDropEnable)$<li>la zone de glisser-déposez</li>$endif$
    <li>l'opération <a href="$photoUrl$">$photoOperation$</a></li>
    <li>l'opération <a href="$videoUrl$">$videoOperation$</a></li>
    <li>l'opération <a href="$soundUrl$">$soundOperation$</a></li>
    <li>l'opération <a href="$streamingUrl$">$streamingOperation$</a></li>
  </ul>
$endif$