$if(albumPart)$
  Dieses Album enthält noch keine sub-Alben.<br/>
  Um einen hinzuzufügen, verwenden Sie eine der folgenden Lösungen:
  <ul>
    $if(dragAndDropEnable)$<li>einen Ordner in der Drag and Drop Zone verschieben</li>$endif$
    <li>der Vorgang <a href="$albumUrl$">$albumOperation$</a></li>
  </ul><br/><br/>
$endif$
Dieses Album enthält noch keine Medien.
$if(mediaPart)$<br/>
  Um eine hinzuzufügen, verwenden Sie eine der folgenden Lösungen:
  <ul>
    $if(dragAndDropEnable)$<li>Die Drag and Drop Zone</li>$endif$
    <li>der Vorgang <a href="$photoUrl$">$photoOperation$</a></li>
    <li>der Vorgang <a href="$videoUrl$">$videoOperation$</a></li>
    <li>der Vorgang <a href="$soundUrl$">$soundOperation$</a></li>
    <li>der Vorgang <a href="$streamingUrl$">$streamingOperation$</a></li>
  </ul>
$endif$