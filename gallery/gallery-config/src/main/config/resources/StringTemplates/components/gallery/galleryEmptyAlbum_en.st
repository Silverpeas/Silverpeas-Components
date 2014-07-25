$if(albumPart)$
  This album contains no albums yet.<br/>
  To add one, use one of the following:
  <ul>
    $if(dragAndDropEnable)$<li>drag a folder in the drag and drop zone</li>$endif$
    <li>the operation <a href="$albumUrl$">$albumOperation$</a></li>
  </ul><br/><br/>
$endif$
This album contains no media yet.
$if(mediaPart)$<br/>
  To add one, use one of the following:
  <ul>
    $if(dragAndDropEnable)$<li>the drag and drop zone</li>$endif$
    <li>the operation <a href="$photoUrl$">$photoOperation$</a></li>
    <li>the operation <a href="$videoUrl$">$videoOperation$</a></li>
    <li>the operation <a href="$soundUrl$">$soundOperation$</a></li>
    <li>the operation <a href="$streamingUrl$">$streamingOperation$</a></li>
  </ul>
$endif$