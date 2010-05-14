var dNdVisible 	= false;
var dNdLoaded	= false;
function showHideDragDrop(targetURL, message, max_upload, webcontext, expandLabel, collapseLabel)
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
        loadApplet('DragAndDrop', targetURL, message, max_upload, webcontext);
        dNdLoaded = true;
      } catch (e) {
      }      
    }
  }
  dNdVisible = !dNdVisible;
}