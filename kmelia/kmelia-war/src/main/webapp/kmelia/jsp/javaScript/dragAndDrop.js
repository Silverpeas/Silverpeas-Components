var dNdVisible 	= false;
var dNdLoaded	= false;
function showHideDragDrop(targetURL1, message1, targetURL2, message2, altMessage, max_upload, webcontext, expandLabel, collapseLabel)
{
  var actionDND = document.getElementById("dNdActionLabel");
	
  if (dNdVisible)
  {
    //hide applet
    hideApplet('DragAndDrop');
    hideApplet('DragAndDropDraft');

    //change link's label
    actionDND.innerHTML = expandLabel;
  }
  else
  {
    actionDND.innerHTML = collapseLabel;
		
    if (dNdLoaded)
    {
      showApplet('DragAndDrop');
      showApplet('DragAndDropDraft');
    }
    else
    {
      try {
        loadApplet('DragAndDrop', targetURL1, message1, max_upload, webcontext, altMessage);
      } catch (e) {
      }
      try {
        loadApplet('DragAndDropDraft', targetURL2, message2, max_upload, webcontext, altMessage);
      } catch (e) {
      }
      dNdLoaded = true;
    }
  }
  dNdVisible = !dNdVisible;
}

function uploadCompleted(s) {
    if (s.indexOf('pubid=') > -1) {
      validatePublicationClassification(s);
    } else if (s.indexOf('newFolder=true') > -1) {
      reloadPage(getCurrentNodeId());
    } else {
      refreshPublications();
    }
	return true;
}