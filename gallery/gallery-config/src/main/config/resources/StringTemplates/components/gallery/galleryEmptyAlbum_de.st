$if(albumPart)$
  Dieses Album enthält noch keine sub-Alben.<br/>
  Um einen hinzuzufügen, verwenden Sie eine der folgenden Lösungen:
  <ul>
    <li>einen Ordner in der Drag and Drop Zone verschieben</li>
    <li>der Vorgang "$albumOperation$"</li>
    <li><a href="$albumUrl$">Klicken Sie direkt hier</a></li>
  </ul><br/><br/>
$endif$
Dieses Album enthält noch keine Medien.
$if(mediaPart)$<br/>
  Um eine hinzuzufügen, verwenden Sie eine der folgenden Lösungen:
  <ul>
    <li>Die Drag and Drop Zone</li>
    <li>der Vorgang "$mediaOperation$"<br/>(<a href="$mediaUrl$">Klicken Sie direkt hier</a>)</li>
    <li>der Vorgang "$videoOperation$"<br/>(<a href="$videoUrl$">Klicken Sie direkt hier</a>)</li>
    <li>der Vorgang "$soundOperation$"<br/>(<a href="$soundUrl$">Klicken Sie direkt hier</a>)</li>
  </ul>
$endif$