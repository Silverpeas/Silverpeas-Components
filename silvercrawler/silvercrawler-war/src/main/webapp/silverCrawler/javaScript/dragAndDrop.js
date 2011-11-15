var dNdVisible 	= false;
var dNdLoaded	= false;
function showHideDragDrop(targetURL1, message1, altMessage, max_upload, webcontext, expandLabel, collapseLabel)
{
  var actionDND = document.getElementById("dNdActionLabel");

  if (dNdVisible)
  {
    //hide applet
    hideApplet('DragAndDrop');

    //change link's label
    actionDND.innerHTML = expandLabel;
  }
  else
  {
    actionDND.innerHTML = collapseLabel;

    if (dNdLoaded)
    {
      showApplet('DragAndDrop');
    }
    else
    {
      try {
        loadApplet('DragAndDrop', targetURL1, message1, max_upload, webcontext, altMessage);
      } catch (e) {
      }
      dNdLoaded = true;
    }
  }
  dNdVisible = !dNdVisible;
}

function uploadCompleted(s)
{
	processDnD();
	return true;
}