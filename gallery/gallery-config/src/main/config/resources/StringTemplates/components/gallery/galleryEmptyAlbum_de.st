$if(albumPart)$
  This album contains no albums yet.<br/>
  To add some, use:
  <ul>
    $if(dragAndDropEnable)$<li class="dragAndDropUploadCheck"><b>this zone</b> to perform a <b>drag&drop</b> of folders</li>$endif$
    <li>the operation <a href="$albumUrl$">$albumOperation$</a></li>
  </ul><br/><br/>
$endif$
This album contains no media yet.
$if(mediaPart)$<br/>
  To add one, use:
  <ul>
    $if(dragAndDropEnable)$<li class="dragAndDropUploadCheck"><b>this zone</b> to perform a <b>drag&drop</b> of media</li>$endif$
    <li>the operation <a href="$photoUrl$">$photoOperation$</a></li>
    <li>the operation <a href="$videoUrl$">$videoOperation$</a></li>
    <li>the operation <a href="$soundUrl$">$soundOperation$</a></li>
    <li>the operation <a href="$streamingUrl$">$streamingOperation$</a></li>
  </ul>
$endif$