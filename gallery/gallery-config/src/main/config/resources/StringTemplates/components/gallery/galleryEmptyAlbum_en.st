$if(albumPart)$
  This album contains no albums yet.<br/>
  To add one, use one of the following:
  <ul>
    $if(dragAndDropEnable)$<li>drag a folder in the drag and drop zone</li>$endif$
    <li>the operation "$albumOperation$"</li>
    <li><a href="$albumUrl$">clic here</a></li>
  </ul><br/><br/>
$endif$
This album contains no media yet.
$if(mediaPart)$<br/>
  To add one, use one of the following:
  <ul>
    $if(dragAndDropEnable)$<li>the drag and drop zone</li>$endif$
    <li>the operation "$photoOperation$"<br/>(<a href="$photoUrl$">clic here</a>)</li>
    <li>the operation "$videoOperation$"<br/>(<a href="$videoUrl$">clic here</a>)</li>
    <li>the operation "$soundOperation$"<br/>(<a href="$soundUrl$">clic here</a>)</li>
    <li>the operation "$streamingOperation$"<br/>(<a href="$streamingUrl$">clic here</a>)</li>
  </ul>
$endif$